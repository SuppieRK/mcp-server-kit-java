package io.github.suppierk.mcp.transport.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HttpEventStreamResponseTest {
  @Test
  void flushesAnEventBeforeTheSourceCompletes() throws Exception {
    var source = new OpenFramePublisher("event: message\ndata: {}\n\n");
    var response = new HttpEventStreamResponse(source);
    var received = new java.util.concurrent.CompletableFuture<String>();
    var destination =
        new ByteArrayOutputStream() {
          @Override
          public synchronized void write(byte[] bytes, int offset, int count) {
            super.write(bytes, offset, count);
            received.complete(toString(java.nio.charset.StandardCharsets.UTF_8));
          }
        };
    var buffered = new java.io.BufferedOutputStream(destination);
    var write =
        new FutureTask<Void>(
            () -> {
              response.writeTo(buffered);
              return null;
            });
    Thread writer = new Thread(write);
    writer.setDaemon(true);
    writer.start();
    try {
      assertEquals("event: message\ndata: {}\n\n", received.get(2, TimeUnit.SECONDS));
    } finally {
      response.close();
      writer.join(2000);
    }
  }

  @Test
  void requestsOnlyOnePublisherFrameAtATime() {
    var source = new ControlledPublisher();
    var response = new HttpEventStreamResponse(source);
    var subscriber = new GreedySubscriber();

    response.events().subscribe(subscriber);

    assertEquals(List.of(1L), source.requests);
    source.emit("first");
    assertEquals(List.of(1L, 1L), source.requests);
    assertEquals(List.of("first"), subscriber.items);
    response.close();
  }

  @Test
  void readsOneInputFrameAtATime() throws Exception {
    var source = new ControlledPublisher();
    var response = new HttpEventStreamResponse(source);
    var input = response.inputStream();

    assertEquals(List.of(1L), source.requests);
    source.emit("ab");
    assertEquals('a', input.read());
    assertEquals(List.of(1L), source.requests);
    assertEquals('b', input.read());
    assertEquals(List.of(1L, 1L), source.requests);
    source.complete();
    assertEquals(-1, input.read());
  }

  @Test
  void writesOneOutputFrameAtATimeUntilCompletion() throws Exception {
    var source = new SequencePublisher("first", "second");
    var response = new HttpEventStreamResponse(source);
    var output = new ByteArrayOutputStream();

    response.writeTo(output);

    assertEquals("firstsecond", output.toString(java.nio.charset.StandardCharsets.UTF_8));
    assertEquals(List.of(1L, 1L, 1L), source.requests);
  }

  @Test
  void closingTheResponseCancelsItsSourceSubscription() {
    var source = new ControlledPublisher();
    var response = new HttpEventStreamResponse(source);

    response.events().subscribe(new RecordingSubscriber());
    response.close();

    assertTrue(source.cancelled);
  }

  @Test
  void closingTheResponseReleasesABlockedInputReader() throws Exception {
    var source = new ControlledPublisher();
    var response = new HttpEventStreamResponse(source);
    var input = response.inputStream();
    var read = new FutureTask<>(input::read);
    Thread reader = new Thread(read);
    reader.setDaemon(true);
    reader.start();

    response.close();

    ExecutionException failure =
        assertThrows(ExecutionException.class, () -> read.get(1, TimeUnit.SECONDS));
    assertInstanceOf(java.io.IOException.class, failure.getCause());
    assertTrue(source.cancelled);
  }

  @Test
  void propagatesSourceFailuresToInputReaders() {
    var source = new ControlledPublisher();
    var response = new HttpEventStreamResponse(source);
    var input = response.inputStream();
    var sourceFailure = new IllegalStateException("failed");

    source.fail(sourceFailure);

    java.io.IOException failure = assertThrows(java.io.IOException.class, input::read);
    assertEquals(sourceFailure, failure.getCause());
  }

  @Test
  void cancelsTheSourceWhenDirectOutputFails() {
    var source = new OpenFramePublisher("frame");
    var response = new HttpEventStreamResponse(source);
    var output =
        new java.io.OutputStream() {
          @Override
          public void write(int value) throws java.io.IOException {
            throw new java.io.IOException("closed");
          }
        };

    java.io.IOException failure =
        assertThrows(java.io.IOException.class, () -> response.writeTo(output));

    assertEquals("closed", failure.getMessage());
    assertTrue(source.cancelled);
  }

  @Test
  void passesTerminalSignalsToPublisherConsumers() {
    var completedSource = new ControlledPublisher();
    var completed = new RecordingSubscriber();
    new HttpEventStreamResponse(completedSource).events().subscribe(completed);

    completedSource.complete();

    assertTrue(completed.complete);

    var failedSource = new ControlledPublisher();
    var failed = new RecordingSubscriber();
    var sourceFailure = new IllegalStateException("failed");
    new HttpEventStreamResponse(failedSource).events().subscribe(failed);

    failedSource.fail(sourceFailure);

    assertEquals(sourceFailure, failed.failure);
  }

  @Test
  void ignoresInvalidDemandAfterATerminalSignal() {
    var completedSource = new ControlledPublisher();
    var completed = new RecordingSubscriber();
    new HttpEventStreamResponse(completedSource).events().subscribe(completed);
    completedSource.complete();

    completed.subscription.request(0);

    assertTrue(completed.complete);
    assertEquals(null, completed.failure);

    var failedSource = new ControlledPublisher();
    var failed = new RecordingSubscriber();
    var sourceFailure = new IllegalStateException("failed");
    new HttpEventStreamResponse(failedSource).events().subscribe(failed);
    failedSource.fail(sourceFailure);

    failed.subscription.request(0);

    assertEquals(sourceFailure, failed.failure);
  }

  @Test
  void rejectsInvalidPublisherDemandAndCancelsTheSource() {
    var source = new ControlledPublisher();
    var subscriber = new RecordingSubscriber();
    new HttpEventStreamResponse(source).events().subscribe(subscriber);

    subscriber.subscription.request(0);

    assertInstanceOf(IllegalArgumentException.class, subscriber.failure);
    assertTrue(source.cancelled);
  }

  @Test
  void propagatesSourceFailuresToDirectWriters() {
    var sourceFailure = new IllegalStateException("failed");
    Flow.Publisher<ByteBuffer> source =
        subscriber ->
            subscriber.onSubscribe(
                new Flow.Subscription() {
                  @Override
                  public void request(long count) {
                    subscriber.onError(sourceFailure);
                  }

                  @Override
                  public void cancel() {}
                });
    var response = new HttpEventStreamResponse(source);

    java.io.IOException failure =
        assertThrows(
            java.io.IOException.class, () -> response.writeTo(new ByteArrayOutputStream()));

    assertEquals(sourceFailure, failure.getCause());
  }

  @Test
  void interruptionReleasesBlockingViewsAndRestoresTheInterrupt() throws Exception {
    var inputSource = new ControlledPublisher();
    var input = new HttpEventStreamResponse(inputSource).inputStream();
    var read =
        new FutureTask<>(
            () -> {
              try {
                input.read();
                return false;
              } catch (java.io.IOException exception) {
                return Thread.currentThread().isInterrupted();
              }
            });
    Thread reader = daemonThread(read);

    reader.interrupt();

    assertTrue(read.get(1, TimeUnit.SECONDS));
    assertTrue(inputSource.cancelled);

    var outputSource = new ControlledPublisher();
    var outputResponse = new HttpEventStreamResponse(outputSource);
    var write =
        new FutureTask<>(
            () -> {
              try {
                outputResponse.writeTo(new ByteArrayOutputStream());
                return false;
              } catch (java.io.IOException exception) {
                return Thread.currentThread().isInterrupted();
              }
            });
    Thread writer = daemonThread(write);

    writer.interrupt();

    assertTrue(write.get(1, TimeUnit.SECONDS));
    assertTrue(outputSource.cancelled);
  }

  @Test
  void closingAnInputViewCancelsTheSource() throws Exception {
    var source = new ControlledPublisher();
    var input = new HttpEventStreamResponse(source).inputStream();

    input.close();

    assertTrue(source.cancelled);
    assertThrows(java.io.IOException.class, input::read);
  }

  @Test
  void allowsOnlyOneConsumptionView() {
    var response = new HttpEventStreamResponse(passivePublisher());
    var first = new RecordingSubscriber();

    response.events().subscribe(first);

    assertThrows(IllegalStateException.class, response::inputStream);
    assertThrows(IllegalStateException.class, () -> response.writeTo(new ByteArrayOutputStream()));
    var second = new RecordingSubscriber();
    response.events().subscribe(second);
    assertInstanceOf(IllegalStateException.class, second.failure);
    second.subscription.request(1);
    second.subscription.cancel();
    response.close();

    var inputFirst = new HttpEventStreamResponse(passivePublisher());
    inputFirst.inputStream();
    var afterInput = new RecordingSubscriber();
    inputFirst.events().subscribe(afterInput);
    assertInstanceOf(IllegalStateException.class, afterInput.failure);
    assertThrows(
        IllegalStateException.class, () -> inputFirst.writeTo(new ByteArrayOutputStream()));
    inputFirst.close();

    var outputFirst = new HttpEventStreamResponse(new SequencePublisher("done"));
    assertThrows(
        IllegalStateException.class,
        () -> {
          outputFirst.writeTo(new ByteArrayOutputStream());
          outputFirst.inputStream();
        });
    var afterOutput = new RecordingSubscriber();
    outputFirst.events().subscribe(afterOutput);
    assertInstanceOf(IllegalStateException.class, afterOutput.failure);
  }

  private static Thread daemonThread(Runnable task) {
    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

  private static Flow.Publisher<ByteBuffer> passivePublisher() {
    return subscriber ->
        subscriber.onSubscribe(
            new Flow.Subscription() {
              @Override
              public void request(long count) {}

              @Override
              public void cancel() {}
            });
  }

  private static final class RecordingSubscriber implements Flow.Subscriber<ByteBuffer> {
    private Flow.Subscription subscription;
    private Throwable failure;
    private boolean complete;

    @Override
    public void onSubscribe(Flow.Subscription value) {
      subscription = value;
    }

    @Override
    public void onNext(ByteBuffer item) {}

    @Override
    public void onError(Throwable throwable) {
      failure = throwable;
    }

    @Override
    public void onComplete() {
      complete = true;
    }
  }

  private static final class GreedySubscriber implements Flow.Subscriber<ByteBuffer> {
    private final List<String> items = new ArrayList<>();

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ByteBuffer item) {
      byte[] bytes = new byte[item.remaining()];
      item.get(bytes);
      items.add(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onComplete() {}
  }

  private static final class ControlledPublisher implements Flow.Publisher<ByteBuffer> {
    private final List<Long> requests = new ArrayList<>();
    private Flow.Subscriber<? super ByteBuffer> subscriber;
    private boolean cancelled;

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> value) {
      subscriber = value;
      value.onSubscribe(
          new Flow.Subscription() {
            @Override
            public void request(long count) {
              requests.add(count);
            }

            @Override
            public void cancel() {
              cancelled = true;
            }
          });
    }

    private void emit(String value) {
      subscriber.onNext(ByteBuffer.wrap(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    private void complete() {
      subscriber.onComplete();
    }

    private void fail(Throwable failure) {
      subscriber.onError(failure);
    }
  }

  private static final class SequencePublisher implements Flow.Publisher<ByteBuffer> {
    private final List<String> values;
    private final List<Long> requests = new ArrayList<>();
    private boolean cancelled;

    private SequencePublisher(String... values) {
      this.values = List.of(values);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
      subscriber.onSubscribe(
          new Flow.Subscription() {
            private int index;

            @Override
            public void request(long count) {
              requests.add(count);
              if (index == values.size()) {
                index++;
                subscriber.onComplete();
              } else if (index < values.size()) {
                String value = values.get(index++);
                subscriber.onNext(
                    ByteBuffer.wrap(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
              }
            }

            @Override
            public void cancel() {
              cancelled = true;
            }
          });
    }
  }

  private static final class OpenFramePublisher implements Flow.Publisher<ByteBuffer> {
    private final String value;
    private boolean emitted;
    private boolean cancelled;

    private OpenFramePublisher(String value) {
      this.value = value;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
      subscriber.onSubscribe(
          new Flow.Subscription() {
            @Override
            public void request(long count) {
              if (!emitted) {
                emitted = true;
                subscriber.onNext(
                    ByteBuffer.wrap(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
              }
            }

            @Override
            public void cancel() {
              cancelled = true;
            }
          });
    }
  }
}
