package io.github.suppierk.mcp.server;

import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonIntegerSchema;
import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonStringSchema;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.JsonTestValues;
import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcNotification;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpCallToolResultResponse;
import io.github.suppierk.mcp.protocol.McpCancelledNotificationParams;
import io.github.suppierk.mcp.protocol.McpClientCapabilities;
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
import io.github.suppierk.mcp.protocol.McpToolAnnotations;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class McpServerKitTest {
  private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

  @Test
  void validatesComposedInputsWithoutInjectingSchemaDefaults() {
    var calls = new AtomicInteger();
    var server =
        McpServerKit.mcpServerKit("schema", "1", McpEmptyContext.class)
            .syncTool(
                tool ->
                    tool.name("find-user")
                        .inputSchema(
                            schema ->
                                schema
                                    .required("id", mcpJsonStringSchema(id -> id.minLength(1)))
                                    .optional(
                                        "limit",
                                        mcpJsonIntegerSchema(
                                            limit ->
                                                limit.minimum(1).maximum(100).defaultValue(10))))
                        .handler(
                            (context, parameters, control) -> {
                              calls.incrementAndGet();
                              assertFalse(
                                  parameters.arguments().orElseThrow().containsKey("limit"));
                              return new McpCallToolResult(List.of(new McpTextContent("found")));
                            }))
            .build();
    var valid = JSON.objectNode().put("name", "find-user");
    valid.putObject("arguments").put("id", "alice");
    success(server, handle(server, request(1, "tools/call", valid)));
    for (ObjectNode arguments :
        List.of(
            JSON.objectNode().put("id", ""),
            JSON.objectNode().put("id", "alice").put("limit", 0),
            JSON.objectNode().put("id", "alice").put("unexpected", true))) {
      var invalid = JSON.objectNode().put("name", "find-user");
      invalid.set("arguments", arguments);
      assertEquals(
          McpInvalidParamsException.CODE,
          error(handle(server, request(2, "tools/call", invalid))).code());
    }
    assertEquals(1, calls.get());
  }

  @Test
  void validatesTheFinalToolNameAndKeepsFailedCallbacksOutOfTheRegistry() {
    var builder = McpServerKit.mcpServerKit("atomic", "1", McpEmptyContext.class);
    var failure = new IllegalStateException("callback failed");
    assertSame(
        failure,
        assertThrows(
            IllegalStateException.class,
            () ->
                builder.syncTool(
                    tool -> {
                      tool.name("hello")
                          .handler((context, parameters, control) -> toolResult("discarded"));
                      throw failure;
                    })));
    assertThrows(NullPointerException.class, () -> builder.syncTool(tool -> tool.name("hello")));
    assertThrows(
        NullPointerException.class,
        () ->
            builder.syncTool(
                tool ->
                    tool.handler((context, parameters, control) -> toolResult("missing name"))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            builder.syncTool(
                tool ->
                    tool.name("hello")
                        .inputSchema(Map.of("type", "string"))
                        .handler((context, parameters, control) -> toolResult("invalid schema"))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            builder.syncTool(
                tool ->
                    tool.name(" ")
                        .handler((context, parameters, control) -> toolResult("blank name"))));
    builder.syncTool(
        tool ->
            tool.name(" ")
                .handler((context, parameters, control) -> toolResult("kept"))
                .name("hello"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            builder.asyncTool(
                tool ->
                    tool.name("hello")
                        .handler(
                            (context, parameters, control) ->
                                CompletableFuture.completedFuture(toolResult("duplicate")))));
    var server = builder.build();
    var listed =
        JsonTestValues.json(
                success(server, handle(server, request(1, "tools/list", JSON.objectNode())))
                    .result())
            .path("tools");
    assertEquals(1, listed.size());
    assertEquals("hello", listed.path(0).path("name").textValue());
    assertEquals(
        "kept",
        JsonTestValues.json(
                success(
                        server,
                        handle(
                            server,
                            request(2, "tools/call", JSON.objectNode().put("name", "hello"))))
                    .result())
            .path("content")
            .path(0)
            .path("text")
            .textValue());
  }

  @Test
  void registersFunctionalToolWithClosedDefaultInputAndApplicationContext() {
    var server =
        McpServerKit.mcpServerKit("functional", "1", McpEmptyContext.class)
            .syncTool(
                tool ->
                    tool.name("hello")
                        .handler(
                            (context, parameters, control) -> {
                              assertSame(McpEmptyContext.INSTANCE, context);
                              return new McpCallToolResult(
                                  List.of(new McpTextContent("Hello, World!")));
                            }))
            .build();

    assertEquals(
        Map.of("type", "object", "additionalProperties", false),
        server.toolInputSchema("hello").orElseThrow());
    var result =
        success(
            server,
            handle(server, request(42, "tools/call", JSON.objectNode().put("name", "hello"))));
    assertEquals(
        "Hello, World!",
        JsonTestValues.json(result.result()).path("content").path(0).path("text").textValue());
    var params = JSON.objectNode().put("name", "hello");
    params.putObject("arguments").put("unexpected", true);
    assertInstanceOf(
        JsonRpcErrorResponse.class, handle(server, request(43, "tools/call", params)).get(0));
  }

  @Test
  void snapshotsAsyncToolMetadataAfterTheCallbackReturns() {
    var schema = new LinkedHashMap<String, Object>();
    schema.put("type", "object");
    schema.put("additionalProperties", false);
    var retained =
        new AtomicReference<
            McpServerKit.ToolBuilder<
                String, CompletableFuture<? extends McpCallToolResultResponse.Result>>>();
    var callbacks = new AtomicInteger();
    var builder = McpServerKit.mcpServerKit("functional", "1", String.class);
    builder.asyncTool(
        tool -> {
          callbacks.incrementAndGet();
          retained.set(tool);
          tool.name("who")
              .inputSchema(schema)
              .outputSchema(Map.of("type", "object"))
              .description("Current user")
              .title("Who")
              .meta(new McpMetaObject(Map.of("owner", "test")))
              .annotations(McpToolAnnotations.mcpToolAnnotations().readOnlyHint(true).build())
              .icons(List.of(new McpIcon(URI.create("https://example.com/icon.png"))))
              .handler(
                  (context, parameters, control) ->
                      CompletableFuture.completedFuture(
                          new McpCallToolResult(
                              List.of(new McpTextContent(context)), Map.of("user", context))));
        });
    schema.put("type", "string");
    retained.get().name("changed").description("changed");
    var server = builder.build();
    assertEquals(1, callbacks.get());
    var listed =
        JsonTestValues.json(
                success(
                        server,
                        handle(server, "alice", request(1, "tools/list", JSON.objectNode())))
                    .result())
            .path("tools")
            .path(0);
    assertEquals("who", listed.path("name").textValue());
    assertEquals("Current user", listed.path("description").textValue());
    assertEquals("Who", listed.path("title").textValue());
    assertEquals("test", listed.path("_meta").path("owner").textValue());
    assertTrue(listed.path("annotations").path("readOnlyHint").booleanValue());
    assertEquals(
        "https://example.com/icon.png", listed.path("icons").path(0).path("src").textValue());
    assertEquals("object", listed.path("inputSchema").path("type").textValue());
    assertEquals("object", listed.path("outputSchema").path("type").textValue());
    var called =
        success(
            server,
            handle(
                server, "alice", request(2, "tools/call", JSON.objectNode().put("name", "who"))));
    assertEquals(
        "alice",
        JsonTestValues.json(called.result()).path("structuredContent").path("user").textValue());
  }

  @Test
  void preservesDomainDenialsForToolsWithAnOutputSchema() {
    var output = JSON.objectNode().put("type", "object");
    output.putArray("required").add("answer");
    output.putObject("properties").putObject("answer").put("type", "string");
    var denial =
        McpCallToolResult.mcpCallToolResult()
            .resultType("complete")
            .content(List.of(new McpTextContent("Not permitted")))
            .isError(true)
            .build();
    var server =
        McpServerKit.mcpServerKit("denial", "1", McpEmptyContext.class)
            .syncTool(
                tool ->
                    tool.name("answer")
                        .outputSchema(JsonTestValues.object(output))
                        .handler((applicationContext, parameters, handlerContext) -> denial))
            .build();

    var result =
        success(
            server,
            handle(server, request(42, "tools/call", JSON.objectNode().put("name", "answer"))));

    assertTrue(JsonTestValues.json(result.result()).path("isError").booleanValue());
    assertEquals(
        "Not permitted",
        JsonTestValues.json(result.result()).path("content").path(0).path("text").textValue());
  }

  @Test
  void acceptsAndPreservesRequestExtensionMetadata() throws Exception {
    var server = McpServerKit.mcpServerKit("metadata", "1", McpEmptyContext.class).build();
    var parameters = JSON.objectNode();
    metadata(parameters).put("com.example/trace", "trace-42").put("progressToken", "progress-42");
    var original = request(42, "tools/list", parameters);

    var decoded =
        assertInstanceOf(McpListToolsRequest.class, server.decode(server.encode(original)));

    var mapper = new ObjectMapper();
    assertEquals(mapper.readTree(server.encode(original)), mapper.readTree(server.encode(decoded)));
  }

  @Test
  void preservesRequestIdWhenToolParametersAreMalformed() {
    var server = McpServerKit.mcpServerKit("decode", "1", McpEmptyContext.class).build();
    var parameters = JSON.objectNode().put("name", "hello");
    parameters.putArray("arguments");

    var decoded =
        assertInstanceOf(
            JsonRpcErrorResponse.class,
            server.decode(server.encode(request(42, "tools/call", parameters))));
    var failure = error(handle(server, decoded));

    assertEquals(McpInvalidParamsException.CODE, failure.code());
    assertEquals(42, failure.id());
  }

  @Test
  void rejectsTrailingJsonAfterARequest() {
    var server = McpServerKit.mcpServerKit("decode", "1", McpEmptyContext.class).build();
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
    var builder = McpServerKit.mcpServerKit("context", "1", String.class);

    McpServerKit<String> serverKit = builder.build();

    assertDoesNotThrow(serverKit::close);
    assertFalse(
        Arrays.stream(builder.getClass().getDeclaredFields())
            .anyMatch(field -> field.getType().equals(Class.class)));
    assertThrows(
        NullPointerException.class,
        () -> McpServerKit.mcpServerKit("context", "1", (Class<String>) null));
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
        McpServerKit.mcpServerKit("context", "1", ApplicationContext.class)
            .syncTool(
                registration ->
                    registration
                        .name("context")
                        .inputSchema(JsonTestValues.object(JSON.objectNode().put("type", "object")))
                        .handler(
                            (applicationContext, parameters, handlerContext) -> {
                              seenContext.set(applicationContext);
                              seenHandlerContext.set(handlerContext);
                              return toolResult(applicationContext.value());
                            }))
            .build();
    var applicationContext = new ApplicationContext("exact");
    var call = JSON.objectNode().put("name", "context");

    assertEquals(
        "exact",
        JsonTestValues.json(
                success(
                        serverKit,
                        handle(serverKit, applicationContext, request(1, "tools/call", call)))
                    .result())
            .path("content")
            .path(0)
            .path("text")
            .textValue());
    assertSame(applicationContext, seenContext.get());
    assertInstanceOf(McpHandlerContext.class, seenHandlerContext.get());
    assertEquals(
        McpProtocol.REVISION,
        JsonTestValues.json(
                success(
                        serverKit,
                        handle(
                            serverKit,
                            new ApplicationContext("built-in"),
                            request(2, "server/discover", JSON.objectNode())))
                    .result())
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
          McpServerKit.mcpServerKit("concurrent-context", "1", ApplicationContext.class)
              .asyncMethod(
                  "context",
                  (applicationContext, call, handlerContext) ->
                      CompletableFuture.supplyAsync(
                          () -> {
                            (JsonTestValues.json(call.id()).intValue() == 1
                                    ? firstSeen
                                    : secondSeen)
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
        McpServerKit.mcpServerKit("codec", "1", McpEmptyContext.class).build();
    JsonRpcRequest request = request(1, "server/discover", JSON.objectNode());
    JsonRpcNotification notification =
        new JsonRpcNotification("notifications/ignored", JsonTestValues.object(Map.of()));
    JsonRpcResultResponse success =
        new JsonRpcResultResponse(
            JsonTestValues.value(JSON.numberNode(1)),
            JsonTestValues.value(JSON.objectNode().put("ok", true)));
    JsonRpcErrorResponse failure =
        new JsonRpcErrorResponse(
            JsonTestValues.value(JSON.numberNode(1)),
            -1,
            "failed",
            JsonTestValues.optionalValue(Optional.of(JSON.objectNode().put("why", "x"))));

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

  @ParameterizedTest
  @ValueSource(
      strings = {"2147483648", "-2147483649", "9223372036854775808", "-9223372036854775809"})
  void rejectsOutOfRangeErrorCodesWithoutThrowing(String code) {
    try (var server = McpServerKit.mcpServerKit("codec", "1", McpEmptyContext.class).build()) {
      String json =
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{\"code\":" + code + ",\"message\":\"failed\"}}";

      var failure =
          assertInstanceOf(
              JsonRpcErrorResponse.class, server.decode(json.getBytes(StandardCharsets.UTF_8)));

      assertEquals(-32600, failure.code());
      assertEquals(failure, error(handle(server, failure)));
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {-2147483648, -32600, 0, 2147483647})
  void preservesSupportedErrorCodes(int code) {
    try (var server = McpServerKit.mcpServerKit("codec", "1", McpEmptyContext.class).build()) {
      var original = new JsonRpcErrorResponse(1, code, "failed", Optional.empty());

      assertEquals(original, server.decode(server.encode(original)));
    }
  }

  @Test
  void rejectsInvalidMessageShapesAndUnexpectedInboundMessages() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.mcpServerKit("messages", "1", McpEmptyContext.class).build();
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
        error(
                handle(
                    server,
                    new JsonRpcResultResponse(
                        JsonTestValues.value(JSON.numberNode(1)),
                        JsonTestValues.value(JSON.objectNode()))))
            .code());
    assertEquals(
        List.of(),
        handle(
            server,
            new JsonRpcNotification("notifications/ignored", JsonTestValues.object(Map.of()))));
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
        McpServerKit.mcpServerKit("notifications", "1", McpEmptyContext.class).build();
    var cancellation =
        new McpClientNotification(
            new McpCancelledNotificationParams(
                Optional.empty(), Optional.of("done"), JsonTestValues.value(JSON.numberNode(42))));

    McpClientNotification decoded =
        assertInstanceOf(McpClientNotification.class, server.decode(server.encode(cancellation)));

    assertEquals(cancellation.params(), decoded.params());
    assertEquals(List.of(), handle(server, cancellation));
    assertEquals(
        List.of(),
        handle(
            server,
            new JsonRpcNotification("notifications/unknown", JsonTestValues.object(Map.of()))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("typedRequests")
  void decodesBuiltInRequestsToExactTypes(
      String method, ObjectNode values, Class<? extends JsonRpcMessage> expectedType) {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.mcpServerKit("typed", "1", McpEmptyContext.class).build();

    assertEquals(expectedType, server.decode(server.encode(request(1, method, values))).getClass());
  }

  @Test
  void discoversIdentityAndRejectsInvalidMetadata() {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.mcpServerKit("server", "1", McpEmptyContext.class)
            .title("Title")
            .description("Description")
            .websiteUrl(URI.create("https://example.com"))
            .instructions("Use it.")
            .icon(new McpIcon(URI.create("data:image/png;base64,AA==")))
            .build();
    JsonRpcResultResponse response =
        success(server, handle(server, request(1, "server/discover", JSON.objectNode())));
    assertEquals(
        McpProtocol.REVISION,
        JsonTestValues.json(response.result()).path("supportedVersions").path(0).textValue());
    assertEquals(
        "server",
        JsonTestValues.json(response.result())
            .path("_meta")
            .path("io.modelcontextprotocol/serverInfo")
            .path("name")
            .textValue());
    assertEquals(
        "Use it.", JsonTestValues.json(response.result()).path("instructions").textValue());

    JsonRpcRequest absent =
        new JsonRpcRequest(
            JsonTestValues.value(JSON.numberNode(2)),
            "server/discover",
            JsonTestValues.object(JSON.objectNode()));
    assertEquals(McpInvalidParamsException.CODE, error(handle(server, absent)).code());
    ObjectNode unsupported = JSON.objectNode();
    metadata(unsupported).put(McpProtocol.PROTOCOL_VERSION_KEY, "1900-01-01");
    assertEquals(
        McpUnsupportedProtocolVersionException.CODE,
        error(
                handle(
                    server,
                    new JsonRpcRequest(
                        JsonTestValues.value(JSON.numberNode(3)),
                        "server/discover",
                        JsonTestValues.object(unsupported))))
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
    McpServerKit<McpEmptyContext> server =
        McpServerKit.mcpServerKit("tools", "1", McpEmptyContext.class)
            .syncTool(
                tool ->
                    tool.name("echo")
                        .inputSchema(JsonTestValues.object(input))
                        .outputSchema(JsonTestValues.object(output))
                        .meta(new McpMetaObject(Map.of("owner", "test")))
                        .annotations(
                            McpToolAnnotations.mcpToolAnnotations().readOnlyHint(true).build())
                        .description("Returns text.")
                        .title("Echo")
                        .handler(
                            (applicationContext, request, handlerContext) -> {
                              String value =
                                  JsonTestValues.json(request.arguments().orElseThrow())
                                      .path("value")
                                      .textValue();
                              return new McpCallToolResult(
                                  List.of(new McpTextContent(value)),
                                  JsonTestValues.value(JSON.objectNode().put("echo", value)));
                            }))
            .build();

    JsonRpcResultResponse listed =
        success(server, handle(server, request(1, "tools/list", JSON.objectNode())));
    assertEquals(
        "echo",
        JsonTestValues.json(listed.result()).path("tools").path(0).path("name").textValue());
    ObjectNode call = JSON.objectNode().put("name", "echo");
    call.set("arguments", JSON.objectNode().put("value", "hello"));
    assertEquals(
        "hello",
        JsonTestValues.json(
                success(server, handle(server, request(2, "tools/call", call))).result())
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
        McpServerKit.mcpServerKit("output", "1", McpEmptyContext.class)
            .syncTool(
                tool ->
                    tool.name("echo")
                        .inputSchema(JsonTestValues.object(input))
                        .outputSchema(JsonTestValues.object(output))
                        .handler(
                            (applicationContext, request, handlerContext) ->
                                new McpCallToolResult(
                                    List.of(new McpTextContent("missing output")))))
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
        McpServerKit.mcpServerKit("features", "1", McpEmptyContext.class)
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
        JsonTestValues.json(
                success(server, handle(server, request(1, "resources/list", JSON.objectNode())))
                    .result())
            .path("resources")
            .path(0)
            .path("name")
            .textValue());
    assertEquals(
        "files",
        JsonTestValues.json(
                success(
                        server,
                        handle(server, request(2, "resources/templates/list", JSON.objectNode())))
                    .result())
            .path("resourceTemplates")
            .path(0)
            .path("name")
            .textValue());
    assertEquals(
        "fixed",
        JsonTestValues.json(
                success(
                        server,
                        handle(
                            server,
                            request(
                                3,
                                "resources/read",
                                JSON.objectNode().put("uri", "file:///fixed"))))
                    .result())
            .path("contents")
            .path(0)
            .path("text")
            .textValue());
    assertEquals(
        "template",
        JsonTestValues.json(
                success(
                        server,
                        handle(
                            server,
                            request(
                                4,
                                "resources/read",
                                JSON.objectNode().put("uri", "file:///other"))))
                    .result())
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
        JsonTestValues.json(
                success(server, handle(server, request(6, "prompts/list", JSON.objectNode())))
                    .result())
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
        JsonTestValues.json(
                success(server, handle(server, request(8, "prompts/get", promptRequest))).result())
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
          McpServerKit.mcpServerKit("custom", "1", McpEmptyContext.class)
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
          JsonTestValues.json(
                  success(server, handle(server, request(1, "example/run", JSON.objectNode())))
                      .result())
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
        McpServerKit.mcpServerKit("failure", "1", McpEmptyContext.class)
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
        McpServerKit.mcpServerKit("subscriptions", "1", McpEmptyContext.class)
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
        McpServerKit.mcpServerKit("duplicates", "1", McpEmptyContext.class);
    builder.syncTool(
        registration ->
            registration
                .name("tool")
                .inputSchema(JsonTestValues.object(schema))
                .handler((applicationContext, request, handlerContext) -> toolResult("x")));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            builder.syncTool(
                registration ->
                    registration
                        .name("tool")
                        .inputSchema(JsonTestValues.object(schema))
                        .handler(
                            (applicationContext, request, handlerContext) -> toolResult("x"))));
    for (String method : clientToServerMethods()) {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              McpServerKit.mcpServerKit("x", "1", McpEmptyContext.class)
                  .syncMethod(
                      method,
                      (applicationContext, request, handlerContext) -> response(request, "x"))
                  .build());
    }
    assertThrows(
        IllegalArgumentException.class,
        () ->
            builder.syncTool(
                tool ->
                    tool.name("bad")
                        .inputSchema(Map.of())
                        .handler((context, parameters, control) -> toolResult("bad"))));
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

    var supplied = server.toolInputSchema("reference").orElseThrow();
    assertThrows(UnsupportedOperationException.class, supplied::clear);

    assertFalse(
        JsonTestValues.json(server.toolInputSchema("reference").orElseThrow()).has("changed"));
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
            McpServerKit.mcpServerKit("schema", "1", McpEmptyContext.class)
                .syncTool(
                    registration ->
                        registration
                            .name("external")
                            .inputSchema(JsonTestValues.object(schema))
                            .handler(
                                (applicationContext, request, handlerContext) -> toolResult("x")))
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
        McpServerKit.mcpServerKit("extension-id", "1", McpEmptyContext.class)
            .syncMethod(
                "extension/run",
                (applicationContext, request, handlerContext) ->
                    new JsonRpcResultResponse(
                        JsonTestValues.value(JSON.numberNode(999)),
                        JsonTestValues.value(JSON.objectNode())))
            .build();

    JsonRpcErrorResponse response =
        error(handle(server, request(42, "extension/run", JSON.objectNode())));

    assertEquals(42, response.id());
    assertEquals(-32603, response.code());
    assertEquals("Internal error", response.message());

    McpServerKit<McpEmptyContext> customErrorServer =
        McpServerKit.mcpServerKit("custom-error", "1", McpEmptyContext.class)
            .syncMethod(
                "extension/error",
                (applicationContext, call, handlerContext) ->
                    new JsonRpcErrorResponse(
                        JsonTestValues.value(call.id()),
                        -32099,
                        "Extension error",
                        JsonTestValues.optionalValue(Optional.empty())))
            .build();
    JsonRpcErrorResponse custom =
        error(handle(customErrorServer, request(43, "extension/error", JSON.objectNode())));
    assertEquals(43, custom.id());
    assertEquals(-32099, custom.code());
  }

  @Test
  void dispatchesCompletionAndDoesNotDispatchExtensionNotifications() {
    AtomicBoolean extensionInvoked = new AtomicBoolean();
    McpServerKit<McpEmptyContext> server =
        McpServerKit.mcpServerKit("remaining-methods", "1", McpEmptyContext.class)
            .syncCompletion(
                (applicationContext, parameters, handlerContext) -> {
                  ObjectNode value = JSON.objectNode();
                  value.putArray("values").add("done");
                  return new McpCompleteResult(
                      Optional.empty(), JsonTestValues.object(value), "complete");
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
        JsonTestValues.json(
                success(server, handle(server, request(44, McpCompleteRequest.METHOD, completion)))
                    .result())
            .path("completion")
            .path("values")
            .path(0)
            .textValue());
    assertEquals(
        List.of(),
        handle(
            server, new JsonRpcNotification("extension/notify", JsonTestValues.object(Map.of()))));
    assertFalse(extensionInvoked.get());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("protocolExceptions")
  void convertsDeliberateProtocolExceptionsToCorrelatedErrors(
      String description, McpProtocolException exception) {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.mcpServerKit("protocol-failure", "1", McpEmptyContext.class)
            .syncMethod(
                "extension/failure",
                (applicationContext, request, handlerContext) -> {
                  throw exception;
                })
            .build();

    JsonRpcErrorResponse response =
        error(handle(server, request(17, "extension/failure", JSON.objectNode())));

    assertEquals(17, response.id());
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
    Optional<Object> noData = Optional.empty();
    var capabilities =
        new McpClientCapabilities(
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
    return McpServerKit.mcpServerKit("schema", "1", McpEmptyContext.class)
        .syncTool(
            registration ->
                registration
                    .name("reference")
                    .inputSchema(JsonTestValues.object(schema))
                    .handler((applicationContext, request, handlerContext) -> toolResult("x")))
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
    return new JsonRpcResultResponse(
        JsonTestValues.value(request.id()),
        JsonTestValues.value(JSON.objectNode().put("value", value)));
  }

  private static JsonRpcRequest request(int id, String method, ObjectNode values) {
    ObjectNode params = values.deepCopy();
    metadata(params);
    return new JsonRpcRequest(
        JsonTestValues.value(JSON.numberNode(id)), method, JsonTestValues.object(params));
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
