package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;

/**
 * The result returned by the client for a {@link McpCreateMessageRequest} ({@code
 * sampling/createMessage}) request. The client should inform the user before returning the sampled
 * message, to allow them to inspect the response (human in the loop) and decide whether to allow
 * the server to see it.
 *
 * @param meta the optional protocol metadata
 * @param content the content
 * @param model The name of the model that generated the message.
 * @param role the message role
 * @param stopReason the optional reason that sampling stopped. Standard values are {@code endTurn},
 *     {@code stopSequence}, {@code maxTokens}, and {@code toolUse}. Custom values are permitted.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCreateMessageResult(
    @JsonProperty("_meta") Optional<McpMetaObject> meta,
    JsonNode content,
    String model,
    McpRole role,
    Optional<String> stopReason)
    implements McpInputResponse {
  /** Validates and copies the protocol fields. */
  public McpCreateMessageResult {
    Objects.requireNonNull(meta, "meta");
    content = Objects.requireNonNull(content, "content").deepCopy();
    Objects.requireNonNull(model, "model");
    Objects.requireNonNull(role, "role");
    Objects.requireNonNull(stopReason, "stopReason");
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
   * Creates a builder for {@link McpCreateMessageResult}.
   *
   * @return a new builder
   */
  public static Builder mcpCreateMessageResult() {
    return new Builder();
  }

  /** Builds {@link McpCreateMessageResult} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private JsonNode content;
    private String model;
    private McpRole role;
    private Optional<String> stopReason = Optional.empty();

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
     * Sets {@code model}.
     *
     * @param model the value
     * @return this builder
     */
    public Builder model(String model) {
      this.model = model;
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
     * Sets {@code stopReason}.
     *
     * @param stopReason the optional value
     * @return this builder
     */
    public Builder stopReason(Optional<String> stopReason) {
      this.stopReason = stopReason;
      return this;
    }

    /**
     * Sets {@code stopReason}.
     *
     * @param stopReason the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder stopReason(String stopReason) {
      return stopReason(Optional.ofNullable(stopReason));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCreateMessageResult build() {
      return new McpCreateMessageResult(meta, content, model, role, stopReason);
    }
  }
}
