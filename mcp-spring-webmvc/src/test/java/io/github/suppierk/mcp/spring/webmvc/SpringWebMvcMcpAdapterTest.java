package io.github.suppierk.mcp.spring.webmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

class SpringWebMvcMcpAdapterTest {
  private final SpringWebMvcMcpAdapter<McpEmptyContext> adapter =
      new SpringWebMvcMcpAdapter<>(
          new StreamableHttpMcpTransport<>(
              McpServerKit.mcpServerKit("mvc-test", "1", McpEmptyContext.class).build()));

  @Test
  void mapsJson() throws Exception {
    MockHttpServletRequest request = request("application/json", "server/discover");

    ResponseEntity<?> response =
        adapter.handle(
            McpEmptyContext.INSTANCE,
            request,
            new MockHttpServletResponse(),
            body(true, "server/discover"));

    assertEquals(200, response.getStatusCode().value());
    var output = new ByteArrayOutputStream();
    assertInstanceOf(StreamingResponseBody.class, response.getBody()).writeTo(output);
    assertTrue(output.toString(StandardCharsets.UTF_8).contains(McpProtocol.REVISION));
  }

  @Test
  void mapsAcceptedNotifications() {
    ResponseEntity<?> response =
        adapter.handle(
            McpEmptyContext.INSTANCE,
            request("application/json", "notifications/ignored"),
            new MockHttpServletResponse(),
            body(false, "notifications/ignored"));

    assertEquals(202, response.getStatusCode().value());
    assertEquals(null, response.getBody());
  }

  @Test
  void preservesRepeatedHeaderValues() {
    MockHttpServletRequest request = request("application/json", "server/discover");
    request.addHeader("mcp-method", "server/discover");

    ResponseEntity<?> response =
        adapter.handle(
            McpEmptyContext.INSTANCE,
            request,
            new MockHttpServletResponse(),
            body(true, "server/discover"));

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void writesEventStreams() throws Exception {
    var servletResponse = new MockHttpServletResponse();
    ResponseEntity<?> response =
        adapter.handle(
            McpEmptyContext.INSTANCE,
            request("application/json", "server/discover"),
            servletResponse,
            body(true, "server/discover", true));
    StreamingResponseBody stream =
        assertInstanceOf(StreamingResponseBody.class, response.getBody());
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    stream.writeTo(output);

    assertTrue(servletResponse.getContentAsString().contains("event: message"));
    assertTrue(servletResponse.isCommitted());
  }

  @Test
  void passesTheExactApplicationContextAndRejectsNullImmediately() {
    AtomicReference<ApplicationContext> seen = new AtomicReference<>();
    var serverKit =
        McpServerKit.mcpServerKit("mvc-context", "1", ApplicationContext.class)
            .syncMethod(
                "context",
                (applicationContext, call, handlerContext) -> {
                  seen.set(applicationContext);
                  return new JsonRpcResultResponse(
                      call.id(), Map.of("value", applicationContext.value));
                })
            .build();
    var contextAdapter = new SpringWebMvcMcpAdapter<>(serverKit);
    var applicationContext = new ApplicationContext("exact");

    assertEquals(
        200,
        contextAdapter
            .handle(
                applicationContext,
                request("application/json", "context"),
                new MockHttpServletResponse(),
                body(true, "context"))
            .getStatusCode()
            .value());
    assertSame(applicationContext, seen.get());
    var discoveryRequest = request("application/json", "server/discover");
    var discoveryResponse = new MockHttpServletResponse();
    var discoveryBody = body(true, "server/discover");
    assertThrows(
        NullPointerException.class,
        () -> contextAdapter.handle(null, discoveryRequest, discoveryResponse, discoveryBody));
  }

  private static MockHttpServletRequest request(String accept, String method) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("POST");
    request.addHeader("Content-Type", "application/json");
    request.addHeader("Accept", accept);
    request.addHeader("MCP-Protocol-Version", McpProtocol.REVISION);
    request.addHeader("Mcp-Method", method);
    return request;
  }

  private static byte[] body(boolean request, String method) {
    return body(request, method, false);
  }

  private static byte[] body(boolean request, String method, boolean progress) {
    return ("{\"jsonrpc\":\"2.0\""
            + (request ? ",\"id\":1" : "")
            + ",\"method\":\""
            + method
            + "\",\"params\":{\"_meta\":{\""
            + McpProtocol.PROTOCOL_VERSION_KEY
            + "\":\""
            + McpProtocol.REVISION
            + (progress ? "\",\"progressToken\":\"progress-1" : "")
            + "\",\""
            + McpProtocol.CLIENT_CAPABILITIES_KEY
            + "\":{}}}}")
        .getBytes(StandardCharsets.UTF_8);
  }

  private record ApplicationContext(String value) {}
}
