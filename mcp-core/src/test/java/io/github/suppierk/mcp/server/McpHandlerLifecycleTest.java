package io.github.suppierk.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.JsonTestValues;
import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpProgressNotification;
import io.github.suppierk.mcp.protocol.McpProtocol;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class McpHandlerLifecycleTest {
  private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

  @Test
  void synchronousHandlerRunsOnceInlineAfterFirstPositiveDemand() {
    var calls = new AtomicInteger();
    var invocationThread = new AtomicReference<Thread>();
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .syncMethod(
                "example/run",
                (applicationContext, request, handlerContext) -> {
                  calls.incrementAndGet();
                  invocationThread.set(Thread.currentThread());
                  return response(request, "done");
                })
            .build();
    var subscriber = new ManualSubscriber();

    kit.handle(McpEmptyContext.INSTANCE, request(1, "example/run")).subscribe(subscriber);

    assertEquals(0, calls.get());
    Thread demandThread = Thread.currentThread();
    subscriber.subscription.request(1);
    subscriber.subscription.request(1);

    assertEquals(1, calls.get());
    assertSame(demandThread, invocationThread.get());
    assertEquals(1, subscriber.messages.size());
    assertEquals(
        "done",
        JsonTestValues.json(((JsonRpcResultResponse) subscriber.messages.get(0)).result())
            .path("value")
            .textValue());
    assertEquals(null, subscriber.termination.join());
  }

  @Test
  void asynchronousHandlerUsesApplicationFutureWithoutScheduling() {
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var invocationThread = new AtomicReference<Thread>();
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod(
                "example/run",
                (applicationContext, request, handlerContext) -> {
                  invocationThread.set(Thread.currentThread());
                  return applicationFuture;
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(2, "example/run")).subscribe(subscriber);

    Thread demandThread = Thread.currentThread();
    subscriber.subscription.request(1);

    assertSame(demandThread, invocationThread.get());
    assertEquals(List.of(), subscriber.messages);

    applicationFuture.complete(
        new JsonRpcResultResponse(
            JsonTestValues.value(JSON.numberNode(2)),
            JsonTestValues.value(JSON.objectNode().put("value", "async"))));

    assertEquals(1, subscriber.messages.size());
    assertEquals(
        "async",
        JsonTestValues.json(((JsonRpcResultResponse) subscriber.messages.get(0)).result())
            .path("value")
            .textValue());
    assertEquals(null, subscriber.termination.join());
  }

  @Test
  void builderExposesOnlyExplicitSynchronousAndAsynchronousHandlerAdapters() {
    Set<String> methods =
        Arrays.stream(McpServerKit.Builder.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());

    assertEquals(
        Set.of(
            "asyncCompletion",
            "asyncMethod",
            "asyncPrompt",
            "asyncResource",
            "asyncResourceTemplate",
            "asyncTool",
            "build",
            "description",
            "icon",
            "instructions",
            "syncCompletion",
            "syncMethod",
            "syncPrompt",
            "syncResource",
            "syncResourceTemplate",
            "syncTool",
            "title",
            "websiteUrl"),
        methods);
  }

  @Test
  void cancellationPreventsInvocationOrCancelsTheApplicationFutureAfterContextSignal() {
    var calls = new AtomicInteger();
    var handlerContext = new AtomicReference<McpHandlerContext>();
    var contextWasCancelledFirst = new AtomicBoolean();
    var applicationFuture =
        new CompletableFuture<JsonRpcResultResponse>() {
          @Override
          public boolean cancel(boolean mayInterruptIfRunning) {
            contextWasCancelledFirst.set(
                handlerContext.get().isCancelled()
                    && handlerContext.get().cancellation().toCompletableFuture().isDone()
                    && !mayInterruptIfRunning);
            return super.cancel(mayInterruptIfRunning);
          }
        };
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod(
                "example/run",
                (applicationContext, request, context) -> {
                  calls.incrementAndGet();
                  handlerContext.set(context);
                  return applicationFuture;
                })
            .build();

    var beforeDemand = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(3, "example/run")).subscribe(beforeDemand);
    beforeDemand.subscription.cancel();
    assertEquals(0, calls.get());

    var active = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(4, "example/run")).subscribe(active);
    active.subscription.request(1);
    CompletionStage<Void> cancellation = handlerContext.get().cancellation();
    assertSame(cancellation, handlerContext.get().cancellation());

    active.subscription.cancel();

    assertEquals(1, calls.get());
    assertEquals(true, handlerContext.get().isCancelled());
    assertEquals(true, cancellation.toCompletableFuture().isDone());
    assertEquals(true, applicationFuture.isCancelled());
    assertEquals(true, contextWasCancelledFirst.get());
    assertEquals(List.of(), active.messages);
  }

  @Test
  void invalidAsynchronousOutcomesBecomeGenericInternalErrors() {
    var cancelled = new CompletableFuture<JsonRpcResultResponse>();
    cancelled.cancel(false);
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod("null/future", (applicationContext, request, context) -> null)
            .asyncMethod(
                "null/result",
                (applicationContext, request, context) -> CompletableFuture.completedFuture(null))
            .asyncMethod("cancelled", (applicationContext, request, context) -> cancelled)
            .build();

    assertInternalError(kit, request(5, "null/future"));
    assertInternalError(kit, request(6, "null/result"));
    assertInternalError(kit, request(7, "cancelled"));
  }

  @Test
  void handlerContextPublishesAllProgressFormsBeforeTheTerminalResponse() {
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .syncMethod(
                "progress",
                (applicationContext, request, context) -> {
                  context.progress(1.0);
                  context.progress(2.0, "");
                  context.progress(3.0, 10.0);
                  context.progress(4.0, 20.0, "four");
                  return response(request, "done");
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(8, "progress", "token-8")).subscribe(subscriber);

    subscriber.subscription.request(Long.MAX_VALUE);

    assertEquals(5, subscriber.messages.size());
    var first = ((McpProgressNotification) subscriber.messages.get(0)).params();
    var second = ((McpProgressNotification) subscriber.messages.get(1)).params();
    var third = ((McpProgressNotification) subscriber.messages.get(2)).params();
    var fourth = ((McpProgressNotification) subscriber.messages.get(3)).params();
    assertEquals(1.0, first.progress());
    assertEquals(Optional.empty(), first.total());
    assertEquals(Optional.empty(), first.message());
    assertEquals(Optional.of(""), second.message());
    assertEquals(Optional.of(10.0), third.total());
    assertEquals(Optional.of(20.0), fourth.total());
    assertEquals(Optional.of("four"), fourth.message());
    assertEquals("token-8", JsonTestValues.json(fourth.progressToken()).textValue());
    assertEquals(
        "done",
        JsonTestValues.json(((JsonRpcResultResponse) subscriber.messages.get(4)).result())
            .path("value")
            .textValue());
  }

  @Test
  void progressRetainsOnlyTheLatestPendingValueWithoutOvertakingTerminalOrder() {
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .syncMethod(
                "progress",
                (applicationContext, request, context) -> {
                  context.progress(1.0);
                  context.progress(2.0);
                  context.progress(3.0);
                  return response(request, "done");
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(9, "progress", "token-9")).subscribe(subscriber);

    subscriber.subscription.request(1);
    assertEquals(1.0, ((McpProgressNotification) subscriber.messages.get(0)).params().progress());

    subscriber.subscription.request(1);
    assertEquals(2, subscriber.messages.size());
    assertEquals(3.0, ((McpProgressNotification) subscriber.messages.get(1)).params().progress());
    assertEquals(false, subscriber.termination.isDone());

    subscriber.subscription.request(1);
    assertEquals(3, subscriber.messages.size());
    assertEquals(
        "done",
        JsonTestValues.json(((JsonRpcResultResponse) subscriber.messages.get(2)).result())
            .path("value")
            .textValue());
    assertEquals(null, subscriber.termination.join());
  }

  @Test
  void completedResponseSurvivesLaterDownstreamCancellationUntilDemandArrives() {
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .syncMethod(
                "progress",
                (applicationContext, request, context) -> {
                  context.progress(1.0);
                  return response(request, "done");
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(22, "progress", "token-22")).subscribe(subscriber);

    subscriber.subscription.request(1);
    subscriber.subscription.cancel();
    subscriber.subscription.request(1);

    assertEquals(2, subscriber.messages.size());
    assertEquals(1.0, ((McpProgressNotification) subscriber.messages.get(0)).params().progress());
    assertEquals(
        "done",
        JsonTestValues.json(((JsonRpcResultResponse) subscriber.messages.get(1)).result())
            .path("value")
            .textValue());
    assertEquals(null, subscriber.termination.join());
  }

  @Test
  void handlerContextValidatesWithoutATokenAndExposesReadOnlyCancellation() {
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var handlerContext = new AtomicReference<McpHandlerContext>();
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod(
                "context",
                (applicationContext, request, context) -> {
                  handlerContext.set(context);
                  return applicationFuture;
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(10, "context")).subscribe(subscriber);
    subscriber.subscription.request(1);
    McpHandlerContext context = handlerContext.get();
    CompletionStage<Void> cancellation = context.cancellation();

    assertSame(cancellation, context.cancellation());
    assertEquals(true, cancellation.toCompletableFuture().complete(null));
    assertEquals(false, context.isCancelled());
    assertEquals(false, cancellation.toCompletableFuture().isDone());
    context.progress(-2.0);
    context.progress(-1.0, -10.0);
    context.progress(0.0, "");
    assertThrows(IllegalArgumentException.class, () -> context.progress(Double.NaN));
    assertThrows(
        IllegalArgumentException.class, () -> context.progress(1.0, Double.POSITIVE_INFINITY));
    assertThrows(IllegalArgumentException.class, () -> context.progress(0.0));
    assertThrows(IllegalArgumentException.class, () -> context.progress(1.0, (String) null));

    applicationFuture.complete(response(request(10, "context"), "done"));

    assertEquals(1, subscriber.messages.size());
    assertThrows(IllegalStateException.class, () -> context.progress(2.0));
  }

  @Test
  void handlerFailuresUseOnlyTheApprovedWrapperClassificationBoundary() {
    var directError = new AssertionError("direct");
    var wrappedError = new AssertionError("wrapped");
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .syncMethod(
                "protocol",
                (applicationContext, request, context) -> {
                  throw new McpInvalidParamsException("invalid", Optional.empty());
                })
            .asyncMethod(
                "wrapped/protocol",
                (applicationContext, request, context) ->
                    CompletableFuture.failedFuture(
                        new CompletionException(
                            new ExecutionException(
                                new McpMethodNotFoundException("missing", Optional.empty())))))
            .syncMethod(
                "nested/protocol",
                (applicationContext, request, context) -> {
                  throw new IllegalStateException(
                      "outer", new McpInvalidParamsException("hidden", Optional.empty()));
                })
            .syncMethod(
                "error",
                (applicationContext, request, context) -> {
                  throw directError;
                })
            .asyncMethod(
                "wrapped/error",
                (applicationContext, request, context) ->
                    CompletableFuture.failedFuture(
                        new CompletionException(new ExecutionException(wrappedError))))
            .build();

    assertErrorCode(kit, request(11, "protocol"), McpInvalidParamsException.ERROR_CODE);
    assertErrorCode(kit, request(12, "wrapped/protocol"), McpMethodNotFoundException.ERROR_CODE);
    assertErrorCode(kit, request(13, "nested/protocol"), McpInternalException.ERROR_CODE);
    assertSame(directError, failure(kit, request(14, "error")));
    assertSame(wrappedError, failure(kit, request(15, "wrapped/error")));
  }

  @Test
  void cancellationWhileTheHandlerConstructsItsFuturePropagatesAfterReturn() throws Exception {
    var entered = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var handlerContext = new AtomicReference<McpHandlerContext>();
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod(
                "slow/construction",
                (applicationContext, request, context) -> {
                  handlerContext.set(context);
                  entered.countDown();
                  try {
                    release.await(5, TimeUnit.SECONDS);
                  } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                  }
                  return applicationFuture;
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(16, "slow/construction")).subscribe(subscriber);
    Thread demandThread = new Thread(() -> subscriber.subscription.request(1));
    demandThread.start();
    assertEquals(true, entered.await(5, TimeUnit.SECONDS));

    subscriber.subscription.cancel();

    assertEquals(true, handlerContext.get().isCancelled());
    assertEquals(true, handlerContext.get().cancellation().toCompletableFuture().isDone());
    release.countDown();
    demandThread.join(TimeUnit.SECONDS.toMillis(5));
    assertEquals(false, demandThread.isAlive());
    assertEquals(true, applicationFuture.isCancelled());
    assertEquals(List.of(), subscriber.messages);
  }

  @Test
  void terminalCompletionAndCancellationHaveExactlyOneWinner() throws Exception {
    for (int iteration = 0; iteration < 20; iteration++) {
      var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
      var handlerContext = new AtomicReference<McpHandlerContext>();
      var kit =
          McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
              .asyncMethod(
                  "race",
                  (applicationContext, request, context) -> {
                    handlerContext.set(context);
                    return applicationFuture;
                  })
              .build();
      var subscriber = new ManualSubscriber();
      kit.handle(McpEmptyContext.INSTANCE, request(17, "race")).subscribe(subscriber);
      subscriber.subscription.request(1);
      var start = new CountDownLatch(1);
      Thread completion =
          new Thread(
              () -> {
                await(start);
                applicationFuture.complete(response(request(17, "race"), "done"));
              });
      Thread cancellation =
          new Thread(
              () -> {
                await(start);
                subscriber.subscription.cancel();
              });
      completion.start();
      cancellation.start();
      start.countDown();
      completion.join(TimeUnit.SECONDS.toMillis(5));
      cancellation.join(TimeUnit.SECONDS.toMillis(5));

      boolean responseWon = subscriber.messages.size() == 1;
      boolean cancellationWon = handlerContext.get().isCancelled();
      assertEquals(true, responseWon ^ cancellationWon);
    }
  }

  @Test
  void unexpectedHandlerExceptionIsLoggedOnceWithoutParametersOrAnUnboundedId() {
    var records = new ArrayList<LogRecord>();
    Handler handler =
        new Handler() {
          @Override
          public void publish(LogRecord logRecord) {
            records.add(logRecord);
          }

          @Override
          public void flush() {
            // This handler records directly into an in-memory list.
          }

          @Override
          public void close() {
            // The recording handler owns no external resources.
          }
        };
    handler.setLevel(Level.ALL);
    Logger logger = Logger.getLogger(McpServerKit.class.getName());
    Level previousLevel = logger.getLevel();
    boolean previousParentHandlers = logger.getUseParentHandlers();
    logger.setLevel(Level.ALL);
    logger.setUseParentHandlers(false);
    logger.addHandler(handler);
    var failure = new IllegalStateException("private detail");
    String requestId = "line\n" + "x".repeat(300);
    ObjectNode params = JSON.objectNode().put("secret", "must-not-be-logged");
    ObjectNode metadata = params.putObject("_meta");
    metadata.put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION);
    metadata.putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    var request =
        new JsonRpcRequest(
            JsonTestValues.value(JSON.textNode(requestId)),
            "logged/method",
            JsonTestValues.object(params));
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .syncMethod(
                "logged/method",
                (applicationContext, suppliedRequest, context) -> {
                  throw failure;
                })
            .build();

    try {
      assertInternalError(kit, request);
    } finally {
      logger.removeHandler(handler);
      logger.setUseParentHandlers(previousParentHandlers);
      logger.setLevel(previousLevel);
    }

    assertEquals(1, records.size());
    LogRecord logRecord = records.get(0);
    assertSame(failure, logRecord.getThrown());
    assertEquals(true, logRecord.getMessage().contains("method=logged/method"));
    assertEquals(false, logRecord.getMessage().contains("must-not-be-logged"));
    String renderedId =
        logRecord
            .getMessage()
            .substring(
                logRecord.getMessage().indexOf("requestId=") + "requestId=".length(),
                logRecord.getMessage().length() - 1);
    assertEquals(true, renderedId.length() <= 256);
    assertEquals(true, renderedId.contains("\\n"));
    assertEquals(false, renderedId.contains("\n"));
  }

  @Test
  void closeCancelsActiveFiniteWorkAndRejectsNewWork() {
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var handlerContext = new AtomicReference<McpHandlerContext>();
    var calls = new AtomicInteger();
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod(
                "close",
                (applicationContext, request, context) -> {
                  calls.incrementAndGet();
                  handlerContext.set(context);
                  return applicationFuture;
                })
            .build();
    var active = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(18, "close")).subscribe(active);
    active.subscription.request(1);

    kit.close();

    assertEquals(true, handlerContext.get().isCancelled());
    assertEquals(true, applicationFuture.isCancelled());
    assertEquals(null, active.termination.join());
    assertEquals(List.of(), active.messages);

    var rejected = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(19, "close")).subscribe(rejected);
    rejected.subscription.request(1);
    assertEquals(1, calls.get());
    assertEquals(true, rejected.termination.join() instanceof IllegalStateException);
  }

  @Test
  void concurrentProgressCallsAreLinearizedIntoIncreasingSignals() {
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var handlerContext = new AtomicReference<McpHandlerContext>();
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod(
                "concurrent/progress",
                (applicationContext, request, context) -> {
                  handlerContext.set(context);
                  return applicationFuture;
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(20, "concurrent/progress", "concurrent-token"))
        .subscribe(subscriber);
    subscriber.subscription.request(1);

    CompletableFuture<Throwable> first =
        CompletableFuture.supplyAsync(() -> progressFailure(handlerContext.get(), 1.0));
    CompletableFuture<Throwable> second =
        CompletableFuture.supplyAsync(() -> progressFailure(handlerContext.get(), 2.0));
    CompletableFuture.allOf(first, second).join();
    applicationFuture.complete(response(request(20, "concurrent/progress"), "done"));
    subscriber.subscription.request(Long.MAX_VALUE);

    List<Double> values =
        subscriber.messages.stream()
            .filter(McpProgressNotification.class::isInstance)
            .map(McpProgressNotification.class::cast)
            .map(notification -> notification.params().progress())
            .toList();
    for (int index = 1; index < values.size(); index++) {
      assertEquals(true, values.get(index) > values.get(index - 1));
    }
    assertEquals(2.0, values.get(values.size() - 1));
    long rejectedCalls =
        Stream.of(first.join(), second.join())
            .filter(IllegalArgumentException.class::isInstance)
            .count();
    assertEquals(true, rejectedCalls <= 1);
    assertEquals(
        "done",
        JsonTestValues.json(
                ((JsonRpcResultResponse) subscriber.messages.get(subscriber.messages.size() - 1))
                    .result())
            .path("value")
            .textValue());
  }

  @Test
  void cancellationDiscardsPendingProgressAndTheLaterResult() {
    var applicationFuture = new CompletableFuture<JsonRpcResultResponse>();
    var kit =
        McpServerKit.mcpServerKit("lifecycle", "1", McpEmptyContext.class)
            .asyncMethod(
                "cancel/progress",
                (applicationContext, request, context) -> {
                  context.progress(1.0);
                  context.progress(2.0);
                  return applicationFuture;
                })
            .build();
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request(21, "cancel/progress", "cancel-token"))
        .subscribe(subscriber);
    subscriber.subscription.request(1);
    assertEquals(1, subscriber.messages.size());

    subscriber.subscription.cancel();
    applicationFuture.complete(response(request(21, "cancel/progress"), "late"));
    subscriber.subscription.request(Long.MAX_VALUE);

    assertEquals(1, subscriber.messages.size());
    assertEquals(1.0, ((McpProgressNotification) subscriber.messages.get(0)).params().progress());
  }

  private static Throwable progressFailure(McpHandlerContext context, double value) {
    try {
      context.progress(value);
      return null;
    } catch (IllegalArgumentException exception) {
      return exception;
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(exception);
    }
  }

  private static void assertErrorCode(
      McpServerKit<McpEmptyContext> kit, JsonRpcRequest request, int code) {
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request).subscribe(subscriber);
    subscriber.subscription.request(1);

    assertEquals(code, ((JsonRpcErrorResponse) subscriber.messages.get(0)).code());
  }

  private static Throwable failure(McpServerKit<McpEmptyContext> kit, JsonRpcRequest request) {
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request).subscribe(subscriber);
    subscriber.subscription.request(1);
    return subscriber.termination.join();
  }

  private static void assertInternalError(
      McpServerKit<McpEmptyContext> kit, JsonRpcRequest request) {
    var subscriber = new ManualSubscriber();
    kit.handle(McpEmptyContext.INSTANCE, request).subscribe(subscriber);
    subscriber.subscription.request(1);

    assertEquals(1, subscriber.messages.size());
    JsonRpcErrorResponse response = (JsonRpcErrorResponse) subscriber.messages.get(0);
    assertEquals(request.id(), response.id());
    assertEquals(McpInternalException.ERROR_CODE, response.code());
    assertEquals("Internal error", response.message());
    assertEquals(true, response.data().isEmpty());
    assertEquals(null, subscriber.termination.join());
  }

  private static JsonRpcResultResponse response(JsonRpcRequest request, String value) {
    return new JsonRpcResultResponse(
        JsonTestValues.value(request.id()),
        JsonTestValues.value(JSON.objectNode().put("value", value)));
  }

  private static JsonRpcRequest request(int id, String method) {
    ObjectNode params = JSON.objectNode();
    ObjectNode metadata = params.putObject("_meta");
    metadata.put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION);
    metadata.putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    return new JsonRpcRequest(
        JsonTestValues.value(JSON.numberNode(id)), method, JsonTestValues.object(params));
  }

  private static JsonRpcRequest request(int id, String method, String progressToken) {
    ObjectNode params = JSON.objectNode();
    ObjectNode metadata = params.putObject("_meta");
    metadata.put(McpProtocol.PROTOCOL_VERSION_KEY, McpProtocol.REVISION);
    metadata.putObject(McpProtocol.CLIENT_CAPABILITIES_KEY);
    metadata.put("progressToken", progressToken);
    return new JsonRpcRequest(
        JsonTestValues.value(JSON.numberNode(id)), method, JsonTestValues.object(params));
  }

  private static final class ManualSubscriber implements Flow.Subscriber<JsonRpcMessage> {
    private final List<JsonRpcMessage> messages = new ArrayList<>();
    private final CompletableFuture<Throwable> termination = new CompletableFuture<>();
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
      termination.complete(throwable);
    }

    @Override
    public void onComplete() {
      termination.complete(null);
    }
  }
}
