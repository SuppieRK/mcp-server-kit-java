package io.github.suppierk.mcp.example.quarkus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

@QuarkusTest
class McpEndpointTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String USERNAME = "demo-user";
  private static final String PASSWORD = "demo-password";

  @Test
  void streamsToolResponsesWhenProgressIsRequested() throws Exception {
    var params = toolCall("hello", JSON.createObjectNode());
    params.putObject("_meta").put("progressToken", "hello-progress");
    String body =
        mcpRequest(McpCallToolRequest.METHOD, params)
            .accept("application/json, text/event-stream")
            .post("/mcp/public")
            .then()
            .statusCode(200)
            .contentType("text/event-stream")
            .extract()
            .asString();

    String data =
        body.lines()
            .filter(line -> line.startsWith("data: "))
            .findFirst()
            .orElseThrow()
            .substring(6);
    assertEquals(
        "Hello, World!",
        JSON.readTree(data).path("result").path("content").path(0).path("text").textValue());
  }

  @Test
  void callsTheAnonymousHelloTool() {
    mcpRequest(McpCallToolRequest.METHOD, toolCall("hello", JSON.createObjectNode()))
        .when()
        .post("/mcp/public")
        .then()
        .statusCode(200)
        .body("result.content[0].text", equalTo("Hello, World!"));
  }

  @Test
  void rejectsMissingAndInvalidCredentialsBeforeMcpDispatch() {
    given()
        .contentType("application/json")
        .accept("application/json")
        .body("not-json")
        .when()
        .post("/mcp/protected")
        .then()
        .statusCode(401);

    given()
        .auth()
        .preemptive()
        .basic(USERNAME, "wrong-password")
        .contentType("application/json")
        .accept("application/json")
        .body("not-json")
        .when()
        .post("/mcp/protected")
        .then()
        .statusCode(401);
  }

  @Test
  void passesTheQuarkusSecurityIdentityToTheProtectedTool() {
    mcpRequest(McpCallToolRequest.METHOD, toolCall("current-user", JSON.createObjectNode()))
        .auth()
        .preemptive()
        .basic(USERNAME, PASSWORD)
        .when()
        .post("/mcp/protected")
        .then()
        .statusCode(200)
        .body("result.content.size()", equalTo(1))
        .body("result.content[0].type", equalTo("text"))
        .body("result.content[0].text", equalTo(USERNAME));
  }

  @Test
  void rejectsArgumentsOutsideTheClosedCurrentUserSchema() {
    mcpRequest(
            McpCallToolRequest.METHOD,
            toolCall("current-user", JSON.createObjectNode().put("unexpected", "value")))
        .auth()
        .preemptive()
        .basic(USERNAME, PASSWORD)
        .when()
        .post("/mcp/protected")
        .then()
        .statusCode(400)
        .body("error.code", equalTo(-32602));
  }

  @Test
  void exposesSeparateFixedRegistries() {
    mcpRequest(McpListToolsRequest.METHOD, JSON.createObjectNode())
        .when()
        .post("/mcp/public")
        .then()
        .statusCode(200)
        .body("result.tools.size()", equalTo(1))
        .body("result.tools[0].name", equalTo("hello"));

    mcpRequest(McpListToolsRequest.METHOD, JSON.createObjectNode())
        .auth()
        .preemptive()
        .basic(USERNAME, PASSWORD)
        .when()
        .post("/mcp/protected")
        .then()
        .statusCode(200)
        .body("result.tools.size()", equalTo(1))
        .body("result.tools[0].name", equalTo("current-user"));
  }

  private static RequestSpecification mcpRequest(String method, ObjectNode params) {
    RequestSpecification request =
        given()
            .contentType("application/json")
            .accept("application/json")
            .header("MCP-Protocol-Version", McpProtocol.REVISION)
            .header("Mcp-Method", method);
    if (params.has("name")) {
      request.header("Mcp-Name", params.path("name").textValue());
    }
    return request.body(requestBody(method, params));
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
}
