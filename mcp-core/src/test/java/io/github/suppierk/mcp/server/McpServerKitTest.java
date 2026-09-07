package io.github.suppierk.mcp.server;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcNotification;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpCancelledNotificationParams;
import io.github.suppierk.mcp.protocol.McpClientNotification;
import io.github.suppierk.mcp.protocol.McpCompleteRequest;
import io.github.suppierk.mcp.protocol.McpCompleteResult;
import io.github.suppierk.mcp.protocol.McpDiscoverRequest;
import io.github.suppierk.mcp.protocol.McpGetPromptRequest;
import io.github.suppierk.mcp.protocol.McpGetPromptResult;
import io.github.suppierk.mcp.protocol.McpIcon;
import io.github.suppierk.mcp.protocol.McpListPromptsRequest;
import io.github.suppierk.mcp.protocol.McpListResourceTemplatesRequest;
import io.github.suppierk.mcp.protocol.McpListResourcesRequest;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpMetaObject;
import io.github.suppierk.mcp.protocol.McpPrompt;
import io.github.suppierk.mcp.protocol.McpPromptArgument;
import io.github.suppierk.mcp.protocol.McpPromptMessage;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.protocol.McpReadResourceRequest;
import io.github.suppierk.mcp.protocol.McpReadResourceResult;
import io.github.suppierk.mcp.protocol.McpResource;
import io.github.suppierk.mcp.protocol.McpResourceTemplate;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotification;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotificationParams;
import io.github.suppierk.mcp.protocol.McpRole;
import io.github.suppierk.mcp.protocol.McpSubscriptionsAcknowledgedNotification;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenRequest;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTextResourceContents;
import io.github.suppierk.mcp.protocol.McpTool;
import io.github.suppierk.mcp.protocol.McpToolAnnotations;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class McpServerKitTest {
  private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

  @Test
  void preservesDomainDenialsForToolsWithAnOutputSchema() {
    var output = JSON.objectNode().put("type", "object");
    output.putArray("required").add("answer");
    output.putObject("properties").putObject("answer").put("type", "string");
    var tool =
        McpTool.mcpTool()
            .name("answer")
            .inputSchema(JSON.objectNode().put("type", "object"))
            .outputSchema(output)
            .build();
    var denial =
        McpCallToolResult.mcpCallToolResult()
            .resultType("complete")
            .content(List.of(new McpTextContent("Not permitted")))
            .isError(true)
            .build();
    var server =
        McpServerKit.builder("denial", "1", McpEmptyContext.class)
            .syncTool(tool, (applicationContext, parameters, handlerContext) -> denial)
            .build();

    var result =
        success(
            server,
            handle(server, request(42, "tools/call", JSON.objectNode().put("name", "answer"))));

    assertTrue(result.result().path("isError").booleanValue());
    assertEquals("Not permitted", result.result().path("content").path(0).path("text").textValue());
  }

  @Test
  void acceptsAndPreservesRequestExtensionMetadata() throws Exception {
    var server = McpServerKit.builder("metadata", "1", McpEmptyContext.class).build();
    var parameters = JSON.objectNode();
    metadata(parameters).put("com.example/trace", "trace-42").put("progressToken", "progress-42");
    var original = request(42, "tools/list", parameters);

    var decoded =
        assertInstanceOf(McpListToolsRequest.class, server.decode(server.encode(original)));

    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    assertEquals(mapper.readTree(server.encode(original)), mapper.readTree(server.encode(decoded)));
  }

  @Test
  void preservesRequestIdWhenToolParametersAreMalformed() {
    var server = McpServerKit.builder("decode", "1", McpEmptyContext.class).build();
    var parameters = JSON.objectNode().put("name", "hello");
    parameters.putArray("arguments");

    var decoded =
        assertInstanceOf(
            JsonRpcErrorResponse.class,
            server.decode(server.encode(request(42, "tools/call", parameters))));
    var failure = error(handle(server, decoded));

    assertEquals(McpInvalidParamsException.CODE, failure.code());
    assertEquals(JSON.numberNode(42), failure.id());
  }

  @Test
  void rejectsTrailingJsonAfterARequest() {
    var server = McpServerKit.builder("decode", "1", McpEmptyContext.class).build();
    String input =
        new String(server.encode(request(42, "ping", JSON.objectNode())), StandardCharsets.UTF_8);

    assertEquals(
        McpParseException.CODE,
        assertInstanceOf(
                JsonRpcErrorResponse.class,
                server.decode((input + " {}").getBytes(StandardCharsets.UTF_8)))
            .code());
  }

  @Test
  void builderRequiresTheApplicationContextType() {
    var builder = McpServerKit.builder("context", "1", String.class);

    McpServerKit<String> serverKit = builder.build();

    assertDoesNotThrow(serverKit::close);
    assertFalse(
        Arrays.stream(builder.getClass().getDeclaredFields())
            .anyMatch(field -> field.getType().equals(Class.class)));
    assertThrows(
        NullPointerException.class,
        () -> McpServerKit.builder("context", "1", (Class<String>) null));
  }

  @Test
  void emptyApplicationContextIsExplicit() {
    assertArrayEquals(new McpEmptyContext[] {McpEmptyContext.INSTANCE}, McpEmptyContext.values());
    assertFalse(
        Stream.of(McpServerKit.class.getMethods())
            .anyMatch(
                method -> method.getName().equals("builder") && method.getParameterCount() == 2));
  }

  @Test
  void passesTheExactApplicationContextOnlyToTheSelectedHandler() {
    var seenContext = new AtomicReference<ApplicationContext>();
    var seenHandlerContext = new AtomicReference<McpHandlerContext>();
    var serverKit =
        McpServerKit.builder("context", "1", ApplicationContext.class)
            .syncTool(
                new McpTool("context", JSON.objectNode().put("type", "object")),
                (applicationContext, parameters, handlerContext) -> {
                  seenContext.set(applicationContext);
                  seenHandlerContext.set(handlerContext);
                  return toolResult(applicationContext.value());
                })
            .build();
    var applicationContext = new ApplicationContext("exact");
    var call = JSON.objectNode().put("name", "context");

    assertEquals(
        "exact",
        success(serverKit, handle(serverKit, applicationContext, request(1, "tools/call", call)))
            .result()
            .path("content")
            .path(0)
            .path("text")
            .textValue());
    assertSame(applicationContext, seenContext.get());
    assertInstanceOf(McpHandlerContext.class, seenHandlerContext.get());
    assertEquals(
        McpProtocol.REVISION,
        success(
                serverKit,
                handle(
                    serverKit,
                    new ApplicationContext("built-in"),
                    request(2, "server/discover", JSON.objectNode())))
            .result()
            .path("supportedVersions")
            .path(0)
            .textValue());
    assertSame(applicationContext, seenContext.get());
    assertThrows(
        NullPointerException.class, () -> serverKit.handle(null, request(3, "tools/call", call)));
    assertFalse(
        Stream.of(McpServerKit.class.getMethods())
            .anyMatch(
                method -> method.getName().equals("handle") && method.getParameterCount() == 1));
  }

  @Test
  void isolatesApplicationContextsAcrossConcurrentRequests() throws Exception {
    var entered = new CountDownLatch(2);
    var proceed = new CountDownLatch(1);
    var firstSeen = new AtomicReference<ApplicationContext>();
    var secondSeen = new AtomicReference<ApplicationContext>();
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      var serverKit =
          McpServerKit.builder("concurrent-context", "1", ApplicationContext.class)
              .asyncMethod(
                  "context",
                  (applicationContext, call, handlerContext) ->
                      CompletableFuture.supplyAsync(
                          () -> {
                            (call.id().intValue() == 1 ? firstSeen : secondSeen)
                                .set(applicationContext);
                            entered.countDown();
                            try {
                              proceed.await(5, TimeUnit.SECONDS);
                            } catch (InterruptedException exception) {
                              Thread.currentThread().interrupt();
                              throw new IllegalStateException(exception);
                            }
                            return response(call, applicationContext.value());
                          },
                          executor))
              .build();
      var firstContext = new ApplicationContext("first");
      var secondContext = new ApplicationContext("second");
      var first = new RecordingSubscriber();
      var second = new RecordingSubscriber();

      serverKit.handle(firstContext, request(1, "context", JSON.objectNode())).subscribe(first);
      serverKit.handle(secondContext, request(2, "context", JSON.objectNode())).subscribe(second);
      assertTrue(entered.await(5, TimeUnit.SECONDS));
      proceed.countDown();
      first.completion.orTimeout(5, TimeUnit.SECONDS).join();
      second.completion.orTimeout(5, TimeUnit.SECONDS).join();

      assertSame(firstContext, firstSeen.get());
      assertSame(secondContext, secondSeen.get());
    } finally {
      proceed.countDown();
      executor.shutdownNow();
    }
  }

  @Test
  void decodesAndEncodesEveryMessageShape() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("codec", "1", McpEmptyContext.class).build();
    JsonRpcRequest request = request(1, "server/discover", JSON.objectNode());
    JsonRpcNotification notification =
        new JsonRpcNotification("notifications/ignored", JSON.objectNode());
    JsonRpcResultResponse success =
        new JsonRpcResultResponse(JSON.numberNode(1), JSON.objectNode().put("ok", true));
    JsonRpcErrorResponse failure =
        new JsonRpcErrorResponse(
            JSON.numberNode(1), -1, "failed", Optional.of(JSON.objectNode().put("why", "x")));

    McpDiscoverRequest decodedRequest =
        assertInstanceOf(McpDiscoverRequest.class, server.decode(server.encode(request)));
    assertEquals(request.id(), decodedRequest.id());
    assertEquals(request.method(), decodedRequest.method());

    for (JsonRpcMessage message : List.of(notification, success, failure)) {
      assertEquals(message, server.decode(server.encode(message)));
    }
    assertEquals(
        McpParseException.CODE,
        assertInstanceOf(
                JsonRpcErrorResponse.class,
                server.decode("not json".getBytes(StandardCharsets.UTF_8)))
            .code());
    assertEquals(
        McpParseException.CODE,
        assertInstanceOf(JsonRpcErrorResponse.class, server.decode(new byte[0])).code());
    assertEquals(
        McpInvalidRequestException.CODE,
        assertInstanceOf(
                JsonRpcErrorResponse.class,
                server.decode("{\"jsonrpc\":\"1.0\"}".getBytes(StandardCharsets.UTF_8)))
            .code());
    assertThrows(NullPointerException.class, () -> server.decode(null));
    assertThrows(NullPointerException.class, () -> server.encode(null));
    server.close();
  }

  @Test
  void rejectsInvalidMessageShapesAndUnexpectedInboundMessages() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("messages", "1", McpEmptyContext.class).build();
    for (String json :
        List.of(
            "[]",
            "{\"jsonrpc\":\"2.0\",\"method\":1}",
            "{\"jsonrpc\":\"2.0\",\"method\":\"x\",\"params\":[]}",
            "{\"jsonrpc\":\"2.0\",\"id\":{},\"method\":\"x\"}",
            "{\"jsonrpc\":\"2.0\",\"id\":1}",
            "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{},\"error\":{}}",
            "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{}}")) {
      assertInstanceOf(
          JsonRpcErrorResponse.class, server.decode(json.getBytes(StandardCharsets.UTF_8)));
    }

    JsonRpcMessage parseFailure = server.decode("not json".getBytes(StandardCharsets.UTF_8));
    assertEquals(parseFailure, handle(server, parseFailure).get(0));
    assertEquals(
        McpInvalidRequestException.CODE,
        error(handle(server, new JsonRpcResultResponse(JSON.numberNode(1), JSON.objectNode())))
            .code());
    assertEquals(
        List.of(),
        handle(server, new JsonRpcNotification("notifications/ignored", JSON.objectNode())));
    assertThrows(NullPointerException.class, () -> server.handle(McpEmptyContext.INSTANCE, null));

    Flow.Publisher<JsonRpcMessage> publication =
        server.handle(McpEmptyContext.INSTANCE, request(2, "server/discover", JSON.objectNode()));
    publication.subscribe(new RecordingSubscriber());
    RecordingSubscriber second = new RecordingSubscriber();
    publication.subscribe(second);
    assertInstanceOf(IllegalStateException.class, second.failure.join());
  }

  @Test
  void decodesAndAcceptsDefinedNotificationsWhileIgnoringUnknownOnes() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("notifications", "1", McpEmptyContext.class).build();
    var cancellation =
        new McpClientNotification(
            new McpCancelledNotificationParams(
                Optional.empty(), Optional.of("done"), JSON.numberNode(42)));

    McpClientNotification decoded =
        assertInstanceOf(McpClientNotification.class, server.decode(server.encode(cancellation)));

    assertEquals(cancellation.params(), decoded.params());
    assertEquals(List.of(), handle(server, cancellation));
    assertEquals(
        List.of(),
        handle(server, new JsonRpcNotification("notifications/unknown", JSON.objectNode())));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("typedRequests")
  void decodesBuiltInRequestsToExactTypes(
      String method, ObjectNode values, Class<? extends JsonRpcMessage> expectedType) {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("typed", "1", McpEmptyContext.class).build();

    assertEquals(expectedType, server.decode(server.encode(request(1, method, values))).getClass());
  }

  @Test
  void discoversIdentityAndRejectsInvalidMetadata() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("server", "1", McpEmptyContext.class)
            .title("Title")
            .description("Description")
            .websiteUrl(URI.create("https://example.com"))
            .instructions("Use it.")
            .icon(new McpIcon(URI.create("data:image/png;base64,AA==")))
            .build();
    JsonRpcResultResponse response =
        success(server, handle(server, request(1, "server/discover", JSON.objectNode())));
    assertEquals(
        McpProtocol.REVISION, response.result().path("supportedVersions").path(0).textValue());
    assertEquals(
        "server",
        response
            .result()
            .path("_meta")
            .path("io.modelcontextprotocol/serverInfo")
            .path("name")
            .textValue());
    assertEquals("Use it.", response.result().path("instructions").textValue());

    JsonRpcRequest absent =
        new JsonRpcRequest(JSON.numberNode(2), "server/discover", JSON.objectNode());
    assertEquals(McpInvalidParamsException.CODE, error(handle(server, absent)).code());
    ObjectNode unsupported = JSON.objectNode();
    metadata(unsupported).put(McpProtocol.PROTOCOL_VERSION_KEY, "1900-01-01");
    assertEquals(
        McpUnsupportedProtocolVersionException.CODE,
        error(
                handle(
                    server, new JsonRpcRequest(JSON.numberNode(3), "server/discover", unsupported)))
            .code());
  }

  @Test
  void listsCallsAndValidatesTools() {
    ObjectNode input = JSON.objectNode().put("type", "object");
    input.putArray("required").add("value");
    input.putObject("properties").putObject("value").put("type", "string");
    ObjectNode output = JSON.objectNode().put("type", "object");
    output.putArray("required").add("echo");
    output.putObject("properties").putObject("echo").put("type", "string");
    McpTool tool =
        new McpTool(
            Optional.of(new McpMetaObject(JSON.objectNode().put("owner", "test"))),
            Optional.of(
                new McpToolAnnotations(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(true),
                    Optional.empty())),
            Optional.of("Returns text."),
            Optional.empty(),
            input,
            "echo",
            Optional.of(output),
            Optional.of("Echo"));
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("tools", "1", McpEmptyContext.class)
            .syncTool(
                tool,
                (applicationContext, request, handlerContext) -> {
                  String value = request.arguments().orElseThrow().path("value").textValue();
                  return new McpCallToolResult(
                      List.of(new McpTextContent(value)), JSON.objectNode().put("echo", value));
                })
            .build();

    JsonRpcResultResponse listed =
        success(server, handle(server, request(1, "tools/list", JSON.objectNode())));
    assertEquals("echo", listed.result().path("tools").path(0).path("name").textValue());
    ObjectNode call = JSON.objectNode().put("name", "echo");
    call.set("arguments", JSON.objectNode().put("value", "hello"));
    assertEquals(
        "hello",
        success(server, handle(server, request(2, "tools/call", call)))
            .result()
            .path("structuredContent")
            .path("echo")
            .textValue());

    ObjectNode invalid = JSON.objectNode().put("name", "echo");
    assertEquals(
        McpInvalidParamsException.CODE,
        error(handle(server, request(3, "tools/call", invalid))).code());
    assertEquals(
        McpInvalidParamsException.CODE,
        error(handle(server, request(4, "tools/call", JSON.objectNode().put("name", "missing"))))
            .code());

    McpServerKit<McpEmptyContext> invalidOutput =
        McpServerKit.builder("output", "1", McpEmptyContext.class)
            .syncTool(
                tool,
                (applicationContext, request, handlerContext) ->
                    new McpCallToolResult(List.of(new McpTextContent("missing output"))))
            .build();
    assertEquals(
        McpInternalException.CODE,
        error(handle(invalidOutput, request(5, "tools/call", call))).code());
  }

  @Test
  void listsAndCallsResourcesTemplatesAndPrompts() {
    McpResource resource = new McpResource(URI.create("file:///fixed"), "fixed");
    McpResourceTemplate template = new McpResourceTemplate("file:///{name}", "files");
    McpPrompt prompt =
        new McpPrompt(
            Optional.empty(),
            Optional.of(List.of(new McpPromptArgument("name", Optional.of("Name"), true))),
            Optional.of("Greets a name."),
            Optional.empty(),
            "hello",
            Optional.empty());
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("features", "1", McpEmptyContext.class)
            .syncResource(
                resource,
                (applicationContext, request, handlerContext) ->
                    resourceResult(request.uri(), "fixed"))
            .syncResourceTemplate(
                template,
                Pattern.compile("file:///.*"),
                (applicationContext, request, handlerContext) ->
                    resourceResult(request.uri(), "template"))
            .syncPrompt(
                prompt, (applicationContext, request, handlerContext) -> promptResult("prompt"))
            .build();

    assertEquals(
        "fixed",
        success(server, handle(server, request(1, "resources/list", JSON.objectNode())))
            .result()
            .path("resources")
            .path(0)
            .path("name")
            .textValue());
    assertEquals(
        "files",
        success(server, handle(server, request(2, "resources/templates/list", JSON.objectNode())))
            .result()
            .path("resourceTemplates")
            .path(0)
            .path("name")
            .textValue());
    assertEquals(
        "fixed",
        success(
                server,
                handle(
                    server,
                    request(3, "resources/read", JSON.objectNode().put("uri", "file:///fixed"))))
            .result()
            .path("contents")
            .path(0)
            .path("text")
            .textValue());
    assertEquals(
        "template",
        success(
                server,
                handle(
                    server,
                    request(4, "resources/read", JSON.objectNode().put("uri", "file:///other"))))
            .result()
            .path("contents")
            .path(0)
            .path("text")
            .textValue());
    assertEquals(
        McpInvalidParamsException.CODE,
        error(
                handle(
                    server,
                    request(5, "resources/read", JSON.objectNode().put("uri", "https://other"))))
            .code());
    assertEquals(
        "hello",
        success(server, handle(server, request(6, "prompts/list", JSON.objectNode())))
            .result()
            .path("prompts")
            .path(0)
            .path("name")
            .textValue());
    assertEquals(
        McpInvalidParamsException.CODE,
        error(handle(server, request(7, "prompts/get", JSON.objectNode().put("name", "hello"))))
            .code());
    ObjectNode promptRequest = JSON.objectNode().put("name", "hello");
    promptRequest.set("arguments", JSON.objectNode().put("name", "Ada"));
    assertEquals(
        "prompt",
        success(server, handle(server, request(8, "prompts/get", promptRequest)))
            .result()
            .path("messages")
            .path(0)
            .path("content")
            .path("text")
            .textValue());
  }

  @Test
  void usesApplicationOwnedAsyncExecutionAndInfrastructureFailures() throws Exception {
    AtomicReference<String> thread = new AtomicReference<>();
    ExecutorService executor =
        Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "mcp-test-executor"));
    try {
      McpServerKit<McpEmptyContext> server =
          McpServerKit.builder("custom", "1", McpEmptyContext.class)
              .asyncMethod(
                  "example/run",
                  (applicationContext, request, handlerContext) ->
                      CompletableFuture.supplyAsync(
                          () -> {
                            thread.set(Thread.currentThread().getName());
                            return response(request, "custom");
                          },
                          executor))
              .build();
      assertEquals(
          "custom",
          success(server, handle(server, request(1, "example/run", JSON.objectNode())))
              .result()
              .path("value")
              .textValue());
      assertEquals("mcp-test-executor", thread.get());
      assertEquals(
          McpMethodNotFoundException.CODE,
          error(handle(server, request(2, "missing", JSON.objectNode()))).code());
    } finally {
      executor.shutdownNow();
    }

    McpServerKit<McpEmptyContext> thrown =
        McpServerKit.builder("failure", "1", McpEmptyContext.class)
            .syncMethod(
                "failure",
                (applicationContext, request, handlerContext) -> {
                  throw new IllegalStateException("boom");
                })
            .build();
    JsonRpcErrorResponse failure = error(handle(thrown, request(1, "failure", JSON.objectNode())));
    assertEquals(McpInternalException.CODE, failure.code());
    assertEquals("Internal error", failure.message());
    assertEquals(Optional.empty(), failure.data());
  }

  @Test
  void publishesSubscriptionsAndHonorsClosure() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("subscriptions", "1", McpEmptyContext.class)
            .syncResource(
                new McpResource(URI.create("file:///live"), "live"),
                (applicationContext, request, handlerContext) ->
                    resourceResult(request.uri(), "live"))
            .build();
    ObjectNode filters = JSON.objectNode();
    filters.putArray("resourceSubscriptions").add("file:///live");
    ObjectNode params = JSON.objectNode();
    params.set("notifications", filters);
    RecordingSubscriber subscriber = new RecordingSubscriber();
    server
        .handle(McpEmptyContext.INSTANCE, request(10, "subscriptions/listen", params))
        .subscribe(subscriber);
    assertEquals(
        "notifications/subscriptions/acknowledged",
        assertInstanceOf(McpSubscriptionsAcknowledgedNotification.class, subscriber.messages.get(0))
            .method());
    server.emit(
        new McpResourceUpdatedNotification(
            new McpResourceUpdatedNotificationParams(
                Optional.empty(), URI.create("file:///live"))));
    assertEquals(2, subscriber.messages.size());
    subscriber.subscription.cancel();
    server.close();
    assertThrows(
        IllegalStateException.class,
        () ->
            server.emit(
                new McpResourceUpdatedNotification(
                    new McpResourceUpdatedNotificationParams(
                        Optional.empty(), URI.create("file:///live")))));

    RecordingSubscriber closed = new RecordingSubscriber();
    server
        .handle(McpEmptyContext.INSTANCE, request(11, "server/discover", JSON.objectNode()))
        .subscribe(closed);
    assertInstanceOf(IllegalStateException.class, closed.failure.join());
  }

  @Test
  void rejectsInvalidDeclarationsAndDuplicates() {
    ObjectNode schema = JSON.objectNode().put("type", "object");
    McpServerKit.Builder<McpEmptyContext> builder =
        McpServerKit.builder("duplicates", "1", McpEmptyContext.class);
    builder.syncTool(
        new McpTool("tool", schema),
        (applicationContext, request, handlerContext) -> toolResult("x"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            builder.syncTool(
                new McpTool("tool", schema),
                (applicationContext, request, handlerContext) -> toolResult("x")));
    for (String method : clientToServerMethods()) {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              McpServerKit.builder("x", "1", McpEmptyContext.class)
                  .syncMethod(
                      method,
                      (applicationContext, request, handlerContext) -> response(request, "x"))
                  .build());
    }
    assertThrows(IllegalArgumentException.class, () -> new McpTool("bad", JSON.objectNode()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new McpIcon(
                Optional.empty(), Optional.empty(), URI.create("file:///x"), Optional.of("other")));
    assertThrows(
        IllegalArgumentException.class, () -> new McpResource(URI.create("file:///x"), " "));
    assertThrows(IllegalArgumentException.class, () -> new McpPrompt(" "));
    assertThrows(
        IllegalArgumentException.class, () -> new McpPromptArgument(" ", Optional.empty(), false));
  }

  @Test
  void exposesCopiedToolInputSchemasWithoutExposingRegistrations() {
    ObjectNode schema = JSON.objectNode().put("type", "object");
    schema.putObject("properties").putObject("value").put("type", "string");
    var server = serverWithToolSchema(schema);

    ObjectNode supplied = server.toolInputSchema("reference").orElseThrow();
    supplied.put("changed", true);

    assertFalse(server.toolInputSchema("reference").orElseThrow().has("changed"));
    assertEquals(Optional.empty(), server.toolInputSchema("missing"));
    assertThrows(NullPointerException.class, () -> server.toolInputSchema(null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("externalSchemaReferences")
  void rejectsExternalSchemaReferences(
      String description, String keyword, String reference, String baseIdentifier) {
    ObjectNode schema = JSON.objectNode().put("type", "object").put(keyword, reference);
    if (baseIdentifier != null) {
      schema.put("$id", baseIdentifier);
    }

    assertThrows(
        IllegalArgumentException.class,
        () ->
            McpServerKit.builder("schema", "1", McpEmptyContext.class)
                .syncTool(
                    new McpTool("external", schema),
                    (applicationContext, request, handlerContext) -> toolResult("x"))
                .build());
  }

  @Test
  void acceptsSameDocumentSchemaReferences() {
    ObjectNode referenceSchema = JSON.objectNode().put("type", "object");
    referenceSchema.putObject("$defs").putObject("text").put("type", "string");
    referenceSchema.putObject("properties").putObject("value").put("$ref", "#/$defs/text");
    ObjectNode dynamicSchema = JSON.objectNode().put("type", "object");
    dynamicSchema
        .putObject("$defs")
        .putObject("text")
        .put("$dynamicAnchor", "text")
        .put("type", "string");
    dynamicSchema.putObject("properties").putObject("value").put("$dynamicRef", "#text");

    assertDoesNotThrow(() -> serverWithToolSchema(referenceSchema));
    assertDoesNotThrow(() -> serverWithToolSchema(dynamicSchema));
    assertDoesNotThrow(
        () -> serverWithToolSchema(JSON.objectNode().put("type", "object").put("$ref", "")));
  }

  @Test
  void convertsAnExtensionResponseIdMismatchToACorrelatedInternalError() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("extension-id", "1", McpEmptyContext.class)
            .syncMethod(
                "extension/run",
                (applicationContext, request, handlerContext) ->
                    new JsonRpcResultResponse(JSON.numberNode(999), JSON.objectNode()))
            .build();

    JsonRpcErrorResponse response =
        error(handle(server, request(42, "extension/run", JSON.objectNode())));

    assertEquals(JSON.numberNode(42), response.id());
    assertEquals(-32603, response.code());
    assertEquals("Internal error", response.message());

    McpServerKit<McpEmptyContext> customErrorServer =
        McpServerKit.builder("custom-error", "1", McpEmptyContext.class)
            .syncMethod(
                "extension/error",
                (applicationContext, call, handlerContext) ->
                    new JsonRpcErrorResponse(
                        call.id(), -32099, "Extension error", Optional.empty()))
            .build();
    JsonRpcErrorResponse custom =
        error(handle(customErrorServer, request(43, "extension/error", JSON.objectNode())));
    assertEquals(JSON.numberNode(43), custom.id());
    assertEquals(-32099, custom.code());
  }

  @Test
  void dispatchesCompletionAndDoesNotDispatchExtensionNotifications() {
    AtomicBoolean extensionInvoked = new AtomicBoolean();
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("remaining-methods", "1", McpEmptyContext.class)
            .syncCompletion(
                (applicationContext, parameters, handlerContext) -> {
                  ObjectNode value = JSON.objectNode();
                  value.putArray("values").add("done");
                  return new McpCompleteResult(Optional.empty(), value, "complete");
                })
            .syncMethod(
                "extension/notify",
                (applicationContext, request, handlerContext) -> {
                  extensionInvoked.set(true);
                  return response(request, "called");
                })
            .build();
    ObjectNode completion = JSON.objectNode();
    completion.putObject("argument").put("name", "value").put("value", "d");
    completion.putObject("ref").put("type", "ref/prompt").put("name", "hello");

    assertEquals(
        "done",
        success(server, handle(server, request(44, McpCompleteRequest.METHOD, completion)))
            .result()
            .path("completion")
            .path("values")
            .path(0)
            .textValue());
    assertEquals(
        List.of(), handle(server, new JsonRpcNotification("extension/notify", JSON.objectNode())));
    assertFalse(extensionInvoked.get());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("protocolExceptions")
  void convertsDeliberateProtocolExceptionsToCorrelatedErrors(
      String description, McpProtocolException exception) {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("protocol-failure", "1", McpEmptyContext.class)
            .syncMethod(
                "extension/failure",
                (applicationContext, request, handlerContext) -> {
                  throw exception;
                })
            .build();

    JsonRpcErrorResponse response =
        error(handle(server, request(17, "extension/failure", JSON.objectNode())));

    assertEquals(JSON.numberNode(17), response.id());
    assertEquals(exception.code(), response.code());
    assertEquals(exception.getMessage(), response.message());
  }

  private static Stream<Arguments> externalSchemaReferences() {
    return Stream.of(
        Arguments.of("HTTP reference", "$ref", "https://example.com/schema", null),
        Arguments.of("protocol-relative reference", "$ref", "//example.com/schema", null),
        Arguments.of("relative reference", "$ref", "schema.json", null),
        Arguments.of(
            "relative reference with a remote base",
            "$ref",
            "schema.json",
            "https://example.com/root"),
        Arguments.of("FTP reference", "$ref", "ftp://example.com/schema", null),
        Arguments.of("file reference", "$ref", "file:///tmp/schema.json", null),
        Arguments.of(
            "JAR reference", "$ref", "jar:https://example.com/schemas.jar!/schema.json", null),
        Arguments.of("query-only reference", "$ref", "?schema", null),
        Arguments.of("URN reference", "$ref", "urn:example:schema", null),
        Arguments.of(
            "external dynamic reference", "$dynamicRef", "https://example.com/schema#node", null));
  }

  private static Stream<Arguments> typedRequests() {
    ObjectNode toolCall = JSON.objectNode().put("name", "echo");
    ObjectNode resourceRead = JSON.objectNode().put("uri", "file:///value");
    ObjectNode promptGet = JSON.objectNode().put("name", "hello");
    ObjectNode subscription = JSON.objectNode();
    subscription.putObject("notifications");
    ObjectNode completion = JSON.objectNode();
    completion.putObject("argument").put("name", "value").put("value", "v");
    completion.putObject("ref").put("type", "ref/prompt").put("name", "hello");
    return Stream.of(
        Arguments.of("server/discover", JSON.objectNode(), McpDiscoverRequest.class),
        Arguments.of("tools/list", JSON.objectNode(), McpListToolsRequest.class),
        Arguments.of("tools/call", toolCall, McpCallToolRequest.class),
        Arguments.of("resources/list", JSON.objectNode(), McpListResourcesRequest.class),
        Arguments.of(
            "resources/templates/list", JSON.objectNode(), McpListResourceTemplatesRequest.class),
        Arguments.of("resources/read", resourceRead, McpReadResourceRequest.class),
        Arguments.of("subscriptions/listen", subscription, McpSubscriptionsListenRequest.class),
        Arguments.of("prompts/list", JSON.objectNode(), McpListPromptsRequest.class),
        Arguments.of("prompts/get", promptGet, McpGetPromptRequest.class),
        Arguments.of("completion/complete", completion, McpCompleteRequest.class));
  }

  private static List<String> clientToServerMethods() {
    return List.of(
        McpDiscoverRequest.METHOD,
        McpListToolsRequest.METHOD,
        McpCallToolRequest.METHOD,
        McpListResourcesRequest.METHOD,
        McpListResourceTemplatesRequest.METHOD,
        McpReadResourceRequest.METHOD,
        McpSubscriptionsListenRequest.METHOD,
        McpListPromptsRequest.METHOD,
        McpGetPromptRequest.METHOD,
        McpCompleteRequest.METHOD);
  }

  private static Stream<Arguments> protocolExceptions() {
    Optional<com.fasterxml.jackson.databind.JsonNode> noData = Optional.empty();
    var capabilities =
        new io.github.suppierk.mcp.protocol.McpClientCapabilities(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
    return Stream.of(
        Arguments.of("parse", new McpParseException("parse", noData)),
        Arguments.of("invalid request", new McpInvalidRequestException("request", noData)),
        Arguments.of("missing method", new McpMethodNotFoundException("method", noData)),
        Arguments.of("invalid parameters", new McpInvalidParamsException("params", noData)),
        Arguments.of("internal", new McpInternalException("internal", noData)),
        Arguments.of("header mismatch", new McpHeaderMismatchException("header", noData)),
        Arguments.of(
            "missing capability",
            new McpMissingRequiredClientCapabilityException("capability", capabilities)),
        Arguments.of(
            "unsupported revision",
            new McpUnsupportedProtocolVersionException(
                "version", "1900-01-01", List.of(McpProtocol.REVISION))));
  }

  private static McpServerKit<McpEmptyContext> serverWithToolSchema(ObjectNode schema) {
    return McpServerKit.builder("schema", "1", McpEmptyContext.class)
        .syncTool(
            new McpTool("reference", schema),
            (applicationContext, request, handlerContext) -> toolResult("x"))
        .build();
  }

  private static McpCallToolResult toolResult(String value) {
    return new McpCallToolResult(List.of(new McpTextContent(value)));
  }

  private static McpReadResourceResult resourceResult(URI uri, String value) {
    return new McpReadResourceResult(List.of(new McpTextResourceContents(uri, value)));
  }

  private static McpGetPromptResult promptResult(String value) {
    return new McpGetPromptResult(
        List.of(new McpPromptMessage(new McpTextContent(value), McpRole.USER)));
  }

  private static JsonRpcResultResponse response(JsonRpcRequest request, String value) {
    return new JsonRpcResultResponse(request.id(), JSON.objectNode().put("value", value));
  }

  private static JsonRpcRequest request(int id, String method, ObjectNode values) {
    ObjectNode params = values.deepCopy();
    metadata(params);
    return new JsonRpcRequest(JSON.numberNode(id), method, params);
  }

  private static ObjectNode metadata(ObjectNode params) {
    ObjectNode metadata =
        params.get("_meta") instanceof ObjectNode supplied ? supplied : params.putObject("_meta");
    metadata.put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION);
    metadata.putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    return metadata;
  }

  private static List<JsonRpcMessage> handle(
      McpServerKit<McpEmptyContext> server, JsonRpcMessage message) {
    RecordingSubscriber subscriber = new RecordingSubscriber();
    server.handle(McpEmptyContext.INSTANCE, message).subscribe(subscriber);
    subscriber.completion.orTimeout(5, TimeUnit.SECONDS).join();
    return List.copyOf(subscriber.messages);
  }

  private static <C> List<JsonRpcMessage> handle(
      McpServerKit<C> server, C applicationContext, JsonRpcMessage message) {
    RecordingSubscriber subscriber = new RecordingSubscriber();
    server.handle(applicationContext, message).subscribe(subscriber);
    subscriber.completion.orTimeout(5, TimeUnit.SECONDS).join();
    return List.copyOf(subscriber.messages);
  }

  private static JsonRpcResultResponse success(
      McpServerKit<?> server, List<JsonRpcMessage> messages) {
    JsonRpcMessage message = messages.get(messages.size() - 1);
    return assertInstanceOf(JsonRpcResultResponse.class, server.decode(server.encode(message)));
  }

  private static JsonRpcErrorResponse error(List<JsonRpcMessage> messages) {
    return assertInstanceOf(JsonRpcErrorResponse.class, messages.get(messages.size() - 1));
  }

  private static final class RecordingSubscriber implements Flow.Subscriber<JsonRpcMessage> {
    private final List<JsonRpcMessage> messages = new ArrayList<>();
    private final CompletableFuture<Void> completion = new CompletableFuture<>();
    private final CompletableFuture<Throwable> failure = new CompletableFuture<>();
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription value) {
      subscription = value;
      value.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(JsonRpcMessage item) {
      messages.add(item);
    }

    @Override
    public void onError(Throwable throwable) {
      failure.complete(throwable);
      completion.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
      completion.complete(null);
    }
  }

  private record ApplicationContext(String value) {}
}
