package io.github.suppierk.mcp.javalin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class JavalinMcpAdapterTest {
  @Test
  void mapsJsonAcceptedAndEventStreamResponses() throws Exception {
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("javalin-test", "1", McpEmptyContext.class).build();
    JavalinMcpAdapter<McpEmptyContext> adapter =
        new JavalinMcpAdapter<>(new StreamableHttpMcpTransport<>(server));
    Javalin application =
        Javalin.create()
            .post("/mcp", context -> adapter.handle(McpEmptyContext.INSTANCE, context))
            .start(0);
    try {
      HttpResponse<String> json =
          send(application.port(), "application/json", "server/discover", true, false);
      HttpResponse<String> accepted =
          send(application.port(), "application/json", "notifications/ignored", false, false);
      HttpResponse<String> stream =
          send(application.port(), "application/json", "server/discover", true, true);
      HttpResponse<String> repeatedHeader =
          send(application.port(), "application/json", "server/discover", true, false, true);

      assertEquals(200, json.statusCode());
      assertTrue(json.body().contains(McpProtocol.REVISION));
      assertEquals(202, accepted.statusCode());
      assertEquals(200, stream.statusCode());
      assertTrue(stream.body().contains("event: message"));
      assertEquals(400, repeatedHeader.statusCode());
    } finally {
      application.stop();
    }
  }

  @Test
  void passesTheExactApplicationContextAndRejectsNullImmediately() throws Exception {
    AtomicReference<ApplicationContext> seen = new AtomicReference<>();
    var serverKit =
        McpServerKit.builder("javalin-context", "1", ApplicationContext.class)
            .syncMethod(
                "context",
                (applicationContext, call, handlerContext) -> {
                  seen.set(applicationContext);
                  return new JsonRpcResultResponse(
                      call.id(), Map.of("value", applicationContext.value));
                })
            .build();
    var adapter = new JavalinMcpAdapter<>(serverKit);
    var applicationContext = new ApplicationContext("exact");
    Javalin application =
        Javalin.create()
            .post("/mcp", context -> adapter.handle(applicationContext, context))
            .start(0);
    try {
      assertEquals(
          200, send(application.port(), "application/json", "context", true, false).statusCode());
      assertSame(applicationContext, seen.get());
      assertThrows(NullPointerException.class, () -> adapter.handle(null, null));
    } finally {
      application.stop();
    }
  }

  private static HttpResponse<String> send(
      int port, String accept, String method, boolean request, boolean progress) throws Exception {
    return send(port, accept, method, request, progress, false);
  }

  private static HttpResponse<String> send(
      int port,
      String accept,
      String method,
      boolean request,
      boolean progress,
      boolean repeatMethod)
      throws Exception {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/mcp"))
            .header("Content-Type", "application/json")
            .header("Accept", accept)
            .header("MCP-Protocol-Version", McpProtocol.REVISION)
            .header("Mcp-Method", method)
            .POST(HttpRequest.BodyPublishers.ofString(body(request, method, progress)));
    if (repeatMethod) {
      builder.header("mcp-method", method);
    }
    HttpRequest httpRequest = builder.build();
    return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
  }

  private static String body(boolean request, String method, boolean progress) {
    return "{\"jsonrpc\":\"2.0\""
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
        + "\":{}}}}";
  }

  private record ApplicationContext(String value) {}
}
