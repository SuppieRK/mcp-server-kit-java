package io.github.suppierk.mcp.protocol;

/**
 * A server-originated invalidation that may be emitted to an opted-in subscription stream.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/server">MCP server
 *     features</a>
 */
public sealed interface McpSubscriptionNotification extends McpServerNotification, JsonRpcMessage
    permits McpToolListChangedNotification,
        McpPromptListChangedNotification,
        McpResourceListChangedNotification,
        McpResourceUpdatedNotification {

  /**
   * Gets the notification's wire-method name.
   *
   * @return the method name
   */
  String method();
}
