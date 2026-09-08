package io.github.suppierk.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.JsonTestValues;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcNotification;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpPromptListChangedNotification;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.protocol.McpReadResourceResult;
import io.github.suppierk.mcp.protocol.McpResource;
import io.github.suppierk.mcp.protocol.McpResourceListChangedNotification;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotification;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotificationParams;
import io.github.suppierk.mcp.protocol.McpSubscriptionNotification;
import io.github.suppierk.mcp.protocol.McpSubscriptionsAcknowledgedNotification;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenResultResponse;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTextResourceContents;
import io.github.suppierk.mcp.protocol.McpToolListChangedNotification;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class McpSubscriptionNotificationTest {
  private static final JsonNodeFactory JSON = JsonNodeFactory.instance;
  private static final URI RESOURCE = URI.create("file:///aggregate");

  @Test
  void acknowledgesBeforeConcurrentInvalidations() throws Exception {
    var running = new AtomicBoolean(true);
    var emitting = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (var server = server()) {
      var warmup = listen(server, -1, filters(true, false, false, List.of()));
      server.emit(toolChanged());
      warmup.cancel();
      var producer =
          executor.submit(
              () -> {
                emitting.countDown();
                while (running.get()) {
                  server.emit(toolChanged());
                  Thread.yield();
                }
              });
      try {
        assertTrue(emitting.await(5, TimeUnit.SECONDS));
        var uris = IntStream.range(0, 100_000).mapToObj(i -> "file:///resource/" + i).toList();
        for (int id = 1; id <= 10; id++) {
          var first = new CompletableFuture<JsonRpcMessage>();
          var subscription = new AtomicReference<Flow.Subscription>();
          var params = JSON.objectNode();
          params.set("notifications", filters(true, false, false, uris));
          params
              .putObject("_meta")
              .put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION)
              .putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
          server
              .handle(
                  McpEmptyContext.INSTANCE,
                  new JsonRpcRequest(
                      JsonTestValues.value(JSON.numberNode(id)),
                      "subscriptions/listen",
                      JsonTestValues.object(params)))
              .subscribe(
                  new Flow.Subscriber<JsonRpcMessage>() {
                    public void onSubscribe(Flow.Subscription value) {
                      subscription.set(value);
                    }

                    public void onNext(JsonRpcMessage value) {
                      first.complete(value);
                    }

                    public void onError(Throwable failure) {
                      first.completeExceptionally(failure);
                    }

                    public void onComplete() {}
                  });
          try {
            subscription.get().request(1);
            assertInstanceOf(
                McpSubscriptionsAcknowledgedNotification.class, first.get(5, TimeUnit.SECONDS));
          } finally {
            subscription.get().cancel();
          }
        }
      } finally {
        running.set(false);
        producer.get(5, TimeUnit.SECONDS);
      }
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void toolsOnlyKitsCanSubscribeToToolChanges() {
    try (var server =
        McpServerKit.mcpServerKit("tools", "1", McpEmptyContext.class)
            .syncTool(
                registration ->
                    registration
                        .name("hello")
                        .inputSchema(JsonTestValues.object(JSON.objectNode().put("type", "object")))
                        .handler(
                            (applicationContext, parameters, handlerContext) ->
                                new McpCallToolResult(
                                    List.of(new McpTextContent("Hello, World!")))))
            .build()) {
      var subscriber = listen(server, 1, filters(true, false, false, List.of()));
      server.emit(toolChanged());
      subscriber.request(1);

      assertEquals(
          List.of("notifications/tools/list_changed"), methodsAfterAcknowledgement(subscriber));
    }
  }

  @Test
  void exposesOnlyTheFourSubscriptionInvalidations() throws Exception {
    assertTrue(McpSubscriptionNotification.class.isSealed());
    assertEquals(
        Set.of(
            McpToolListChangedNotification.class,
            McpPromptListChangedNotification.class,
            McpResourceListChangedNotification.class,
            McpResourceUpdatedNotification.class),
        Arrays.stream(McpSubscriptionNotification.class.getPermittedSubclasses())
            .collect(Collectors.toSet()));
    assertEquals(
        McpSubscriptionNotification.class,
        McpServerKit.class.getMethod("emit", McpSubscriptionNotification.class)
            .getParameterTypes()[0]);
    assertEquals(
        1,
        Arrays.stream(McpServerKit.class.getMethods())
            .filter(method -> method.getName().equals("emit"))
            .count());
  }

  @Test
  void routesOnlyMatchingInvalidationsAndUsesExactResourceUris() {
    var server = server();
    var tools = listen(server, 1, filters(true, false, false, List.of()));
    var prompts = listen(server, 2, filters(false, true, false, List.of()));
    var resources = listen(server, 3, filters(false, false, true, List.of(RESOURCE.toString())));

    server.emit(toolChanged());
    server.emit(promptChanged());
    server.emit(resourceListChanged());
    server.emit(resourceUpdated(RESOURCE));
    server.emit(resourceUpdated(URI.create("file:///aggregate/child")));
    tools.request(Long.MAX_VALUE);
    prompts.request(Long.MAX_VALUE);
    resources.request(Long.MAX_VALUE);

    assertEquals(List.of("notifications/tools/list_changed"), methodsAfterAcknowledgement(tools));
    assertEquals(
        List.of("notifications/prompts/list_changed"), methodsAfterAcknowledgement(prompts));
    assertEquals(
        List.of("notifications/resources/list_changed", "notifications/resources/updated"),
        methodsAfterAcknowledgement(resources));
    assertEquals(1, subscriptionId(tools.messages.get(1)).intValue());
    assertEquals(2, subscriptionId(prompts.messages.get(1)).intValue());
    assertEquals(3, subscriptionId(resources.messages.get(1)).intValue());
    server.close();
  }

  @Test
  void coalescesEachLogicalKeyAndMovesReplacementsToTheTail() {
    var server = server();
    var subscriber =
        listen(server, 10, filters(true, true, true, List.of(RESOURCE.toString(), "file:///two")));

    server.emit(toolChanged());
    server.emit(promptChanged());
    server.emit(toolChanged());
    server.emit(resourceUpdated(RESOURCE));
    server.emit(resourceUpdated(URI.create("file:///two")));
    server.emit(resourceUpdated(RESOURCE));
    server.emit(resourceListChanged());
    subscriber.request(Long.MAX_VALUE);

    assertEquals(
        List.of(
            "notifications/prompts/list_changed",
            "notifications/tools/list_changed",
            "notifications/resources/updated:file:///two",
            "notifications/resources/updated:file:///aggregate",
            "notifications/resources/list_changed"),
        keyedMessagesAfterAcknowledgement(subscriber));
    server.close();
  }

  @Test
  void doesNotReplayAcrossCancellationKitsOrLaterSubscriptions() {
    var first = server();
    var second = server();
    first.emit(toolChanged());
    var cancelled = listen(first, 20, filters(true, true, false, List.of()));
    var otherKit = listen(second, 21, filters(true, false, false, List.of()));

    first.emit(toolChanged());
    first.emit(promptChanged());
    cancelled.cancel();
    otherKit.request(Long.MAX_VALUE);
    assertEquals(List.of(), methodsAfterAcknowledgement(otherKit));

    var replacement = listen(first, 22, filters(true, true, false, List.of()));
    replacement.request(Long.MAX_VALUE);
    assertEquals(List.of(), methodsAfterAcknowledgement(replacement));
    first.close();
    second.close();
  }

  @Test
  void gracefulCloseDiscardsInvalidationsAndWaitsForTerminalDemand() {
    var server = server();
    var subscriber = listen(server, 30, filters(true, true, false, List.of()));
    server.emit(toolChanged());
    server.emit(promptChanged());

    server.close();

    assertEquals(1, subscriber.messages.size());
    assertFalse(subscriber.completion.isDone());
    subscriber.request(1);
    var terminal =
        assertInstanceOf(McpSubscriptionsListenResultResponse.class, subscriber.messages.get(1));
    assertEquals(30, JsonTestValues.json(terminal.id()).intValue());
    assertEquals(30, JsonTestValues.json(terminal.result().meta().subscriptionId()).intValue());
    assertEquals("complete", terminal.result().resultType());
    subscriber.completion.orTimeout(5, TimeUnit.SECONDS).join();
    assertThrows(IllegalStateException.class, () -> server.emit(toolChanged()));
  }

  @Test
  void gracefulCloseRetainsItsTerminalResultAfterLaterCancellation() {
    var server = server();
    var subscriber = listen(server, 31, filters(true, false, false, List.of()));

    server.close();
    subscriber.cancel();
    subscriber.request(1);

    var terminal =
        assertInstanceOf(McpSubscriptionsListenResultResponse.class, subscriber.messages.get(1));
    assertEquals("complete", terminal.result().resultType());
    subscriber.completion.orTimeout(5, TimeUnit.SECONDS).join();
  }

  @Test
  void subscriptionFailureDiscardsPendingInvalidations() {
    var server = server();
    var subscriber = listen(server, 32, filters(true, true, false, List.of()));
    server.emit(toolChanged());

    subscriber.request(0);

    assertInstanceOf(
        IllegalArgumentException.class,
        assertThrows(CompletionException.class, subscriber.completion::join).getCause());
    server.emit(promptChanged());
    subscriber.request(Long.MAX_VALUE);
    assertEquals(1, subscriber.messages.size());
    server.close();
  }

  @Test
  void concurrentEmissionStillRetainsOnlyOneInvalidationPerKey() {
    var server = server();
    var subscriber = listen(server, 40, filters(true, true, true, List.of(RESOURCE.toString())));
    ExecutorService executor = Executors.newFixedThreadPool(4);
    try {
      CompletableFuture.allOf(
              CompletableFuture.runAsync(() -> emitRepeatedly(server, toolChanged()), executor),
              CompletableFuture.runAsync(() -> emitRepeatedly(server, promptChanged()), executor),
              CompletableFuture.runAsync(
                  () -> emitRepeatedly(server, resourceListChanged()), executor),
              CompletableFuture.runAsync(
                  () -> emitRepeatedly(server, resourceUpdated(RESOURCE)), executor))
          .orTimeout(5, TimeUnit.SECONDS)
          .join();
    } finally {
      executor.shutdownNow();
    }

    subscriber.request(Long.MAX_VALUE);

    assertEquals(4, methodsAfterAcknowledgement(subscriber).size());
    assertEquals(
        Set.of(
            "notifications/tools/list_changed",
            "notifications/prompts/list_changed",
            "notifications/resources/list_changed",
            "notifications/resources/updated"),
        Set.copyOf(methodsAfterAcknowledgement(subscriber)));
    server.close();
  }

  private static void emitRepeatedly(
      McpServerKit<McpEmptyContext> server, McpSubscriptionNotification notification) {
    for (int index = 0; index < 100; index++) {
      server.emit(notification);
    }
  }

  private static McpServerKit<McpEmptyContext> server() {
    return McpServerKit.mcpServerKit("subscriptions", "1", McpEmptyContext.class)
        .syncResource(
            new McpResource(RESOURCE, "aggregate"),
            (applicationContext, request, handlerContext) ->
                new McpReadResourceResult(
                    List.of(new McpTextResourceContents(request.uri(), "resource"))))
        .build();
  }

  private static ObjectNode filters(
      boolean tools, boolean prompts, boolean resources, List<String> resourceUris) {
    ObjectNode filters = JSON.objectNode();
    filters.put("toolsListChanged", tools);
    filters.put("promptsListChanged", prompts);
    filters.put("resourcesListChanged", resources);
    filters
        .putArray("resourceSubscriptions")
        .addAll(resourceUris.stream().map(JSON::textNode).toList());
    return filters;
  }

  private static TestSubscriber listen(
      McpServerKit<McpEmptyContext> server, int id, ObjectNode filters) {
    ObjectNode params = JSON.objectNode();
    params.set("notifications", filters);
    ObjectNode metadata = params.putObject("_meta");
    metadata.put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION);
    metadata.putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    var subscriber = new TestSubscriber();
    server
        .handle(
            McpEmptyContext.INSTANCE,
            new JsonRpcRequest(
                JsonTestValues.value(JSON.numberNode(id)),
                "subscriptions/listen",
                JsonTestValues.object(params)))
        .subscribe(subscriber);
    subscriber.request(1);
    assertInstanceOf(McpSubscriptionsAcknowledgedNotification.class, subscriber.messages.get(0));
    return subscriber;
  }

  private static List<String> methodsAfterAcknowledgement(TestSubscriber subscriber) {
    return subscriber.messages.stream()
        .skip(1)
        .map(JsonRpcNotification.class::cast)
        .map(JsonRpcNotification::method)
        .toList();
  }

  private static List<String> keyedMessagesAfterAcknowledgement(TestSubscriber subscriber) {
    return subscriber.messages.stream()
        .skip(1)
        .map(JsonRpcNotification.class::cast)
        .map(
            notification ->
                notification.method().equals("notifications/resources/updated")
                    ? notification.method() + ":" + notification.params().get("uri")
                    : notification.method())
        .toList();
  }

  private static Number subscriptionId(JsonRpcMessage message) {
    return (Number)
        ((Map<?, ?>) assertInstanceOf(JsonRpcNotification.class, message).params().get("_meta"))
            .get("io.modelcontextprotocol/subscriptionId");
  }

  private static McpToolListChangedNotification toolChanged() {
    return new McpToolListChangedNotification(Optional.empty());
  }

  private static McpPromptListChangedNotification promptChanged() {
    return new McpPromptListChangedNotification(Optional.empty());
  }

  private static McpResourceListChangedNotification resourceListChanged() {
    return new McpResourceListChangedNotification(Optional.empty());
  }

  private static McpResourceUpdatedNotification resourceUpdated(URI uri) {
    return new McpResourceUpdatedNotification(
        new McpResourceUpdatedNotificationParams(Optional.empty(), uri));
  }

  private static final class TestSubscriber implements Flow.Subscriber<JsonRpcMessage> {
    private final List<JsonRpcMessage> messages = new ArrayList<>();
    private final CompletableFuture<Void> completion = new CompletableFuture<>();
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription value) {
      subscription = value;
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

    private void request(long count) {
      subscription.request(count);
    }

    private void cancel() {
      subscription.cancel();
    }
  }
}
