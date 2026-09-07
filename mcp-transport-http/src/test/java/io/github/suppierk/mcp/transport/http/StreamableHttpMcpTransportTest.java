package io.github.suppierk.mcp.transport.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.protocol.McpReadResourceResult;
import io.github.suppierk.mcp.protocol.McpResource;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotification;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotificationParams;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTextResourceContents;
import io.github.suppierk.mcp.protocol.McpTool;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpHandlerContext;
import io.github.suppierk.mcp.server.McpServerKit;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StreamableHttpMcpTransportTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void independentHttpRequestsCanReuseTheSameJsonRpcId() throws Exception {
    var firstResult = new CompletableFuture<JsonRpcResultResponse>();
    var secondResult = new CompletableFuture<JsonRpcResultResponse>();
    var firstCall = new AtomicBoolean(true);
    try (var server =
        McpServerKit.builder("request-ids", "1", McpEmptyContext.class)
            .asyncMethod(
                "slow",
                (applicationContext, call, handlerContext) ->
                    firstCall.getAndSet(false) ? firstResult : secondResult)
            .build()) {
      var transport = new StreamableHttpMcpTransport<>(server);
      var request = request("slow", "application/json", Map.of());
      var first = transport.handle(McpEmptyContext.INSTANCE, request).toCompletableFuture();
      var second = transport.handle(McpEmptyContext.INSTANCE, request).toCompletableFuture();

      assertFalse(second.isDone(), "The second independent request must reach its own handler");
      first.cancel(false);
      assertTrue(firstResult.isCancelled());
      assertFalse(secondResult.isCancelled());
      secondResult.complete(new JsonRpcResultResponse(1, Map.of("value", "second")));
      var response = assertInstanceOf(HttpJsonResponse.class, second.get(5, TimeUnit.SECONDS));
      assertEquals(1, mapper.readTree(response.body()).path("id").intValue());
      assertEquals(
          "second", mapper.readTree(response.body()).path("result").path("value").textValue());
    }
  }

  @Test
  void cancellingAFiniteHttpResponseCancelsItsApplicationHandler() throws Exception {
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var context = new AtomicReference<McpHandlerContext>();
    try (var server =
        McpServerKit.builder("cancellation", "1", McpEmptyContext.class)
            .asyncMethod(
                "slow",
                (applicationContext, call, handlerContext) -> {
                  context.set(handlerContext);
                  return applicationFuture;
                })
            .build()) {
      var response =
          new StreamableHttpMcpTransport<>(server)
              .handle(McpEmptyContext.INSTANCE, request("slow", "application/json", Map.of()))
              .toCompletableFuture();

      assertTrue(response.cancel(false));
      assertTrue(context.get().isCancelled());
      assertTrue(applicationFuture.isCancelled());
    }
  }

  @Test
  void suppliesAcceptedResponseMetadata() {
    HttpAcceptedResponse response = new HttpAcceptedResponse();

    assertEquals(202, response.status());
    assertEquals(Map.of(), response.headers());
  }

  @Test
  void returnsJsonForAValidRequest() throws Exception {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("http-test", "1", McpEmptyContext.class).build();
    StreamableHttpMcpTransport<McpEmptyContext> transport =
        new StreamableHttpMcpTransport<>(server);

    HttpJsonResponse response =
        assertInstanceOf(
            HttpJsonResponse.class,
            transport
                .handle(
                    McpEmptyContext.INSTANCE,
                    request("server/discover", "application/json", Map.of()))
                .toCompletableFuture()
                .join());

    assertEquals(200, response.status());
    assertEquals("application/json", response.headers().get("Content-Type"));
    JsonNode json = mapper.readTree(response.body());
    assertEquals(
        McpProtocol.REVISION, json.path("result").path("supportedVersions").path(0).textValue());
    assertInstanceOf(
        HttpJsonResponse.class,
        transport
            .handle(
                McpEmptyContext.INSTANCE,
                request("server/discover", "application/json, text/event-stream", Map.of()))
            .toCompletableFuture()
            .join());
  }

  @Test
  void selectsEventStreamsFromRequestSemanticsRatherThanAccept() throws Exception {
    var server =
        McpServerKit.builder("selection", "1", McpEmptyContext.class)
            .syncMethod(
                "work",
                (applicationContext, call, handlerContext) ->
                    new JsonRpcResultResponse(call.id(), Map.of()))
            .build();
    var transport = new StreamableHttpMcpTransport<>(server);

    HttpMcpResponse progressResponse =
        transport
            .handle(
                McpEmptyContext.INSTANCE,
                withProgressToken(request("work", "application/json", Map.of())))
            .toCompletableFuture()
            .join();
    HttpMcpResponse finiteResponse =
        transport
            .handle(McpEmptyContext.INSTANCE, request("work", "text/event-stream", Map.of()))
            .toCompletableFuture()
            .join();

    var eventStream = assertInstanceOf(HttpEventStreamResponse.class, progressResponse);
    assertEquals("no", eventStream.headers().get("X-Accel-Buffering"));
    assertInstanceOf(HttpJsonResponse.class, finiteResponse);
    eventStream.close();
  }

  @Test
  void validatesHttpEnvelopeBeforeDispatch() throws Exception {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("http-test", "1", McpEmptyContext.class).build();
    StreamableHttpMcpTransport<McpEmptyContext> transport =
        new StreamableHttpMcpTransport<>(server);
    HttpMcpRequest valid = request("server/discover", "application/json", Map.of());

    assertEquals(
        405,
        response(transport, new HttpMcpRequest("GET", valid.headers(), valid.body())).status());
    assertEquals(
        415, response(transport, withHeader(valid, "Content-Type", "text/plain")).status());
    assertEquals(
        415, response(transport, withHeader(valid, "Content-Type", "application/jsonx")).status());
    assertEquals(406, response(transport, withHeader(valid, "Accept", "text/plain")).status());
    assertEquals(
        403, response(transport, withHeader(valid, "Origin", "https://other.example")).status());
    assertEquals(400, response(transport, withoutHeader(valid, "Mcp-Method")).status());
    assertEquals(400, response(transport, withHeader(valid, "Mcp-Method", "other")).status());
    assertEquals(400, response(transport, withoutMetadata(valid)).status());
    assertEquals(
        404, response(transport, request("missing", "application/json", Map.of())).status());
  }

  @Test
  void returnsProtocolErrorForAnOutOfRangeErrorCode() throws Exception {
    try (var server = McpServerKit.builder("http-test", "1", McpEmptyContext.class).build()) {
      var transport = new StreamableHttpMcpTransport<>(server);
      var envelope = request("server/discover", "application/json", Map.of());
      String body =
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{\"code\":2147483648,\"message\":\"failed\"}}";

      var failure =
          response(
              transport,
              new HttpMcpRequest(
                  "POST", envelope.headers(), body.getBytes(StandardCharsets.UTF_8)));

      assertEquals(400, failure.status());
      assertEquals(-32600, mapper.readTree(failure.body()).path("error").path("code").intValue());
    }
  }

  @Test
  void rejectsStdioCancellationNotificationsWithoutCancellingHttpWork() throws Exception {
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var server =
        McpServerKit.builder("notifications", "1", McpEmptyContext.class)
            .asyncMethod("slow", (applicationContext, call, handlerContext) -> applicationFuture)
            .build();
    var transport = new StreamableHttpMcpTransport<>(server);
    var pending =
        transport.handle(McpEmptyContext.INSTANCE, request("slow", "application/json", Map.of()));
    ObjectNode cancellation = mapper.createObjectNode().put("requestId", 1);

    assertEquals(
        400,
        response(
                transport,
                notification(
                    "notifications/cancelled",
                    "different/method",
                    "application/json",
                    cancellation))
            .status());
    assertFalse(applicationFuture.isCancelled());

    HttpMcpResponse rejected =
        transport
            .handle(
                McpEmptyContext.INSTANCE,
                notification(
                    "notifications/cancelled",
                    "notifications/cancelled",
                    "application/json",
                    cancellation))
            .toCompletableFuture()
            .join();

    try {
      assertEquals(400, rejected.status());
      assertFalse(applicationFuture.isCancelled());
      assertFalse(pending.toCompletableFuture().isDone());
    } finally {
      server.close();
    }
  }

  @Test
  void appliesAnExactImmutableOriginAllowlistBeforeOtherValidation() throws Exception {
    var server = McpServerKit.builder("origins", "1", McpEmptyContext.class).build();
    var defaultTransport = new StreamableHttpMcpTransport<>(server);
    HttpMcpRequest valid = request("server/discover", "application/json", Map.of());
    var allowed = new HashSet<>(Set.of(URI.create("https://Example.COM")));
    var configured = new StreamableHttpMcpTransport<>(server, allowed);

    assertEquals(200, response(defaultTransport, valid).status());
    assertEquals(
        403,
        response(defaultTransport, withHeader(valid, "Origin", "https://example.com")).status());
    assertEquals(
        200, response(configured, withHeader(valid, "Origin", "https://example.com:443")).status());
    assertEquals(
        403, response(configured, withHeader(valid, "Origin", "https://example.com:444")).status());
    allowed.add(URI.create("https://later.example"));
    assertEquals(
        403, response(configured, withHeader(valid, "Origin", "https://later.example")).status());
    assertEquals(403, response(configured, withHeader(valid, "Origin", "null")).status());
    assertEquals(403, response(configured, withHeader(valid, "Origin", "*")).status());
    assertEquals(
        403, response(configured, withHeader(valid, "Origin", "https:example.com")).status());
    assertEquals(403, response(configured, withHeader(valid, "Origin", "https://[")).status());
    assertEquals(
        403,
        response(
                configured,
                withAdditionalHeader(
                    withHeader(valid, "Origin", "https://example.com:443"),
                    "origin",
                    "https://example.com:443"))
            .status());
    HttpMcpRequest invalidMethod =
        new HttpMcpRequest(
            "GET",
            withHeader(valid, "Origin", "https://untrusted.example").headers(),
            valid.body());
    assertEquals(403, response(configured, invalidMethod).status());

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new StreamableHttpMcpTransport<>(
                server, Set.of(URI.create("https://example.com/path"))));
    assertEquals(
        Set.of(List.of(McpServerKit.class), List.of(McpServerKit.class, Set.class)),
        Arrays.stream(StreamableHttpMcpTransport.class.getConstructors())
            .map(constructor -> List.of(constructor.getParameterTypes()))
            .collect(Collectors.toSet()));
  }

  @Test
  void rejectsAmbiguousSingletonsButCombinesRepeatedAcceptFields() throws Exception {
    var transport =
        new StreamableHttpMcpTransport<>(
            McpServerKit.builder("headers", "1", McpEmptyContext.class).build());
    HttpMcpRequest discover = request("server/discover", "application/json", Map.of());

    assertEquals(
        200,
        response(transport, withAdditionalHeader(discover, "accept", "text/event-stream"))
            .status());
    assertEquals(
        400,
        response(
                transport,
                withAdditionalHeader(discover, "content-type", "application/json; charset=utf-8"))
            .status());
    assertEquals(
        400,
        response(
                transport,
                withAdditionalHeader(discover, "mcp-protocol-version", McpProtocol.REVISION))
            .status());
    assertEquals(
        400,
        response(transport, withAdditionalHeader(discover, "mcp-method", "server/discover"))
            .status());

    ObjectNode tool = mapper.createObjectNode().put("name", "echo");
    tool.putObject("arguments").put("value", "same");
    HttpMcpRequest call =
        request(
            "tools/call",
            "application/json",
            Map.of("Mcp-Name", "echo", "Mcp-Param-value", "same"),
            tool);
    assertEquals(400, response(transport, withAdditionalHeader(call, "mcp-name", "echo")).status());
  }

  @Test
  void validatesMirroredHeadersForTypedRequests() throws Exception {
    StreamableHttpMcpTransport<McpEmptyContext> transport =
        new StreamableHttpMcpTransport<>(
            McpServerKit.builder("http-test", "1", McpEmptyContext.class).build());
    ObjectNode tool = mapper.createObjectNode().put("name", "echo");
    tool.putObject("arguments").put("value", "expected");
    HttpMcpRequest toolRequest =
        request("tools/call", "application/json", Map.of("Mcp-Name", "echo"), tool);

    assertEquals(404, response(transport, toolRequest).status());
    assertEquals(400, response(transport, withoutHeader(toolRequest, "Mcp-Name")).status());
    assertEquals(400, response(transport, withHeader(toolRequest, "Mcp-Name", "other")).status());
    assertEquals(
        400,
        response(transport, withHeader(toolRequest, "MCP-Protocol-Version", "1900-01-01"))
            .status());
    ObjectNode prompt = mapper.createObjectNode().put("name", "hello");
    assertEquals(
        400,
        response(
                transport,
                request("prompts/get", "application/json", Map.of("Mcp-Name", "other"), prompt))
            .status());
    ObjectNode resource = mapper.createObjectNode().put("uri", "file:///value");
    assertEquals(
        400,
        response(
                transport,
                request(
                    "resources/read",
                    "application/json",
                    Map.of("Mcp-Name", "file:///other"),
                    resource))
            .status());
  }

  @ParameterizedTest
  @CsvSource({
    "number, 100.0, 100.0, 200",
    "number, 100.0, 1E+2, 200",
    "number, 1e2, 100.00, 200",
    "integer, 100, 100.0, 200",
    "number, -0.0, 0, 200",
    "number, 1e1000, 10e999, 200",
    "number, 1e-1000, 10e-1001, 200",
    "integer, 9007199254740993, 9007199254740993, 200",
    "number, 0.123456789012345678901, 0.1234567890123456789010, 200",
    "number, 100.0, =?base64?MTAwLjA=?=, 200",
    "number, 100.0, 101, 400",
    "number, 100.0, 100.000000000000000001, 400",
    "integer, 9007199254740993, 9007199254740992, 400",
    "number, 1e-1000, 0, 400",
    "number, 100.0, NaN, 400",
    "number, 100.0, Infinity, 400",
    "number, 100.0, not-a-number, 400",
    "number, 100.0, +100, 400",
    "number, 100.0, 0100, 400",
    "number, 100.0, 100., 400",
    "number, 0.1, .1, 400",
    "number, 100.0, 1e2147483648, 400",
    "number, 100.0, =?base64?!!!?=, 400",
    "number, 100.0, '', 400",
    "string, 100.0, 100.0, 200",
    "string, 100.0, 1E+2, 400",
    "boolean, true, true, 200",
    "boolean, true, TRUE, 400"
  })
  void comparesPrimitiveHeaderMirrorsWithoutLosingNumericPrecision(
      String type, String bodyValue, String headerValue, int expectedStatus) throws Exception {
    var schema =
        Map.of(
            "type",
            "object",
            "properties",
            Map.of("value", Map.of("type", type, "x-mcp-header", "Value")));
    var invoked = new AtomicBoolean();
    try (var server =
        McpServerKit.builder("numeric-headers", "1", McpEmptyContext.class)
            .syncTool(
                new McpTool("echo", schema),
                (applicationContext, parameters, handlerContext) -> {
                  invoked.set(true);
                  return new McpCallToolResult(List.of(new McpTextContent("called")));
                })
            .build()) {
      var transport = new StreamableHttpMcpTransport<>(server);
      ObjectNode call = mapper.createObjectNode().put("name", "echo");
      ObjectNode arguments = call.putObject("arguments");
      switch (type) {
        case "string" -> arguments.put("value", bodyValue);
        case "boolean" -> arguments.put("value", Boolean.parseBoolean(bodyValue));
        default -> arguments.put("value", new BigDecimal(bodyValue));
      }

      var result =
          response(
              transport,
              request(
                  "tools/call",
                  "application/json",
                  Map.of("Mcp-Name", "echo", "Mcp-Param-Value", headerValue),
                  call));

      assertEquals(expectedStatus, result.status());
      assertEquals(expectedStatus == 200, invoked.get());
      if (expectedStatus == 400) {
        assertEquals(-32020, mapper.readTree(result.body()).path("error").path("code").intValue());
      }
    }
  }

  @Test
  void validatesEveryDeclaredToolHeaderBeforeApplicationDispatch() throws Exception {
    var schema =
        Map.of(
            "type",
            "object",
            "properties",
            Map.of(
                "region", Map.of("type", "string", "x-mcp-header", "Region"),
                "routing",
                    Map.of(
                        "type",
                        "object",
                        "properties",
                        Map.of("tenant", Map.of("type", "string", "x-mcp-header", "Tenant")))));
    var invoked = new AtomicBoolean();
    var server =
        McpServerKit.builder("headers", "1", McpEmptyContext.class)
            .syncTool(
                new McpTool("route", schema),
                (applicationContext, parameters, handlerContext) -> {
                  invoked.set(true);
                  return new McpCallToolResult(List.of(new McpTextContent("called")));
                })
            .build();
    var transport = new StreamableHttpMcpTransport<>(server);
    ObjectNode call = mapper.createObjectNode().put("name", "route");
    call.putObject("arguments")
        .put("region", "eu-west")
        .putObject("routing")
        .put("tenant", "customer-7");
    Map<String, String> validHeaders =
        Map.of(
            "Mcp-Name", "route",
            "Mcp-Param-Region", "eu-west",
            "Mcp-Param-Tenant", "customer-7");
    HttpMcpRequest validCall = request("tools/call", "application/json", validHeaders, call);

    assertEquals(200, response(transport, validCall).status());
    assertTrue(invoked.getAndSet(false));
    assertEquals(
        400,
        response(transport, withAdditionalHeader(validCall, "mcp-param-region", "eu-west"))
            .status());
    assertFalse(invoked.get());
    assertEquals(
        400,
        response(
                transport,
                request(
                    "tools/call",
                    "application/json",
                    Map.of("Mcp-Name", "route", "Mcp-Param-Tenant", "customer-7"),
                    call))
            .status());
    assertFalse(invoked.get());
    assertEquals(
        400,
        response(
                transport,
                request(
                    "tools/call",
                    "application/json",
                    Map.of(
                        "Mcp-Name",
                        "route",
                        "Mcp-Param-Region",
                        "eu-west",
                        "Mcp-Param-Tenant",
                        "other"),
                    call))
            .status());
    assertFalse(invoked.get());
    var headersWithUnknown = new HashMap<>(validHeaders);
    headersWithUnknown.put("Mcp-Param-Unrecognized", "forwarded");
    assertEquals(
        200,
        response(transport, request("tools/call", "application/json", headersWithUnknown, call))
            .status());
    assertTrue(invoked.get());
  }

  @Test
  void decodesTheExactBase64SentinelBeforeComparingMirroredValues() throws Exception {
    var schema =
        Map.of(
            "type",
            "object",
            "properties",
            Map.of("greeting", Map.of("type", "string", "x-mcp-header", "Greeting")));
    var invoked = new AtomicBoolean();
    var server =
        McpServerKit.builder("encoding", "1", McpEmptyContext.class)
            .syncTool(
                new McpTool("écho", schema),
                (applicationContext, parameters, handlerContext) -> {
                  invoked.set(true);
                  return new McpCallToolResult(List.of(new McpTextContent("called")));
                })
            .build();
    var transport = new StreamableHttpMcpTransport<>(server);
    ObjectNode call = mapper.createObjectNode().put("name", "écho");
    call.putObject("arguments").put("greeting", "Hello, 世界");

    assertEquals(
        200,
        response(
                transport,
                request(
                    "tools/call",
                    "application/json",
                    Map.of(
                        "Mcp-Name",
                        "=?base64?w6ljaG8=?=",
                        "Mcp-Param-Greeting",
                        "=?base64?SGVsbG8sIOS4lueVjA==?="),
                    call))
            .status());
    assertTrue(invoked.getAndSet(false));
    assertEquals(
        400,
        response(
                transport,
                request(
                    "tools/call",
                    "application/json",
                    Map.of(
                        "Mcp-Name",
                        "=?base64?w6ljaG8=?=",
                        "Mcp-Param-Greeting",
                        "=?base64?invalid!?="),
                    call))
            .status());
    assertFalse(invoked.get());
  }

  @Test
  void passesTheExactContextAndLeavesCallerIdentityToTheHost() throws Exception {
    AtomicReference<ApplicationContext> seen = new AtomicReference<>();
    var serverKit =
        McpServerKit.builder("http-context", "1", ApplicationContext.class)
            .syncMethod(
                "context",
                (applicationContext, call, handlerContext) -> {
                  seen.set(applicationContext);
                  return new JsonRpcResultResponse(
                      call.id(), Map.of("value", applicationContext.value));
                })
            .build();
    var transport = new StreamableHttpMcpTransport<>(serverKit);
    var applicationContext = new ApplicationContext("exact");
    HttpMcpRequest request = request("context", "application/json", Map.of());

    assertEquals(200, response(transport, applicationContext, request).status());
    assertSame(applicationContext, seen.get());
    assertThrows(NullPointerException.class, () -> transport.handle(null, request));
    assertEquals(
        List.of("method", "headers", "body"),
        Arrays.stream(HttpMcpRequest.class.getRecordComponents())
            .map(RecordComponent::getName)
            .toList());
  }

  @Test
  void streamsAcknowledgementAndMatchingNotifications() throws Exception {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("http-test", "1", McpEmptyContext.class)
            .syncResource(
                new McpResource(URI.create("test://live"), "live"),
                (applicationContext, call, handlerContext) ->
                    new McpReadResourceResult(
                        List.of(new McpTextResourceContents(call.uri(), "live"))))
            .build();
    ObjectNode extra = mapper.createObjectNode();
    extra.putObject("notifications").putArray("resourceSubscriptions").add("test://live");
    HttpMcpRequest request = request("subscriptions/listen", "application/json", Map.of(), extra);
    HttpEventStreamResponse response =
        assertInstanceOf(
            HttpEventStreamResponse.class,
            new StreamableHttpMcpTransport<>(server)
                .handle(McpEmptyContext.INSTANCE, request)
                .toCompletableFuture()
                .join());
    CollectingSubscriber subscriber = new CollectingSubscriber();
    response.events().subscribe(subscriber);

    assertEquals(200, response.status());
    assertEquals("text/event-stream", response.headers().get("Content-Type"));
    assertTrue(subscriber.first.await(5, TimeUnit.SECONDS));
    server.emit(
        new McpResourceUpdatedNotification(
            new McpResourceUpdatedNotificationParams(Optional.empty(), URI.create("test://live"))));
    assertTrue(
        subscriber.second.await(5, TimeUnit.SECONDS),
        () -> "frames=" + subscriber.frames + ", failure=" + subscriber.failure.get());
    response.close();
    assertEquals(2, subscriber.frames.size());
    assertTrue(subscriber.frames.get(0).contains("notifications/subscriptions/acknowledged"));
    assertTrue(subscriber.frames.get(1).contains("notifications/resources/updated"));
  }

  private HttpMcpRequest request(String method, String accept, Map<String, String> extraHeaders)
      throws Exception {
    return request(method, accept, extraHeaders, mapper.createObjectNode());
  }

  private HttpMcpRequest request(
      String method, String accept, Map<String, String> extraHeaders, ObjectNode extraParams)
      throws Exception {
    ObjectNode body =
        mapper.createObjectNode().put("jsonrpc", "2.0").put("id", 1).put("method", method);
    ObjectNode params = body.putObject("params");
    params.setAll(extraParams);
    params
        .putObject("_meta")
        .put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION)
        .putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    HashMap<String, List<String>> headers =
        new HashMap<>(
            Map.of(
                "Content-Type",
                List.of("application/json"),
                "Accept",
                List.of(accept),
                "MCP-Protocol-Version",
                List.of(McpProtocol.REVISION),
                "Mcp-Method",
                List.of(method)));
    extraHeaders.forEach((name, value) -> headers.put(name, List.of(value)));
    return new HttpMcpRequest("POST", headers, mapper.writeValueAsBytes(body));
  }

  private HttpMcpRequest notification(
      String method, String mirroredMethod, String accept, ObjectNode params) throws Exception {
    ObjectNode body = mapper.createObjectNode().put("jsonrpc", "2.0").put("method", method);
    body.set("params", params);
    return new HttpMcpRequest(
        "POST",
        Map.of(
            "Content-Type", List.of("application/json"),
            "Accept", List.of(accept),
            "MCP-Protocol-Version", List.of(McpProtocol.REVISION),
            "Mcp-Method", List.of(mirroredMethod)),
        mapper.writeValueAsBytes(body));
  }

  private static HttpJsonResponse response(
      StreamableHttpMcpTransport<McpEmptyContext> transport, HttpMcpRequest request) {
    return assertInstanceOf(
        HttpJsonResponse.class,
        transport.handle(McpEmptyContext.INSTANCE, request).toCompletableFuture().join());
  }

  private static <C> HttpJsonResponse response(
      StreamableHttpMcpTransport<C> transport, C applicationContext, HttpMcpRequest request) {
    return assertInstanceOf(
        HttpJsonResponse.class,
        transport.handle(applicationContext, request).toCompletableFuture().join());
  }

  private static HttpMcpRequest withHeader(HttpMcpRequest request, String name, String value) {
    HashMap<String, List<String>> headers = new HashMap<>(request.headers());
    headers.keySet().removeIf(key -> key.equalsIgnoreCase(name));
    headers.put(name, List.of(value));
    return new HttpMcpRequest(request.method(), headers, request.body());
  }

  private static HttpMcpRequest withAdditionalHeader(
      HttpMcpRequest request, String name, String value) {
    LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>(request.headers());
    headers.put(name, List.of(value));
    return new HttpMcpRequest(request.method(), headers, request.body());
  }

  private static HttpMcpRequest withoutHeader(HttpMcpRequest request, String name) {
    HashMap<String, List<String>> headers = new HashMap<>(request.headers());
    headers.keySet().removeIf(key -> key.equalsIgnoreCase(name));
    return new HttpMcpRequest(request.method(), headers, request.body());
  }

  private HttpMcpRequest withoutMetadata(HttpMcpRequest request) throws Exception {
    ObjectNode body = (ObjectNode) mapper.readTree(request.body());
    body.withObject("params").remove("_meta");
    return new HttpMcpRequest(request.method(), request.headers(), mapper.writeValueAsBytes(body));
  }

  private HttpMcpRequest withProgressToken(HttpMcpRequest request) throws Exception {
    ObjectNode body = (ObjectNode) mapper.readTree(request.body());
    body.withObject("params").withObject("_meta").put("progressToken", "progress-1");
    return new HttpMcpRequest(request.method(), request.headers(), mapper.writeValueAsBytes(body));
  }

  private static final class CollectingSubscriber implements Flow.Subscriber<ByteBuffer> {
    private final CopyOnWriteArrayList<String> frames = new CopyOnWriteArrayList<>();
    private final CountDownLatch first = new CountDownLatch(1);
    private final CountDownLatch second = new CountDownLatch(1);
    private final AtomicReference<Throwable> failure = new AtomicReference<>();

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ByteBuffer item) {
      byte[] bytes = new byte[item.remaining()];
      item.get(bytes);
      frames.add(new String(bytes, StandardCharsets.UTF_8));
      first.countDown();
      if (frames.size() == 2) {
        second.countDown();
      }
    }

    @Override
    public void onError(Throwable throwable) {
      failure.set(throwable);
    }

    @Override
    public void onComplete() {}
  }

  private record ApplicationContext(String value) {}
}
