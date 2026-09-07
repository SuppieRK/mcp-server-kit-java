package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * Describes a message returned as part of a prompt. This is similar to {@code SamplingMessage}, but
 * also supports the embedding of resources from the MCP server.
 *
 * @param content the content
 * @param role the message role
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPromptMessage(McpContentBlock content, McpRole role) {
  /** Validates and copies the protocol fields. */
  public McpPromptMessage {
    Objects.requireNonNull(content, "content");
    Objects.requireNonNull(role, "role");
  }

  /**
   * Creates a builder for {@link McpPromptMessage}.
   *
   * @return a new builder
   */
  public static Builder mcpPromptMessage() {
    return new Builder();
  }

  /** Builds {@link McpPromptMessage} values. */
  public static final class Builder {
    private McpContentBlock content;
    private McpRole role;

    private Builder() {}

    /**
     * Sets {@code content}.
     *
     * @param content the value
     * @return this builder
     */
    public Builder content(McpContentBlock content) {
      this.content = content;
      return this;
    }

    /**
     * Sets {@code role}.
     *
     * @param role the value
     * @return this builder
     */
    public Builder role(McpRole role) {
      this.role = role;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpPromptMessage build() {
      return new McpPromptMessage(content, role);
    }
  }
}
