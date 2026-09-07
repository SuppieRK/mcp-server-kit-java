package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The sender or recipient of messages and data in a conversation.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public enum McpRole {
  /** The assistant role. */
  @JsonProperty("assistant")
  ASSISTANT,

  /** The user role. */
  @JsonProperty("user")
  USER;
}
