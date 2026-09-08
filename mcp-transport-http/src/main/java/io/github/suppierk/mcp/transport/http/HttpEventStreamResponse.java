package io.github.suppierk.mcp.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Flow;

/**
 * A request-scoped Server-Sent Events response with one shared consumption.
 *
 * <p>The {@link #events()}, {@link #inputStream()}, and {@link #writeTo(OutputStream)} views share
 * one consume-once claim. The first subscribed or opened view wins. The claim remains consumed
 * after completion, failure, cancellation, or close.
 */
public final class HttpEventStreamResponse implements HttpMcpResponse, AutoCloseable {
  private final Flow.Publisher<ByteBuffer> events;
  private boolean closed;
  private boolean consumed;
  private Consumption activeConsumption;

  /**
   * Creates one event-stream response.
   *
   * @param events the encoded event publisher
   * @throws NullPointerException if {@code events} is {@code null}
   */
  public HttpEventStreamResponse(Flow.Publisher<ByteBuffer> events) {
    this.events = Objects.requireNonNull(events, "events");
  }

  /** {@inheritDoc} */
  @Override
  public int status() {
    return 200;
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> headers() {
    return Map.of(
        "Content-Type",
        "text/event-stream",
        "Cache-Control",
        "no-cache",
        "X-Accel-Buffering",
        "no");
  }

  /**
   * Gets the encoded SSE publisher.
   *
   * <p>Subscription claims this response and requests at most one source frame at a time. Source
   * completion and failure pass to the subscriber. Subscriber cancellation cancels the source. A
   * subscriber that loses the shared claim receives {@link IllegalStateException} through {@code
   * onError}.
   *
   * @return the event publisher
   */
  public Flow.Publisher<ByteBuffer> events() {
    return subscriber -> {
      Objects.requireNonNull(subscriber, "subscriber");
      OneFrameSubscriber consumption = new OneFrameSubscriber(subscriber);
      if (!claimIfAvailable(consumption)) {
        subscriber.onSubscribe(new EmptySubscription());
        subscriber.onError(new IllegalStateException("The MCP event stream is already consumed"));
        return;
      }
      events.subscribe(consumption);
    };
  }

  /**
   * Opens the encoded events as a blocking input stream.
   *
   * <p>This call claims the response. Source completion produces end of stream after buffered bytes
   * are read. A source failure produces {@link IOException} with that failure as its cause. Closing
   * the stream cancels the source. An interrupted read restores the interrupt status, cancels the
   * source, and throws {@code IOException}.
   *
   * @return the event input stream
   * @throws IllegalStateException if this response is already consumed or closed
   */
  public InputStream inputStream() {
    EventInputStream input = new EventInputStream();
    claim(input);
    events.subscribe(input);
    return input;
  }

  /**
   * Writes the encoded events directly to an output stream.
   *
   * <p>This call claims the response and blocks until source completion or failure. A source or
   * output failure produces {@link IOException}. Interruption restores the interrupt status,
   * cancels the source, and produces {@code IOException}. Each written chunk is flushed so that an
   * open stream does not retain events in the host's output buffer.
   *
   * @param output the destination
   * @throws IOException if streaming fails
   * @throws IllegalStateException if this response is already consumed or closed
   * @throws NullPointerException if {@code output} is {@code null}
   */
  public void writeTo(OutputStream output) throws IOException {
    Objects.requireNonNull(output, "output");
    try (InputStream input = inputStream()) {
      byte[] buffer = new byte[8192];
      int count;
      while ((count = input.read(buffer)) != -1) {
        output.write(buffer, 0, count);
        output.flush();
      }
    }
  }

  /**
   * Closes this response and cancels an active source subscription.
   *
   * <p>This operation is idempotent. Closing before consumption prevents a later view from claiming
   * the response.
   */
  @Override
  public void close() {
    Consumption consumption;
    synchronized (this) {
      if (closed) {
        return;
      }
      closed = true;
      consumption = activeConsumption;
      activeConsumption = null;
    }
    if (consumption != null) {
      consumption.cancel();
    }
  }

  /** Claims the one response consumption. */
  private synchronized void claim(Consumption consumption) {
    if (!claimIfAvailable(consumption)) {
      throw new IllegalStateException("The MCP event stream is already consumed");
    }
  }

  /** Claims the stream when it has not been consumed or closed. */
  private synchronized boolean claimIfAvailable(Consumption consumption) {
    if (consumed || closed) {
      return false;
    }
    consumed = true;
    activeConsumption = consumption;
    return true;
  }

  /** Releases the active consumption after a terminal signal. */
  private synchronized void finish(Consumption consumption) {
    if (activeConsumption == consumption) {
      activeConsumption = null;
    }
    closed = true;
  }

  /** One response view that can release a waiting caller and cancel its source. */
  private interface Consumption {
    void cancel();
  }

  /** A subscription used to report a rejected second subscriber. */
  private static final class EmptySubscription implements Flow.Subscription {
    @Override
    public void request(long count) {
      // A rejected subscription has no items to request.
    }

    @Override
    public void cancel() {
      // A rejected subscription owns no upstream work.
    }
  }

  /** Exposes one event frame at a time through blocking reads. */
  private final class EventInputStream extends InputStream
      implements Flow.Subscriber<ByteBuffer>, Consumption {
    private Flow.Subscription subscription;
    private ByteBuffer current = ByteBuffer.allocate(0);
    private Throwable failure;
    private boolean complete;
    private boolean inputClosed;

    @Override
    public void onSubscribe(Flow.Subscription value) {
      synchronized (this) {
        if (subscription != null || inputClosed) {
          value.cancel();
          return;
        }
        subscription = value;
      }
      value.request(1);
    }

    @Override
    public void onNext(ByteBuffer item) {
      Flow.Subscription next = null;
      synchronized (this) {
        if (inputClosed || complete || failure != null) {
          return;
        }
        current = item.asReadOnlyBuffer();
        if (!current.hasRemaining()) {
          next = subscription;
        }
        notifyAll();
      }
      OneFrameSubscriber.request(next);
    }

    @Override
    public void onError(Throwable throwable) {
      synchronized (this) {
        if (inputClosed || complete || failure != null) {
          return;
        }
        failure = throwable;
        notifyAll();
      }
      finish(this);
    }

    @Override
    public void onComplete() {
      synchronized (this) {
        if (inputClosed || complete || failure != null) {
          return;
        }
        complete = true;
        notifyAll();
      }
      finish(this);
    }

    @Override
    public int read() throws IOException {
      byte[] one = new byte[1];
      int count = read(one, 0, 1);
      return count < 0 ? -1 : Byte.toUnsignedInt(one[0]);
    }

    @Override
    public int read(byte[] target, int offset, int length) throws IOException {
      Objects.checkFromIndexSize(offset, length, target.length);
      if (length == 0) {
        return 0;
      }
      Flow.Subscription next;
      synchronized (this) {
        awaitFrame();
        if (!current.hasRemaining()) {
          if (failure != null) {
            throw new IOException("The MCP event stream failed", failure);
          }
          if (inputClosed) {
            throw new IOException("The MCP event stream is closed");
          }
          return -1;
        }
        int count = Math.min(length, current.remaining());
        current.get(target, offset, count);
        next = current.hasRemaining() ? null : subscription;
        OneFrameSubscriber.request(next);
        return count;
      }
    }

    @Override
    public void close() {
      HttpEventStreamResponse.this.close();
    }

    /** Cancels the source and releases a waiting reader. */
    @Override
    public void cancel() {
      Flow.Subscription active;
      synchronized (this) {
        if (inputClosed) {
          return;
        }
        inputClosed = true;
        active = subscription;
        notifyAll();
      }
      if (active != null) {
        active.cancel();
      }
    }

    /** Waits until a frame or terminal signal is available. */
    private synchronized void awaitFrame() throws IOException {
      while (!current.hasRemaining() && failure == null && !complete && !inputClosed) {
        try {
          wait();
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          close();
          throw new IOException("The MCP event stream was interrupted", exception);
        }
      }
    }
  }

  /** Adapts arbitrary downstream demand to one outstanding source frame. */
  private final class OneFrameSubscriber
      implements Flow.Subscriber<ByteBuffer>, Flow.Subscription, Consumption {
    private final Flow.Subscriber<? super ByteBuffer> downstream;
    private Flow.Subscription upstream;
    private long requested;
    private boolean ready;
    private boolean inFlight;
    private boolean delivering;
    private boolean terminated;

    private OneFrameSubscriber(Flow.Subscriber<? super ByteBuffer> downstream) {
      this.downstream = downstream;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      synchronized (this) {
        if (upstream != null || terminated) {
          subscription.cancel();
          return;
        }
        upstream = subscription;
      }
      downstream.onSubscribe(this);
      Flow.Subscription next;
      synchronized (this) {
        ready = true;
        next = nextRequest();
      }
      request(next);
    }

    @Override
    public void onNext(ByteBuffer item) {
      synchronized (this) {
        if (terminated) {
          return;
        }
        inFlight = false;
        if (requested != Long.MAX_VALUE) {
          requested--;
        }
        delivering = true;
      }
      try {
        downstream.onNext(item.asReadOnlyBuffer());
      } catch (RuntimeException exception) {
        cancel();
        throw exception;
      } finally {
        Flow.Subscription next;
        synchronized (this) {
          delivering = false;
          next = nextRequest();
        }
        request(next);
      }
    }

    @Override
    public void onError(Throwable throwable) {
      synchronized (this) {
        if (terminated) {
          return;
        }
        terminated = true;
      }
      finish(this);
      downstream.onError(throwable);
    }

    @Override
    public void onComplete() {
      synchronized (this) {
        if (terminated) {
          return;
        }
        terminated = true;
      }
      finish(this);
      downstream.onComplete();
    }

    @Override
    public void request(long count) {
      if (count <= 0) {
        Flow.Subscription subscription;
        synchronized (this) {
          if (terminated) {
            return;
          }
          terminated = true;
          subscription = upstream;
        }
        if (subscription != null) {
          subscription.cancel();
        }
        finish(this);
        downstream.onError(new IllegalArgumentException("Demand must be positive"));
        return;
      }
      Flow.Subscription next;
      synchronized (this) {
        if (terminated) {
          return;
        }
        requested = addDemand(requested, count);
        next = nextRequest();
      }
      request(next);
    }

    @Override
    public void cancel() {
      Flow.Subscription subscription;
      synchronized (this) {
        if (terminated) {
          return;
        }
        terminated = true;
        subscription = upstream;
      }
      if (subscription != null) {
        subscription.cancel();
      }
      finish(this);
    }

    /** Reserves one source request when downstream demand permits it. */
    private Flow.Subscription nextRequest() {
      if (ready && !terminated && !delivering && !inFlight && requested > 0) {
        inFlight = true;
        return upstream;
      }
      return null;
    }

    /** Requests one frame when a request was reserved. */
    private static void request(Flow.Subscription subscription) {
      if (subscription != null) {
        subscription.request(1);
      }
    }

    /** Adds demand without overflowing. */
    private static long addDemand(long current, long additional) {
      long sum = current + additional;
      return sum < 0 ? Long.MAX_VALUE : sum;
    }
  }
}
