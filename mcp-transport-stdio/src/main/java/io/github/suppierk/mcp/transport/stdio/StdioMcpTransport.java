package io.github.suppierk.mcp.transport.stdio;

import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.McpClientNotification;
import io.github.suppierk.mcp.protocol.McpClientRequest;
import io.github.suppierk.mcp.server.McpInvalidRequestException;
import io.github.suppierk.mcp.server.McpServerKit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Runs MCP over newline-delimited UTF-8 standard input and standard output.
 *
 * @param <C> the application-context type
 * @see <a
 *     href="https://modelcontextprotocol.io/specification/2026-07-28/basic/transports#stdio">MCP
 *     standard input and output transport</a>
 */
public final class StdioMcpTransport<C> {
  private static final byte[] NEWLINE = {'\n'};

  private final McpServerKit<C> serverKit;
  private final C applicationContext;
  private final InputStream input;
  private final OutputStream output;
  private final OutputStream diagnostics;
  private final Executor executor;
  private final ReentrantLock outputLock = new ReentrantLock();
  private final Map<Object, WritingSubscriber> active = new ConcurrentHashMap<>();

  /**
   * Creates a transport for the process streams.
   *
   * @param serverKit the MCP server kit
   * @param applicationContext the application-owned context reused for every message
   * @param <C> the application-context type
   * @return the transport
   */
  public static <C> StdioMcpTransport<C> system(McpServerKit<C> serverKit, C applicationContext) {
    return new StdioMcpTransport<>(
        serverKit,
        applicationContext,
        System.in,
        System.out,
        System.err,
        ForkJoinPool.commonPool());
  }

  /**
   * Creates a stdio transport.
   *
   * @param serverKit the MCP server kit
   * @param applicationContext the application-owned context reused for every message
   * @param input the newline-delimited input
   * @param output the protocol-only output
   * @param diagnostics the diagnostic output
   * @param executor the input-message executor
   */
  public StdioMcpTransport(
      McpServerKit<C> serverKit,
      C applicationContext,
      InputStream input,
      OutputStream output,
      OutputStream diagnostics,
      Executor executor) {
    this.serverKit = Objects.requireNonNull(serverKit, "serverKit");
    this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
    this.input = Objects.requireNonNull(input, "input");
    this.output = Objects.requireNonNull(output, "output");
    this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
    this.executor = Objects.requireNonNull(executor, "executor");
  }

  /**
   * Reads messages until end of input and then cancels open subscriptions.
   *
   * @throws IOException if an input or output stream fails
   */
  public void run() throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isBlank()) {
          submit(line.getBytes(StandardCharsets.UTF_8));
        }
      }
    } finally {
      active.values().forEach(WritingSubscriber::cancel);
      awaitFinished();
    }
  }

  /** Starts one input message. */
  private void submit(byte[] bytes) {
    JsonRpcMessage message = serverKit.decode(bytes);
    if (message instanceof McpClientNotification notification) {
      WritingSubscriber request = active.get(notification.params().requestId());
      if (request != null) {
        request.cancel();
      }
      return;
    }
    var requestId =
        message instanceof McpClientRequest request
            ? request.id()
            : message instanceof JsonRpcRequest request ? request.id() : null;
    Object key = requestId == null ? new Object() : requestId;
    WritingSubscriber subscriber = new WritingSubscriber();
    if (active.putIfAbsent(key, subscriber) != null) {
      write(
          new JsonRpcErrorResponse(
              requestId,
              McpInvalidRequestException.CODE,
              "The request ID is active",
              Optional.empty()));
      return;
    }
    subscriber.completion.whenComplete((unused, failure) -> active.remove(key, subscriber));
    CompletableFuture.runAsync(
            () -> {
              if (subscriber.begin()) {
                serverKit.handle(applicationContext, message).subscribe(subscriber);
              }
            },
            executor)
        .exceptionally(
            failure -> {
              subscriber.fail(failure);
              return null;
            });
  }

  /** Waits for all subscribers after input ends. */
  private void awaitFinished() {
    CompletableFuture.allOf(
            active.values().stream().map(item -> item.completion).toArray(CompletableFuture[]::new))
        .join();
  }

  /** Writes one protocol message. */
  private void write(JsonRpcMessage message) {
    outputLock.lock();
    try {
      output.write(serverKit.encode(message));
      output.write(NEWLINE);
      output.flush();
    } catch (IOException exception) {
      throw new IllegalStateException("The MCP output stream failed", exception);
    } finally {
      outputLock.unlock();
    }
  }

  /** Writes one diagnostic message. */
  private void diagnostic() {
    try {
      diagnostics.write("MCP request failed\n".getBytes(StandardCharsets.UTF_8));
      diagnostics.flush();
    } catch (IOException ignored) {
      // The transport cannot report a failure after the diagnostic stream closes.
    }
  }

  /** Writes one server publication to stdout. */
  private final class WritingSubscriber implements Flow.Subscriber<JsonRpcMessage> {
    private final CompletableFuture<Void> completion = new CompletableFuture<>();
    private Flow.Subscription subscription;

    /** Claims dispatch unless transport shutdown already completed this request. */
    private synchronized boolean begin() {
      return !completion.isDone();
    }

    /** {@inheritDoc} */
    @Override
    public void onSubscribe(Flow.Subscription value) {
      boolean completed;
      synchronized (this) {
        subscription = value;
        completed = completion.isDone();
      }
      if (completed) {
        value.cancel();
      } else {
        value.request(Long.MAX_VALUE);
      }
    }

    /** {@inheritDoc} */
    @Override
    public void onNext(JsonRpcMessage item) {
      try {
        write(item);
      } catch (RuntimeException exception) {
        cancel();
        fail(exception);
      }
    }

    /** {@inheritDoc} */
    @Override
    public void onError(Throwable failure) {
      diagnostic();
      completion.completeExceptionally(failure);
    }

    /** {@inheritDoc} */
    @Override
    public void onComplete() {
      completion.complete(null);
    }

    /** Cancels this publication and releases the wait. */
    private synchronized void cancel() {
      if (subscription != null) {
        subscription.cancel();
      }
      completion.complete(null);
    }

    /** Fails this subscriber before or after subscription. */
    private void fail(Throwable failure) {
      diagnostic();
      completion.completeExceptionally(failure);
    }
  }
}
