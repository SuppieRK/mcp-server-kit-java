package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes a message issued to or received from an LLM API.
 *
 * @param meta the optional protocol metadata
 * @param content the content
 * @param role the message role
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSamplingMessage(
    @JsonProperty("_meta") Optional<McpMetaObject> meta, JsonNode content, McpRole role) {
  /** Validates and copies the protocol fields. */
  public McpSamplingMessage {
    Objects.requireNonNull(meta, "meta");
    content = Objects.requireNonNull(content, "content").deepCopy();
    Objects.requireNonNull(role, "role");
  }

  /**
   * Returns a copy of the message content.
   *
   * @return the copied content
   */
  public JsonNode content() {
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
    private JsonNode content;
    private McpRole role;

    private Builder() {}

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
    public Builder content(JsonNode content) {
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
