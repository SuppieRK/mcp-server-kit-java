package io.github.suppierk.mcp.spring.webflux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpCallToolResultResponse;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpInternalException;
import io.github.suppierk.mcp.server.McpServerKit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class ReactorFutureInteropTest {

  @Test
  void nonEmptyMonoProducesOneNonNullAsynchronousToolResult() {
    var kit =
        McpServerKit.mcpServerKit("reactor", "1", McpEmptyContext.class)
            .asyncTool(
                registration ->
                    registration
                        .name("reactor")
                        .inputSchema(Map.of("type", "object"))
                        .handler(
                            (applicationContext, request, handlerContext) ->
                                Mono.just(toolResult("reactor-value")).toFuture()))
            .build();

    JsonRpcMessage message = invoke(kit, toolCall(1));

    var response = assertInstanceOf(McpCallToolResultResponse.class, message);
    var result = assertInstanceOf(McpCallToolResult.class, response.result());
    assertEquals(1, result.content().size());
    assertEquals(
        "reactor-value", assertInstanceOf(McpTextContent.class, result.content().get(0)).text());
  }

  @Test
  void monoErrorCompletesTheFutureExceptionallyAndBecomesAnInternalErrorResponse() {
    var applicationFuture = new AtomicReference<CompletableFuture<McpCallToolResult>>();
    var sourceFailure = new IllegalStateException("reactor-failure");
    var kit =
        McpServerKit.mcpServerKit("reactor", "1", McpEmptyContext.class)
            .asyncTool(
                registration ->
                    registration
                        .name("reactor")
                        .inputSchema(Map.of("type", "object"))
                        .handler(
                            (applicationContext, request, handlerContext) -> {
                              CompletableFuture<McpCallToolResult> future =
                                  Mono.<McpCallToolResult>error(sourceFailure).toFuture();
                              applicationFuture.set(future);
                              return future;
                            }))
            .build();

    JsonRpcMessage message = invoke(kit, toolCall(2));

    assertTrue(applicationFuture.get().isCompletedExceptionally());
    var response = assertInstanceOf(JsonRpcErrorResponse.class, message);
    assertEquals(McpInternalException.ERROR_CODE, response.code());
    assertEquals("Internal error", response.message());
  }

  @Test
  void cancellingTheApplicationFutureCancelsTheReactorSubscription() throws Exception {
    var cancelled = new CountDownLatch(1);
    var applicationFuture = new AtomicReference<CompletableFuture<McpCallToolResult>>();
    var kit =
        McpServerKit.mcpServerKit("reactor", "1", McpEmptyContext.class)
            .asyncTool(
                registration ->
                    registration
                        .name("reactor")
                        .inputSchema(Map.of("type", "object"))
                        .handler(
                            (applicationContext, request, handlerContext) -> {
                              CompletableFuture<McpCallToolResult> future =
                                  Mono.<McpCallToolResult>never()
                                      .doOnCancel(cancelled::countDown)
                                      .toFuture();
                              applicationFuture.set(future);
                              return future;
                            }))
            .build();
    var subscriber = new RecordingSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, toolCall(3)).subscribe(subscriber);

    subscriber.subscription.cancel();

    assertTrue(cancelled.await(1, TimeUnit.SECONDS));
    assertTrue(applicationFuture.get().isCancelled());
    assertEquals(List.of(), subscriber.messages);
  }

  @Test
  void emptyMonoCompletesWithNullAndBecomesAnInternalErrorResponse() {
    var applicationFuture = new AtomicReference<CompletableFuture<McpCallToolResult>>();
    var kit =
        McpServerKit.mcpServerKit("reactor", "1", McpEmptyContext.class)
            .asyncTool(
                registration ->
                    registration
                        .name("reactor")
                        .inputSchema(Map.of("type", "object"))
                        .handler(
                            (applicationContext, request, handlerContext) -> {
                              CompletableFuture<McpCallToolResult> future =
                                  Mono.<McpCallToolResult>empty().toFuture();
                              applicationFuture.set(future);
                              return future;
                            }))
            .build();

    JsonRpcMessage message = invoke(kit, toolCall(4));

    assertEquals(null, applicationFuture.get().join());
    var response = assertInstanceOf(JsonRpcErrorResponse.class, message);
    assertEquals(McpInternalException.ERROR_CODE, response.code());
    assertEquals("Internal error", response.message());
  }

  private static McpCallToolResult toolResult(String value) {
    return new McpCallToolResult(List.of(new McpTextContent(value)));
  }

  private static JsonRpcRequest toolCall(int id) {
    return new JsonRpcRequest(
        id,
        McpCallToolRequest.METHOD_NAME,
        Map.of(
            "name",
            "reactor",
            "arguments",
            Map.of(),
            "_meta",
            Map.of(
                McpProtocol.PROTOCOL_VERSION_KEY,
                McpProtocol.REVISION,
                McpProtocol.CLIENT_CAPABILITIES_KEY,
                Map.of())));
  }

  private static JsonRpcMessage invoke(McpServerKit<McpEmptyContext> kit, JsonRpcRequest request) {
    var subscriber = new RecordingSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request).subscribe(subscriber);
    subscriber.completion.orTimeout(5, TimeUnit.SECONDS).join();
    assertEquals(1, subscriber.messages.size());
    return subscriber.messages.get(0);
  }

  private static final class RecordingSubscriber implements Flow.Subscriber<JsonRpcMessage> {
    private final List<JsonRpcMessage> messages = new ArrayList<>();
    private final CompletableFuture<Void> completion = new CompletableFuture<>();
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription value) {
      subscription = value;
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(JsonRpcMessage item) {
      messages.add(item);
    }

    @Override
    public void onError(Throwable throwable) {
      completion.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
      completion.complete(null);
    }
  }
}
