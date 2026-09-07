package io.github.suppierk.mcp.example.springwebmvc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpProtocol;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringWebMvcExampleApplicationTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String USERNAME = "demo-user";
  private static final String PASSWORD = "demo-password";

  @LocalServerPort private int port;

  @Test
  void receivesASubscriptionAcknowledgementBeforeTheStreamCloses() {
    var params = JSON.createObjectNode();
    params.putObject("notifications").put("toolsListChanged", true);
    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
    factory.setReadTimeout(30_000);
    RestClient.builder()
        .baseUrl("http://localhost:" + port)
        .requestFactory(factory)
        .build()
        .post()
        .uri("/mcp/public")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json, text/event-stream")
        .header("MCP-Protocol-Version", McpProtocol.REVISION)
        .header("Mcp-Method", "subscriptions/listen")
        .body(requestBody("subscriptions/listen", params))
        .exchange(
            (sent, response) -> {
              assertEquals(200, response.getStatusCode().value());
              assertEquals("text/event-stream", response.getHeaders().getContentType().toString());
              var reader =
                  new java.io.BufferedReader(
                      new java.io.InputStreamReader(response.getBody(), UTF_8));
              assertEquals("event: message", reader.readLine());
              var notification = JSON.readTree(reader.readLine().substring(6));
              assertEquals(
                  "notifications/subscriptions/acknowledged",
                  notification.path("method").textValue());
              assertEquals(
                  1,
                  notification
                      .path("params")
                      .path("_meta")
                      .path("io.modelcontextprotocol/subscriptionId")
                      .intValue());
              assertEquals("", reader.readLine());
              return null;
            });
  }

  @Test
  void callsTheAnonymousHelloTool() throws Exception {
    EndpointResponse response =
        post(
            "/mcp/public",
            McpCallToolRequest.METHOD,
            toolCall("hello", JSON.createObjectNode()),
            null);

    assertEquals(200, response.status(), response.body());
    assertEquals(
        "Hello, World!",
        json(response).path("result").path("content").path(0).path("text").textValue());
  }

  @Test
  void rejectsMissingAndInvalidCredentialsBeforeMcpDispatch() {
    EndpointResponse missing = postInvalid("/mcp/protected", null);
    EndpointResponse invalid =
        postInvalid("/mcp/protected", headers -> headers.setBasicAuth(USERNAME, "wrong-password"));

    assertEquals(401, missing.status());
    assertEquals(401, invalid.status());
  }

  @Test
  void passesTheAuthenticatedSpringIdentityToTheProtectedTool() throws Exception {
    EndpointResponse response =
        post(
            "/mcp/protected",
            McpCallToolRequest.METHOD,
            toolCall("current-user", JSON.createObjectNode()),
            headers -> headers.setBasicAuth(USERNAME, PASSWORD));

    assertEquals(200, response.status());
    JsonNode content = json(response).path("result").path("content");
    assertEquals(1, content.size());
    assertEquals("text", content.path(0).path("type").textValue());
    assertEquals(USERNAME, content.path(0).path("text").textValue());
  }

  @Test
  void rejectsArgumentsOutsideTheClosedCurrentUserSchema() throws Exception {
    ObjectNode arguments = JSON.createObjectNode().put("unexpected", "value");
    EndpointResponse response =
        post(
            "/mcp/protected",
            McpCallToolRequest.METHOD,
            toolCall("current-user", arguments),
            headers -> headers.setBasicAuth(USERNAME, PASSWORD));

    assertEquals(400, response.status(), response.body());
    assertEquals(-32602, json(response).path("error").path("code").intValue());
  }

  @Test
  void exposesSeparateFixedRegistries() throws Exception {
    EndpointResponse publicList =
        post("/mcp/public", McpListToolsRequest.METHOD, JSON.createObjectNode(), null);
    EndpointResponse protectedList =
        post(
            "/mcp/protected",
            McpListToolsRequest.METHOD,
            JSON.createObjectNode(),
            headers -> headers.setBasicAuth(USERNAME, PASSWORD));

    JsonNode publicTools = json(publicList).path("result").path("tools");
    JsonNode protectedTools = json(protectedList).path("result").path("tools");
    assertEquals(1, publicTools.size());
    assertEquals("hello", publicTools.path(0).path("name").textValue());
    assertEquals(1, protectedTools.size());
    assertEquals("current-user", protectedTools.path(0).path("name").textValue());
  }

  private EndpointResponse post(
      String path, String method, ObjectNode params, Consumer<HttpHeaders> headers) {
    RestClient.RequestBodySpec request =
        RestClient.create("http://localhost:" + port)
            .post()
            .uri(path)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("MCP-Protocol-Version", McpProtocol.REVISION)
            .header("Mcp-Method", method);
    if (params.has("name")) {
      request.header("Mcp-Name", params.path("name").textValue());
    }
    if (headers != null) {
      request.headers(headers);
    }
    return request
        .body(requestBody(method, params))
        .exchange(
            (sent, response) ->
                new EndpointResponse(
                    response.getStatusCode().value(),
                    new String(response.getBody().readAllBytes(), UTF_8)));
  }

  private EndpointResponse postInvalid(String path, Consumer<HttpHeaders> headers) {
    RestClient.RequestBodySpec request =
        RestClient.create("http://localhost:" + port)
            .post()
            .uri(path)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
    if (headers != null) {
      request.headers(headers);
    }
    return request
        .body("not-json")
        .exchange(
            (sent, response) ->
                new EndpointResponse(
                    response.getStatusCode().value(),
                    new String(response.getBody().readAllBytes(), UTF_8)));
  }

  private static ObjectNode toolCall(String name, ObjectNode arguments) {
    ObjectNode params = JSON.createObjectNode().put("name", name);
    params.set("arguments", arguments);
    return params;
  }

  private static String requestBody(String method, ObjectNode params) {
    ObjectNode metadata = params.putObject("_meta");
    metadata.put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION);
    metadata.putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    ObjectNode request =
        JSON.createObjectNode().put("jsonrpc", "2.0").put("id", 1).put("method", method);
    request.set("params", params);
    return request.toString();
  }

  private static JsonNode json(EndpointResponse response) throws IOException {
    return JSON.readTree(response.body());
  }

  private record EndpointResponse(int status, String body) {}

  @org.junit.jupiter.api.AfterAll
  static void closeOpenSubscriptions(
      @org.springframework.beans.factory.annotation.Autowired
          io.github.suppierk.mcp.server.McpServerKit<io.github.suppierk.mcp.server.McpEmptyContext>
              serverKit) {
    serverKit.close();
  }
}
