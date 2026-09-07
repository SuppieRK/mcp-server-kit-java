package io.github.suppierk.mcp.example.javalin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JavalinExampleApplicationTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String USERNAME = "demo-user";
  private static final String PASSWORD = "demo-password";
  private static final HttpClient CLIENT = HttpClient.newHttpClient();
  private static Javalin application;

  @BeforeAll
  static void startApplication() {
    application = JavalinExampleApplication.create().start(0);
  }

  @AfterAll
  static void stopApplication() {
    application.stop();
  }

  @Test
  void callsTheAnonymousHelloTool() throws Exception {
    HttpResponse<String> response =
        post(
            "/mcp/public",
            McpCallToolRequest.METHOD,
            toolCall("hello", JSON.createObjectNode()),
            null);

    assertEquals(200, response.statusCode());
    assertEquals(
        "Hello, World!",
        json(response).path("result").path("content").path(0).path("text").textValue());
  }

  @Test
  void rejectsMissingAndInvalidCredentialsBeforeMcpDispatch() throws Exception {
    HttpResponse<String> missing = postInvalid("/mcp/protected", null);
    HttpResponse<String> invalid =
        postInvalid(
            "/mcp/protected", request -> request.header("Authorization", basic(USERNAME, "wrong")));

    assertEquals(401, missing.statusCode());
    assertEquals(401, invalid.statusCode());
  }

  @Test
  void passesTheApplicationIdentityToTheProtectedTool() throws Exception {
    HttpResponse<String> response =
        post(
            "/mcp/protected",
            McpCallToolRequest.METHOD,
            toolCall("current-user", JSON.createObjectNode()),
            request -> request.header("Authorization", basic(USERNAME, PASSWORD)));

    assertEquals(200, response.statusCode());
    JsonNode content = json(response).path("result").path("content");
    assertEquals(1, content.size());
    assertEquals("text", content.path(0).path("type").textValue());
    assertEquals(USERNAME, content.path(0).path("text").textValue());
  }

  @Test
  void rejectsArgumentsOutsideTheClosedCurrentUserSchema() throws Exception {
    HttpResponse<String> response =
        post(
            "/mcp/protected",
            McpCallToolRequest.METHOD,
            toolCall("current-user", JSON.createObjectNode().put("unexpected", "value")),
            request -> request.header("Authorization", basic(USERNAME, PASSWORD)));

    assertEquals(400, response.statusCode());
    assertEquals(-32602, json(response).path("error").path("code").intValue());
  }

  @Test
  void exposesSeparateFixedRegistries() throws Exception {
    HttpResponse<String> publicList =
        post("/mcp/public", McpListToolsRequest.METHOD, JSON.createObjectNode(), null);
    HttpResponse<String> protectedList =
        post(
            "/mcp/protected",
            McpListToolsRequest.METHOD,
            JSON.createObjectNode(),
            request -> request.header("Authorization", basic(USERNAME, PASSWORD)));

    JsonNode publicTools = json(publicList).path("result").path("tools");
    JsonNode protectedTools = json(protectedList).path("result").path("tools");
    assertEquals(1, publicTools.size());
    assertEquals("hello", publicTools.path(0).path("name").textValue());
    assertEquals(1, protectedTools.size());
    assertEquals("current-user", protectedTools.path(0).path("name").textValue());
  }

  private static HttpResponse<String> post(
      String path, String method, ObjectNode params, Consumer<HttpRequest.Builder> customization)
      throws IOException, InterruptedException {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + application.port() + path))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("MCP-Protocol-Version", McpProtocol.REVISION)
            .header("Mcp-Method", method);
    if (params.has("name")) {
      request.header("Mcp-Name", params.path("name").textValue());
    }
    if (customization != null) {
      customization.accept(request);
    }
    return CLIENT.send(
        request.POST(HttpRequest.BodyPublishers.ofString(requestBody(method, params))).build(),
        HttpResponse.BodyHandlers.ofString());
  }

  private static HttpResponse<String> postInvalid(
      String path, Consumer<HttpRequest.Builder> customization)
      throws IOException, InterruptedException {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + application.port() + path))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
    if (customization != null) {
      customization.accept(request);
    }
    return CLIENT.send(
        request.POST(HttpRequest.BodyPublishers.ofString("not-json")).build(),
        HttpResponse.BodyHandlers.ofString());
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

  private static String basic(String username, String password) {
    String credentials = username + ":" + password;
    return "Basic "
        + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }

  private static JsonNode json(HttpResponse<String> response) throws IOException {
    return JSON.readTree(response.body());
  }
}
