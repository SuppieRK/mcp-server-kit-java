package io.github.suppierk.mcp.server;

import java.util.concurrent.CompletionStage;

/**
 * Exposes request-scoped cancellation and progress control to an MCP handler.
 *
 * <p>This interface is thread-safe. Cancellation is monotonic. Concurrent progress calls are
 * applied in one order, and downstream signals are serialized. All progress calls validate their
 * arguments and lifecycle state. A valid progress call has no effect when the request has no
 * progress token.
 */
public interface McpHandlerContext {
  /**
   * Returns whether this request has been cancelled.
   *
   * @return {@code true} after cancellation
   */
  boolean isCancelled();

  /**
   * Returns a read-only stage that completes when this request is cancelled.
   *
   * <p>Each call returns the same stage. Attempts to complete a future obtained from the stage do
   * not change this context's cancellation state.
   *
   * @return the stable cancellation stage
   */
  CompletionStage<Void> cancellation();

  /**
   * Reports progress without a total or message.
   *
   * <p>The value must be finite and greater than every previously accepted value.
   *
   * @param value the completed amount
   * @throws IllegalArgumentException if the value is not finite or does not increase
   * @throws IllegalStateException if the request has ended
   */
  void progress(double value);

  /**
   * Reports progress with a message.
   *
   * <p>The value must be finite and greater than every previously accepted value. The message can
   * be empty but cannot be {@code null}.
   *
   * @param value the completed amount
   * @param message the progress message
   * @throws IllegalArgumentException if the value is invalid or the message is {@code null}
   * @throws IllegalStateException if the request has ended
   */
  void progress(double value, String message);

  /**
   * Reports progress with a total.
   *
   * <p>The value and total must be finite. The value must be greater than every previously accepted
   * value. The total can be less than the value, negative, or different from an earlier total.
   *
   * @param value the completed amount
   * @param total the total amount
   * @throws IllegalArgumentException if a value is not finite or progress does not increase
   * @throws IllegalStateException if the request has ended
   */
  void progress(double value, double total);

  /**
   * Reports progress with a total and message.
   *
   * <p>The value and total follow the same rules as {@link #progress(double, double)}. The message
   * can be empty but cannot be {@code null}.
   *
   * @param value the completed amount
   * @param total the total amount
   * @param message the progress message
   * @throws IllegalArgumentException if a value is invalid or the message is {@code null}
   * @throws IllegalStateException if the request has ended
   */
  void progress(double value, double total, String message);
}
