package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The result returned by the server for a {@link McpCompleteRequest} ({@code completion/complete})
 * request.
 *
 * @param meta the optional protocol metadata
 * @param completion the completion data
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCompleteResult(
    Optional<McpResultMetaObject> meta, Map<String, ?> completion, String resultType)
    implements McpServerResult {
  /** Validates and copies the protocol fields. */
  public McpCompleteResult {
    Objects.requireNonNull(meta, "meta");
    completion = McpProtocol.copy(Objects.requireNonNull(completion, "completion"));
    Objects.requireNonNull(resultType, "resultType");
  }

  /**
   * Returns a copy of the completion value.
   *
   * @return the copied completion value
   */
  public Map<String, ?> completion() {
    return McpProtocol.copy(completion);
  }

  /**
   * Creates a builder for {@link McpCompleteResult}.
   *
   * @return a new builder
   */
  public static Builder mcpCompleteResult() {
    return new Builder();
  }

  /** Builds {@link McpCompleteResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private Map<String, ?> completion;
    private String resultType;

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpResultMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpResultMetaObject.Builder> configure) {
      var child = McpResultMetaObject.mcpResultMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

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
     * Sets {@code completion}.
     *
     * @param completion the value
     * @return this builder
     */
    public Builder completion(Map<String, ?> completion) {
      this.completion = completion;
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
    public McpCompleteResult build() {
      return new McpCompleteResult(meta, completion, resultType);
    }
  }
}
