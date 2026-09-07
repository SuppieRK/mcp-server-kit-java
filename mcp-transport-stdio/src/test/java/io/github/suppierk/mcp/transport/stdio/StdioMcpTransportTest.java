package io.github.suppierk.mcp.transport.stdio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpHandlerContext;
import io.github.suppierk.mcp.server.McpServerKit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class StdioMcpTransportTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void transportsSharingAKitKeepRequestIdsAndCancellationIndependent() throws Exception {
    var first = new CompletableFuture<JsonRpcResultResponse>();
    var second = new CompletableFuture<JsonRpcResultResponse>();
    var started = new CompletableFuture<McpHandlerContext>();
    var calls = new AtomicInteger();
    var server =
        McpServerKit.builder("shared-kit", "1", McpEmptyContext.class)
            .asyncMethod(
                "pending",
                (applicationContext, request, handlerContext) -> {
                  if (calls.getAndIncrement() == 0) {
                    started.complete(handlerContext);
                    return first;
                  }
                  return second;
                })
            .syncMethod(
                "probe",
                (applicationContext, request, handlerContext) ->
                    new JsonRpcResultResponse(
                        request.id(),
                        mapper
                            .createObjectNode()
                            .put("firstPending", !first.isDone())
                            .put("firstContextCancelled", started.join().isCancelled())
                            .put("secondCancelled", second.isCancelled())))
            .build();
    var firstInput = new PipedInputStream();
    var firstClient = new PipedOutputStream(firstInput);
    var firstOutput = new ByteArrayOutputStream();
    var firstDiagnostics = new ByteArrayOutputStream();
    var firstTransport =
        new StdioMcpTransport<>(
            server,
            McpEmptyContext.INSTANCE,
            firstInput,
            firstOutput,
            firstDiagnostics,
            Runnable::run);
    var running =
        new FutureTask<Void>(
            () -> {
              firstTransport.run();
              return null;
            });
    var reader = new Thread(running, "stdio-first-client");
    reader.start();
    try {
      firstClient.write((request(1, "pending", "") + "\n").getBytes(StandardCharsets.UTF_8));
      firstClient.flush();
      started.get(5, TimeUnit.SECONDS);
      String secondInput =
          request(1, "pending", "")
              + "\n"
              + cancellation(1)
              + "\n"
              + request(2, "probe", "")
              + "\n";
      var secondOutput = new ByteArrayOutputStream();
      var secondDiagnostics = new ByteArrayOutputStream();

      new StdioMcpTransport<>(
              server,
              McpEmptyContext.INSTANCE,
              new ByteArrayInputStream(secondInput.getBytes(StandardCharsets.UTF_8)),
              secondOutput,
              secondDiagnostics,
              Runnable::run)
          .run();

      var lines = secondOutput.toString(StandardCharsets.UTF_8).lines().toList();
      assertEquals(1, lines.size());
      var probe = mapper.readTree(lines.get(0));
      assertEquals(2, probe.path("id").intValue());
      assertEquals(
          mapper.readTree(
              "{\"firstPending\":true,\"firstContextCancelled\":false,\"secondCancelled\":true}"),
          probe.path("result"));
      assertEquals("", secondDiagnostics.toString(StandardCharsets.UTF_8));
      assertTrue(
          first.complete(
              new JsonRpcResultResponse(
                  mapper.valueToTree(1), mapper.createObjectNode().put("value", "first"))));
    } finally {
      firstClient.close();
      try {
        running.get(5, TimeUnit.SECONDS);
      } finally {
        reader.join(5000);
      }
    }
    var response = mapper.readTree(firstOutput.toByteArray());
    assertEquals(1, response.path("id").intValue());
    assertEquals("first", response.path("result").path("value").textValue());
    assertEquals("", firstDiagnostics.toString(StandardCharsets.UTF_8));
  }

  @Test
  void rejectsAnActiveDuplicateWithoutReplacingTheRequestAndAllowsReuseAfterCompletion()
      throws Exception {
    var pending = new CompletableFuture<JsonRpcResultResponse>();
    var calls = new AtomicInteger();
    var server =
        McpServerKit.builder("duplicate", "1", McpEmptyContext.class)
            .asyncMethod(
                "pending",
                (applicationContext, request, handlerContext) -> {
                  calls.incrementAndGet();
                  return pending;
                })
            .syncMethod(
                "complete",
                (applicationContext, request, handlerContext) -> {
                  pending.complete(
                      new JsonRpcResultResponse(
                          mapper.valueToTree(1),
                          mapper.createObjectNode().put("value", "original")));
                  return new JsonRpcResultResponse(request.id(), mapper.createObjectNode());
                })
            .build();
    String input =
        request(1, "pending", "")
            + "\n"
            + request(1, "pending", "")
            + "\n"
            + request(2, "complete", "")
            + "\n"
            + request(1, "pending", "")
            + "\n";
    var output = new ByteArrayOutputStream();
    var diagnostics = new ByteArrayOutputStream();

    new StdioMcpTransport<>(
            server,
            McpEmptyContext.INSTANCE,
            new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
            output,
            diagnostics,
            Runnable::run)
        .run();

    var lines = output.toString(StandardCharsets.UTF_8).lines().toList();
    assertEquals(4, lines.size());
    var duplicate = mapper.readTree(lines.get(0));
    assertEquals(1, duplicate.path("id").intValue());
    assertEquals(-32600, duplicate.path("error").path("code").intValue());
    var original = mapper.readTree(lines.get(1));
    assertEquals(1, original.path("id").intValue());
    assertEquals("original", original.path("result").path("value").textValue());
    assertEquals(2, mapper.readTree(lines.get(2)).path("id").intValue());
    assertEquals(original, mapper.readTree(lines.get(3)));
    assertEquals(2, calls.get());
    assertFalse(pending.isCancelled());
    assertEquals("", diagnostics.toString(StandardCharsets.UTF_8));
  }

  @Test
  void cancellationReachesARunningSynchronousHandler() throws Exception {
    var started = new CompletableFuture<McpHandlerContext>();
    var release = new CountDownLatch(1);
    var server =
        McpServerKit.builder("sync-cancellation", "1", McpEmptyContext.class)
            .syncMethod(
                "blocking",
                (applicationContext, request, handlerContext) -> {
                  handlerContext.cancellation().thenRun(release::countDown);
                  started.complete(handlerContext);
                  try {
                    if (!release.await(10, TimeUnit.SECONDS)) {
                      throw new IllegalStateException("The handler did not receive cancellation");
                    }
                  } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                  }
                  return new JsonRpcResultResponse(request.id(), mapper.createObjectNode());
                })
            .build();
    var executor = Executors.newSingleThreadExecutor();
    var input = new PipedInputStream();
    var client = new PipedOutputStream(input);
    var output = new ByteArrayOutputStream();
    var diagnostics = new ByteArrayOutputStream();
    var transport =
        new StdioMcpTransport<>(
            server, McpEmptyContext.INSTANCE, input, output, diagnostics, executor);
    var running =
        new FutureTask<Void>(
            () -> {
              transport.run();
              return null;
            });
    var reader = new Thread(running, "stdio-test-reader");
    reader.start();
    try {
      client.write((request(1, "blocking", "") + "\n").getBytes(StandardCharsets.UTF_8));
      client.flush();
      var context = started.get(5, TimeUnit.SECONDS);
      client.write((cancellation(1) + "\n").getBytes(StandardCharsets.UTF_8));
      client.flush();

      context.cancellation().toCompletableFuture().get(2, TimeUnit.SECONDS);
      assertTrue(context.isCancelled());
    } finally {
      release.countDown();
      client.close();
      try {
        running.get(5, TimeUnit.SECONDS);
      } finally {
        executor.shutdownNow();
        reader.join(5000);
      }
    }
    assertEquals("", output.toString(StandardCharsets.UTF_8));
    assertEquals("", diagnostics.toString(StandardCharsets.UTF_8));
  }

  @Test
  void cancellationStopsOnlyTheNamedRequestBeforeEndOfInput() throws Exception {
    var first = new CompletableFuture<JsonRpcResultResponse>();
    var second = new CompletableFuture<JsonRpcResultResponse>();
    var firstContext = new AtomicReference<McpHandlerContext>();
    var server =
        McpServerKit.builder("cancellation", "1", McpEmptyContext.class)
            .asyncMethod(
                "pending",
                (applicationContext, request, handlerContext) -> {
                  if (request.id().intValue() == 1) {
                    firstContext.set(handlerContext);
                    return first;
                  }
                  return second;
                })
            .syncMethod(
                "probe",
                (applicationContext, request, handlerContext) -> {
                  var state =
                      mapper
                          .createObjectNode()
                          .put("cancelled", first.isCancelled())
                          .put("contextCancelled", firstContext.get().isCancelled())
                          .put("otherPending", !second.isDone());
                  second.complete(
                      new JsonRpcResultResponse(
                          mapper.valueToTree(2), mapper.createObjectNode().put("value", "second")));
                  return new JsonRpcResultResponse(request.id(), state);
                })
            .build();
    String input =
        request(1, "pending", "")
            + "\n"
            + request(2, "pending", "")
            + "\n"
            + cancellation(999)
            + "\n"
            + cancellation(1)
            + "\n"
            + request(3, "probe", "")
            + "\n";
    var output = new ByteArrayOutputStream();
    var diagnostics = new ByteArrayOutputStream();

    new StdioMcpTransport<>(
            server,
            McpEmptyContext.INSTANCE,
            new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
            output,
            diagnostics,
            Runnable::run)
        .run();

    var lines = output.toString(StandardCharsets.UTF_8).lines().toList();
    assertEquals(2, lines.size());
    assertEquals(2, mapper.readTree(lines.get(0)).path("id").intValue());
    assertEquals("second", mapper.readTree(lines.get(0)).path("result").path("value").textValue());
    var probe = mapper.readTree(lines.get(1));
    assertEquals(3, probe.path("id").intValue());
    assertEquals(
        mapper.readTree("{\"cancelled\":true,\"contextCancelled\":true,\"otherPending\":true}"),
        probe.path("result"));
    assertEquals("", diagnostics.toString(StandardCharsets.UTF_8));
  }

  @Test
  void writesOnlyProtocolResponsesToStandardOutput() throws Exception {
    String input =
        request(1, "server/discover", "")
            + "\n"
            + "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/ignored\",\"params\":{}}\n";
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("stdio-test", "1", McpEmptyContext.class).build();
    StdioMcpTransport<McpEmptyContext> transport =
        new StdioMcpTransport<>(
            server,
            McpEmptyContext.INSTANCE,
            new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
            output,
            diagnostics,
            Runnable::run);

    transport.run();

    String[] lines = output.toString(StandardCharsets.UTF_8).lines().toArray(String[]::new);
    assertEquals(1, lines.length);
    JsonNode response = mapper.readTree(lines[0]);
    assertEquals(1, response.path("id").intValue());
    assertEquals(
        McpProtocol.REVISION,
        response.path("result").path("supportedVersions").path(0).textValue());
    assertEquals("", diagnostics.toString(StandardCharsets.UTF_8));
  }

  @Test
  void returnsParseErrorsAsNewlineDelimitedJson() throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("stdio-test", "1", McpEmptyContext.class).build();
    StdioMcpTransport<McpEmptyContext> transport =
        new StdioMcpTransport<>(
            server,
            McpEmptyContext.INSTANCE,
            new ByteArrayInputStream("{\n".getBytes(StandardCharsets.UTF_8)),
            output,
            new ByteArrayOutputStream(),
            Runnable::run);

    transport.run();

    JsonNode response = mapper.readTree(output.toByteArray());
    assertEquals(-32700, response.path("error").path("code").intValue());
    assertTrue(output.toString(StandardCharsets.UTF_8).endsWith("\n"));
  }

  @Test
  void createsTheSystemTransport() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("stdio-test", "1", McpEmptyContext.class).build();

    StdioMcpTransport<McpEmptyContext> transport =
        StdioMcpTransport.system(server, McpEmptyContext.INSTANCE);

    assertTrue(transport != null);
  }

  @Test
  void reportsProtocolOutputFailuresToDiagnostics() throws Exception {
    ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
    OutputStream failedOutput =
        new OutputStream() {
          @Override
          public void write(int value) throws IOException {
            throw new IOException("closed");
          }
        };
    StdioMcpTransport<McpEmptyContext> transport =
        new StdioMcpTransport<>(
            McpServerKit.builder("stdio-test", "1", McpEmptyContext.class).build(),
            McpEmptyContext.INSTANCE,
            new ByteArrayInputStream(
                request(1, "server/discover", "").getBytes(StandardCharsets.UTF_8)),
            failedOutput,
            diagnostics,
            Runnable::run);

    transport.run();

    assertEquals("MCP request failed\n", diagnostics.toString(StandardCharsets.UTF_8));
  }

  @Test
  void eofCancelsQueuedDispatchBeforeRunReturns() throws Exception {
    var invoked = new AtomicBoolean();
    var queued = new ArrayDeque<Runnable>();
    var output = new ByteArrayOutputStream();
    var server =
        McpServerKit.builder("queued", "1", McpEmptyContext.class)
            .syncMethod(
                "queued",
                (applicationContext, request, handlerContext) -> {
                  invoked.set(true);
                  return new JsonRpcResultResponse(request.id(), mapper.createObjectNode());
                })
            .build();
    var transport =
        new StdioMcpTransport<>(
            server,
            McpEmptyContext.INSTANCE,
            new ByteArrayInputStream(request(1, "queued", "").getBytes(StandardCharsets.UTF_8)),
            output,
            new ByteArrayOutputStream(),
            queued::add);

    transport.run();

    assertEquals(1, queued.size());
    assertFalse(invoked.get());
    assertEquals(0, output.size());

    queued.remove().run();

    assertFalse(invoked.get());
    assertEquals(0, output.size());
  }

  @Test
  void reusesTheExactConstructorContextWithoutOwningIt() throws Exception {
    AtomicReference<ApplicationContext> first = new AtomicReference<>();
    AtomicReference<ApplicationContext> second = new AtomicReference<>();
    var calls = new java.util.concurrent.atomic.AtomicInteger();
    var serverKit =
        McpServerKit.builder("stdio-context", "1", ApplicationContext.class)
            .syncMethod(
                "context",
                (applicationContext, request, handlerContext) -> {
                  (calls.getAndIncrement() == 0 ? first : second).set(applicationContext);
                  return new JsonRpcResultResponse(
                      request.id(),
                      mapper.createObjectNode().put("value", applicationContext.value));
                })
            .build();
    var applicationContext = new ApplicationContext("exact");
    String input = request(1, "context", "") + "\n" + request(2, "context", "") + "\n";
    var transport =
        new StdioMcpTransport<>(
            serverKit,
            applicationContext,
            new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
            new ByteArrayOutputStream(),
            new ByteArrayOutputStream(),
            Runnable::run);

    transport.run();

    assertSame(applicationContext, first.get());
    assertSame(applicationContext, second.get());
    assertEquals(0, applicationContext.closeCalls);
    assertThrows(
        NullPointerException.class,
        () ->
            new StdioMcpTransport<>(
                serverKit,
                null,
                new ByteArrayInputStream(new byte[0]),
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream(),
                Runnable::run));
  }

  private static String cancellation(int id) {
    return "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/cancelled\",\"params\":{\"requestId\":"
        + id
        + "}}";
  }

  private static String request(int id, String method, String extraParams) {
    return "{\"jsonrpc\":\"2.0\",\"id\":"
        + id
        + ",\"method\":\""
        + method
        + "\",\"params\":{\"_meta\":{\""
        + McpProtocol.PROTOCOL_VERSION_KEY
        + "\":\""
        + McpProtocol.REVISION
        + "\",\""
        + McpProtocol.CLIENT_CAPABILITIES_KEY
        + "\":{}}"
        + extraParams
        + "}}";
  }

  private static final class ApplicationContext implements AutoCloseable {
    private final String value;
    private int closeCalls;

    private ApplicationContext(String value) {
      this.value = value;
    }

    @Override
    public void close() {
      closeCalls++;
    }
  }
}
