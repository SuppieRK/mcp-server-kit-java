package io.github.suppierk.mcp.spring.webflux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpHandlerContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

class SpringWebFluxMcpAdapterTest {
  private final SpringWebFluxMcpAdapter<McpEmptyContext> adapter =
      new SpringWebFluxMcpAdapter<>(
          new StreamableHttpMcpTransport<>(
              McpServerKit.builder("webflux-test", "1", McpEmptyContext.class).build()));

  @Test
  void mapsJsonRequests() {
    ServerResponse response =
        adapter
            .handle(McpEmptyContext.INSTANCE, request("application/json", "server/discover", true))
            .block();

    assertEquals(200, response.statusCode().value());
  }

  @Test
  void mapsAcceptedNotifications() {
    ServerResponse response =
        adapter
            .handle(
                McpEmptyContext.INSTANCE,
                request("application/json", "notifications/ignored", false))
            .block();

    assertEquals(202, response.statusCode().value());
  }

  @Test
  void preservesRepeatedHeaderValues() {
    MockServerRequest request =
        MockServerRequest.builder()
            .method(HttpMethod.POST)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("MCP-Protocol-Version", McpProtocol.REVISION)
            .header("Mcp-Method", "server/discover")
            .header("mcp-method", "server/discover")
            .body(Mono.just(body(true, "server/discover")));

    ServerResponse response = adapter.handle(McpEmptyContext.INSTANCE, request).block();

    assertEquals(400, response.statusCode().value());
  }

  @Test
  void mapsEventStreams() {
    ServerResponse response =
        adapter
            .handle(
                McpEmptyContext.INSTANCE,
                request("application/json", "server/discover", true, true))
            .block();

    assertEquals(200, response.statusCode().value());
    assertTrue(response.headers().getFirst("Content-Type").contains("text/event-stream"));
  }

  @Test
  void propagatesReactiveCancellationToTheApplicationHandler() throws Exception {
    var started = new CountDownLatch(1);
    var cancelled = new CountDownLatch(1);
    var handlerContext = new AtomicReference<McpHandlerContext>();
    var pending = new CompletableFuture<JsonRpcResultResponse>();
    var serverKit =
        McpServerKit.builder("webflux-cancellation", "1", McpEmptyContext.class)
            .asyncMethod(
                "work",
                (applicationContext, call, context) -> {
                  handlerContext.set(context);
                  context.cancellation().thenRun(cancelled::countDown);
                  started.countDown();
                  return pending;
                })
            .build();
    var cancellationAdapter = new SpringWebFluxMcpAdapter<>(serverKit);
    ServerResponse response =
        cancellationAdapter
            .handle(McpEmptyContext.INSTANCE, request("application/json", "work", true, true))
            .block();
    var exchange =
        MockServerWebExchange.from(MockServerHttpRequest.post("https://example.test/mcp").build());
    HandlerStrategies strategies = HandlerStrategies.withDefaults();
    ServerResponse.Context responseContext =
        new ServerResponse.Context() {
          @Override
          public List<HttpMessageWriter<?>> messageWriters() {
            return strategies.messageWriters();
          }

          @Override
          public List<ViewResolver> viewResolvers() {
            return strategies.viewResolvers();
          }
        };

    assertEquals(null, handlerContext.get());
    var body = response.writeTo(exchange, responseContext).subscribe();
    assertTrue(started.await(1, TimeUnit.SECONDS));
    body.dispose();

    assertTrue(cancelled.await(1, TimeUnit.SECONDS));
    assertTrue(handlerContext.get().isCancelled());
    assertTrue(pending.isCancelled());
  }

  @Test
  void passesTheExactApplicationContextAndRejectsNullImmediately() {
    AtomicReference<ApplicationContext> seen = new AtomicReference<>();
    var serverKit =
        McpServerKit.builder("webflux-context", "1", ApplicationContext.class)
            .syncMethod(
                "context",
                (applicationContext, call, handlerContext) -> {
                  seen.set(applicationContext);
                  return new JsonRpcResultResponse(
                      call.id(), Map.of("value", applicationContext.value));
                })
            .build();
    var contextAdapter = new SpringWebFluxMcpAdapter<>(serverKit);
    var applicationContext = new ApplicationContext("exact");

    assertEquals(
        200,
        contextAdapter
            .handle(applicationContext, request("application/json", "context", true))
            .block()
            .statusCode()
            .value());
    assertSame(applicationContext, seen.get());
    assertThrows(
        NullPointerException.class,
        () -> contextAdapter.handle(null, request("application/json", "server/discover", true)));
  }

  private static MockServerRequest request(String accept, String method, boolean request) {
    return request(accept, method, request, false);
  }

  private static MockServerRequest request(
      String accept, String method, boolean request, boolean progress) {
    MockServerRequest.Builder builder =
        MockServerRequest.builder()
            .method(HttpMethod.POST)
            .header("Content-Type", "application/json")
            .header("Accept", accept)
            .header("MCP-Protocol-Version", McpProtocol.REVISION)
            .header("Mcp-Method", method);
    return builder.body(Mono.just(body(request, method, progress)));
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
