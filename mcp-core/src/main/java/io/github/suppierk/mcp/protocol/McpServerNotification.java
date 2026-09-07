package io.github.suppierk.mcp.protocol;

/**
 * A typed notification sent by an MCP server.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpServerNotification
    permits McpCancelledNotification,
        McpProgressNotification,
        McpSubscriptionNotification,
        McpSubscriptionsAcknowledgedNotification,
        McpLoggingMessageNotification {}
