package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The response to a {@link McpSubscriptionsListenRequest} ({@code subscriptions/listen}) request,
 * signalling that the subscription has ended gracefully (for example, during server shutdown).
 * Because the listen stream is long-lived, this result is sent only when the server tears the
 * subscription down; an abrupt transport close carries no response. The result body is otherwise
 * empty.
 *
 * @param meta the optional protocol metadata
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionsListenResult(
    McpSubscriptionsListenResultMetaObject meta, String resultType) implements McpServerResult {
  /** Validates and copies the protocol fields. */
  public McpSubscriptionsListenResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(resultType, "resultType");
  }

  /**
   * Creates a builder for {@link McpSubscriptionsListenResult}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionsListenResult() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionsListenResult} values. */
  public static final class Builder {
    private McpSubscriptionsListenResultMetaObject meta;
    private String resultType;

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpSubscriptionsListenResultMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpSubscriptionsListenResultMetaObject.Builder> configure) {
      var child = McpSubscriptionsListenResultMetaObject.mcpSubscriptionsListenResultMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value
     * @return this builder
     */
    public Builder meta(McpSubscriptionsListenResultMetaObject meta) {
      this.meta = meta;
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
    public McpSubscriptionsListenResult build() {
      return new McpSubscriptionsListenResult(meta, resultType);
    }
  }
}
