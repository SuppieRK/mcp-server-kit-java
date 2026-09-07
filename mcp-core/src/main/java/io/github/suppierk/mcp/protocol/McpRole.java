package io.github.suppierk.mcp.protocol;

/**
 * The sender or recipient of messages and data in a conversation.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public enum McpRole {
  /** The assistant role. */
  ASSISTANT,

  /** The user role. */
  USER;
}
