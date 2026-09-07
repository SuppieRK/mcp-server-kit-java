package io.github.suppierk.mcp.server;

import java.util.concurrent.CompletableFuture;

/**
 * Handles one typed MCP operation with application-owned invocation data.
 *
 * <p>The server kit invokes a selected handler once, after the first positive downstream demand. A
 * synchronous registration runs the handler inline on that demand thread. An asynchronous
 * registration also invokes the handler inline, but the application owns the returned {@link
 * CompletableFuture} and its execution policy. The server kit does not select an executor or
 * provide thread affinity.
 *
 * <p>The handler and its eventual result must be non-null. A protocol exception becomes its fixed
 * error response. Another {@link Exception} becomes a generic internal-error response. An {@link
 * Error} terminates the publication with the same error. Downstream cancellation becomes visible
 * through {@code handlerContext} before the server kit calls {@code cancel(false)} on an
 * asynchronous application future.
 *
 * @param <C> the application-context type
 * @param <P> the operation-parameters type
 * @param <R> the operation-result type
 */
@FunctionalInterface
public interface McpHandler<C, P, R> {
  /**
   * Handles one operation.
   *
   * @param applicationContext application-owned invocation data
   * @param parameters the operation parameters
   * @param handlerContext cancellation and progress control for this request
   * @return the non-null operation result or application-owned future required by the registration
   */
  R handle(C applicationContext, P parameters, McpHandlerContext handlerContext);
}
