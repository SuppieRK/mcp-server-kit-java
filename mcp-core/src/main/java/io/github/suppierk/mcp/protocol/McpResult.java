package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Common result fields.
 *
 * @param meta the optional protocol metadata
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResult(Optional<McpResultMetaObject> meta, String resultType)
    implements McpServerResult {
  /** Validates and copies the protocol fields. */
  public McpResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(resultType, "resultType");
  }

  /**
   * Creates a builder for {@link McpResult}.
   *
   * @return a new builder
   */
  public static Builder mcpResult() {
    return new Builder();
  }

  /** Builds {@link McpResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
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
    public McpResult build() {
      return new McpResult(meta, resultType);
    }
  }
}
