package io.github.suppierk.mcp.example.springwebflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpProtocol;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringWebFluxExampleApplicationTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String USERNAME = "demo-user";
  private static final String PASSWORD = "demo-password";

  @LocalServerPort private int port;
  private WebTestClient client;

  @BeforeEach
  void createClient() {
    client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void callsTheAnonymousHelloTool() {
    post("/mcp/public", McpCallToolRequest.METHOD, toolCall("hello", JSON.createObjectNode()), null)
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.result.content[0].text")
        .isEqualTo("Hello, World!");
  }

  @Test
  void rejectsMissingAndInvalidCredentialsBeforeMcpDispatch() {
    postInvalid("/mcp/protected", null).expectStatus().isUnauthorized();
    postInvalid("/mcp/protected", headers -> headers.setBasicAuth(USERNAME, "wrong-password"))
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void passesTheAuthenticatedSpringIdentityToTheProtectedTool() {
    post(
            "/mcp/protected",
            McpCallToolRequest.METHOD,
            toolCall("current-user", JSON.createObjectNode()),
            headers -> headers.setBasicAuth(USERNAME, PASSWORD))
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.result.content.length()")
        .isEqualTo(1)
        .jsonPath("$.result.content[0].type")
        .isEqualTo("text")
        .jsonPath("$.result.content[0].text")
        .isEqualTo(USERNAME);
  }

  @Test
  void rejectsArgumentsOutsideTheClosedCurrentUserSchema() {
    post(
            "/mcp/protected",
            McpCallToolRequest.METHOD,
            toolCall("current-user", JSON.createObjectNode().put("unexpected", "value")),
            headers -> headers.setBasicAuth(USERNAME, PASSWORD))
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.error.code")
        .isEqualTo(-32602);
  }

  @Test
  void exposesSeparateFixedRegistries() {
    post("/mcp/public", McpListToolsRequest.METHOD, JSON.createObjectNode(), null)
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.result.tools.length()")
        .isEqualTo(1)
        .jsonPath("$.result.tools[0].name")
        .isEqualTo("hello");

    post(
            "/mcp/protected",
            McpListToolsRequest.METHOD,
            JSON.createObjectNode(),
            headers -> headers.setBasicAuth(USERNAME, PASSWORD))
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.result.tools.length()")
        .isEqualTo(1)
        .jsonPath("$.result.tools[0].name")
        .isEqualTo("current-user");
  }

  private WebTestClient.ResponseSpec post(
      String path, String method, ObjectNode params, Consumer<HttpHeaders> headers) {
    WebTestClient.RequestBodySpec request =
        client
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
    return request.bodyValue(requestBody(method, params)).exchange();
  }

  private WebTestClient.ResponseSpec postInvalid(String path, Consumer<HttpHeaders> headers) {
    WebTestClient.RequestBodySpec request =
        client
            .post()
            .uri(path)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
    if (headers != null) {
      request.headers(headers);
    }
    return request.bodyValue("not-json").exchange();
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
}
