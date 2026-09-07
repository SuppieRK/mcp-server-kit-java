package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The result returned by the server for a {@link McpGetPromptRequest} ({@code prompts/get})
 * request.
 *
 * @param meta the optional protocol metadata
 * @param description An optional description for the prompt.
 * @param messages the message list
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpGetPromptResult(
    Optional<McpResultMetaObject> meta,
    Optional<String> description,
    List<McpPromptMessage> messages,
    String resultType)
    implements McpServerResult, McpGetPromptResultResponse.Result {
  /** Validates and copies the protocol fields. */
  public McpGetPromptResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(description, "description");
    messages = List.copyOf(messages);
    Objects.requireNonNull(resultType, "resultType");
  }

  /**
   * Creates a complete prompt result.
   *
   * @param messages the prompt messages
   */
  public McpGetPromptResult(List<McpPromptMessage> messages) {
    this(Optional.empty(), Optional.empty(), messages, "complete");
  }

  /**
   * Creates a builder for {@link McpGetPromptResult}.
   *
   * @return a new builder
   */
  public static Builder mcpGetPromptResult() {
    return new Builder();
  }

  /** Builds {@link McpGetPromptResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private Optional<String> description = Optional.empty();
    private List<McpPromptMessage> messages;
    private String resultType;

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpResultMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpResultMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Sets {@code description}.
     *
     * @param description the optional value
     * @return this builder
     */
    public Builder description(Optional<String> description) {
      this.description = description;
      return this;
    }

    /**
     * Sets {@code description}.
     *
     * @param description the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder description(String description) {
      return description(Optional.ofNullable(description));
    }

    /**
     * Sets {@code messages}.
     *
     * @param messages the value
     * @return this builder
     */
    public Builder messages(List<McpPromptMessage> messages) {
      this.messages = messages;
      return this;
    }

    /**
     * Sets {@code resultType}.
     *
     * @param resultType the value
     * @return this builder
     */
    public Builder resultType(String resultType) {
      this.resultType = resultType;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpGetPromptResult build() {
      return new McpGetPromptResult(meta, description, messages, resultType);
    }
  }
}
