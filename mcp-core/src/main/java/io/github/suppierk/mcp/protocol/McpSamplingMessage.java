package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Describes a message issued to or received from an LLM API.
 *
 * @param meta the optional protocol metadata
 * @param content the content
 * @param role the message role
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSamplingMessage(Optional<McpMetaObject> meta, Object content, McpRole role) {
  /** Validates and copies the protocol fields. */
  public McpSamplingMessage {
    Objects.requireNonNull(meta, "meta");
    content = McpProtocol.copy(Objects.requireNonNull(content, "content"));
    Objects.requireNonNull(role, "role");
  }

  /**
   * Returns a copy of the message content.
   *
   * @return the copied content
   */
  public Object content() {
    return McpProtocol.copy(content);
  }

  /**
   * Creates a builder for {@link McpSamplingMessage}.
   *
   * @return a new builder
   */
  public static Builder mcpSamplingMessage() {
    return new Builder();
  }

  /** Builds {@link McpSamplingMessage} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Object content;
    private McpRole role;

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpMetaObject.Builder> configure) {
      var child = McpMetaObject.mcpMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Sets {@code content}.
     *
     * @param content the value
     * @return this builder
     */
    public Builder content(Object content) {
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
    public McpSamplingMessage build() {
      return new McpSamplingMessage(meta, content, role);
    }
  }
}
