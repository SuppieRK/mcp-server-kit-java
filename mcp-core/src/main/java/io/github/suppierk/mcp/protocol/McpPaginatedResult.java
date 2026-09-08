package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A result that can provide a next-page cursor.
 *
 * @param meta the optional protocol metadata
 * @param nextCursor An opaque token representing the pagination position after the last returned
 *     result. If present, there may be more results available.
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPaginatedResult(
    Optional<McpResultMetaObject> meta, Optional<String> nextCursor, String resultType) {
  /** Validates and copies the protocol fields. */
  public McpPaginatedResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(nextCursor, "nextCursor");
    Objects.requireNonNull(resultType, "resultType");
  }

  /**
   * Creates a builder for {@link McpPaginatedResult}.
   *
   * @return a new builder
   */
  public static Builder mcpPaginatedResult() {
    return new Builder();
  }

  /** Builds {@link McpPaginatedResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private Optional<String> nextCursor = Optional.empty();
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
     * Sets {@code nextCursor}.
     *
     * @param nextCursor the optional value
     * @return this builder
     */
    public Builder nextCursor(Optional<String> nextCursor) {
      this.nextCursor = nextCursor;
      return this;
    }

    /**
     * Sets {@code nextCursor}.
     *
     * @param nextCursor the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder nextCursor(String nextCursor) {
      return nextCursor(Optional.ofNullable(nextCursor));
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
    public McpPaginatedResult build() {
      return new McpPaginatedResult(meta, nextCursor, resultType);
    }
  }
}
