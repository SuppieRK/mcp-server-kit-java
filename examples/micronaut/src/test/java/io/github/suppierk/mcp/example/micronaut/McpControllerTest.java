package io.github.suppierk.mcp.example.micronaut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.StreamingHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@MicronautTest
class McpControllerTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String USERNAME = "demo-user";
  private static final String PASSWORD = "demo-password";

  @Inject
  @Client("/")
  private HttpClient client;

  @Inject
  @Client("/")
  private StreamingHttpClient streamingClient;

  @Test
  void receivesASubscriptionAcknowledgementBeforeTheStreamCloses() throws Exception {
    var params = JSON.createObjectNode();
    params.putObject("notifications").put("toolsListChanged", true);
    var first = new CompletableFuture<HttpResponse<byte[]>>();
    var received = new ByteArrayOutputStream();
    var subscription = new AtomicReference<Subscription>();
    streamingClient
        .exchangeStream(
            request("/mcp/public", "subscriptions/listen", params)
                .accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_EVENT_STREAM_TYPE))
        .subscribe(
            new Subscriber<>() {
              public void onSubscribe(Subscription value) {
                subscription.set(value);
                value.request(Long.MAX_VALUE);
              }

              public void onNext(HttpResponse<ByteBuffer<?>> response) {
                received.writeBytes(response.body().toByteArray());
                if (received.toString(StandardCharsets.UTF_8).contains("\n\n")) {
                  first.complete(response.toMutableResponse().body(received.toByteArray()));
                }
              }

              public void onError(Throwable failure) {
                first.completeExceptionally(failure);
              }

              public void onComplete() {}
            });
    try {
      var response = first.get(30, TimeUnit.SECONDS);
      assertEquals(HttpStatus.OK, response.getStatus());
      assertEquals(MediaType.TEXT_EVENT_STREAM_TYPE, response.getContentType().orElseThrow());
      String data =
          new String(response.body(), StandardCharsets.UTF_8)
              .lines()
              .filter(line -> line.startsWith("data: "))
              .findFirst()
              .orElseThrow()
              .substring(6);
      var notification = JSON.readTree(data);
      assertEquals(
          "notifications/subscriptions/acknowledged", notification.path("method").textValue());
      assertEquals(
          1,
          notification
              .path("params")
              .path("_meta")
              .path("io.modelcontextprotocol/subscriptionId")
              .intValue());
    } finally {
      if (subscription.get() != null) {
        subscription.get().cancel();
      }
    }
  }

  @Test
  void streamsToolResponsesWhenProgressIsRequested() throws Exception {
    var params = toolCall("hello", JSON.createObjectNode());
    params.putObject("_meta").put("progressToken", "hello-progress");
    var response =
        exchange(
            request("/mcp/public", McpCallToolRequest.METHOD, params)
                .accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_EVENT_STREAM_TYPE));

    assertEquals(HttpStatus.OK, response.getStatus());
    assertEquals(MediaType.TEXT_EVENT_STREAM_TYPE, response.getContentType().orElseThrow());
    String data =
        response
            .body()
            .lines()
            .filter(line -> line.startsWith("data: "))
            .findFirst()
            .orElseThrow()
            .substring(6);
    assertEquals(
        "Hello, World!",
        JSON.readTree(data).path("result").path("content").path(0).path("text").textValue());
  }

  @Test
  void callsTheAnonymousHelloTool() throws Exception {
    HttpResponse<String> response =
        exchange(
            request(
                "/mcp/public",
                McpCallToolRequest.METHOD,
                toolCall("hello", JSON.createObjectNode())));

    assertEquals(HttpStatus.OK, response.getStatus());
    assertEquals(
        "Hello, World!",
        json(response).path("result").path("content").path(0).path("text").textValue());
  }

  @Test
  void rejectsMissingAndInvalidCredentialsBeforeMcpDispatch() {
    MutableHttpRequest<String> missing =
        HttpRequest.POST("/mcp/protected", "not-json")
            .contentType(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE);
    MutableHttpRequest<String> invalid =
        HttpRequest.POST("/mcp/protected", "not-json")
            .contentType(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .basicAuth(USERNAME, "wrong-password");

    assertUnauthorized(missing);
    assertUnauthorized(invalid);
  }

  @Test
  void passesTheMicronautAuthenticationToTheProtectedTool() throws Exception {
    MutableHttpRequest<String> request =
        request(
                "/mcp/protected",
                McpCallToolRequest.METHOD,
                toolCall("current-user", JSON.createObjectNode()))
            .basicAuth(USERNAME, PASSWORD);

    HttpResponse<String> response = exchange(request);

    assertEquals(HttpStatus.OK, response.getStatus());
    JsonNode content = json(response).path("result").path("content");
    assertEquals(1, content.size());
    assertEquals("text", content.path(0).path("type").textValue());
    assertEquals(USERNAME, content.path(0).path("text").textValue());
  }

  @Test
  void rejectsArgumentsOutsideTheClosedCurrentUserSchema() throws Exception {
    MutableHttpRequest<String> request =
        request(
                "/mcp/protected",
                McpCallToolRequest.METHOD,
                toolCall("current-user", JSON.createObjectNode().put("unexpected", "value")))
            .basicAuth(USERNAME, PASSWORD);

    HttpClientResponseException failure =
        assertThrows(HttpClientResponseException.class, () -> exchange(request));

    assertEquals(HttpStatus.BAD_REQUEST, failure.getStatus());
    assertEquals(
        -32602,
        JSON.readTree(failure.getResponse().getBody(String.class).orElseThrow())
            .path("error")
            .path("code")
            .intValue());
  }

  @Test
  void exposesSeparateFixedRegistries() throws Exception {
    HttpResponse<String> publicList =
        exchange(request("/mcp/public", McpListToolsRequest.METHOD, JSON.createObjectNode()));
    HttpResponse<String> protectedList =
        exchange(
            request("/mcp/protected", McpListToolsRequest.METHOD, JSON.createObjectNode())
                .basicAuth(USERNAME, PASSWORD));

    JsonNode publicTools = json(publicList).path("result").path("tools");
    JsonNode protectedTools = json(protectedList).path("result").path("tools");
    assertEquals(1, publicTools.size());
    assertEquals("hello", publicTools.path(0).path("name").textValue());
    assertEquals(1, protectedTools.size());
    assertEquals("current-user", protectedTools.path(0).path("name").textValue());
  }

  private HttpResponse<String> exchange(MutableHttpRequest<String> request) {
    return client.toBlocking().exchange(request, String.class);
  }

  private void assertUnauthorized(MutableHttpRequest<String> request) {
    HttpClientResponseException failure =
        assertThrows(HttpClientResponseException.class, () -> exchange(request));
    assertEquals(HttpStatus.UNAUTHORIZED, failure.getStatus());
  }

  private static MutableHttpRequest<String> request(String path, String method, ObjectNode params) {
    MutableHttpRequest<String> request =
        HttpRequest.POST(path, requestBody(method, params))
            .contentType(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .header("MCP-Protocol-Version", McpProtocol.REVISION)
            .header("Mcp-Method", method);
    if (params.has("name")) {
      request.header("Mcp-Name", params.path("name").textValue());
    }
    return request;
  }

  private static ObjectNode toolCall(String name, ObjectNode arguments) {
    ObjectNode params = JSON.createObjectNode().put("name", name);
    params.set("arguments", arguments);
    return params;
  }

  private static String requestBody(String method, ObjectNode params) {
    ObjectNode metadata = params.withObject("_meta");
    metadata.put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION);
    metadata.putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    ObjectNode request =
        JSON.createObjectNode().put("jsonrpc", "2.0").put("id", 1).put("method", method);
    request.set("params", params);
    return request.toString();
  }

  private static JsonNode json(HttpResponse<String> response) throws IOException {
    return JSON.readTree(response.body());
  }
}
