package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The result returned by the server for a {@link McpListToolsRequest} ({@code tools/list}) request.
 *
 * @param meta the optional protocol metadata
 * @param cacheScope the cache scope. {@code public} permits reuse across authorization contexts.
 *     {@code private} limits reuse to the same authorization context.
 * @param nextCursor An opaque token representing the pagination position after the last returned
 *     result. If present, there may be more results available.
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @param tools the listed tools
 * @param ttlMs the cache lifetime in milliseconds. Zero means immediately stale. A positive value
 *     specifies how long the result stays fresh. The value must not be negative.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListToolsResult(
    Optional<McpResultMetaObject> meta,
    String cacheScope,
    Optional<String> nextCursor,
    String resultType,
    List<Map<String, ?>> tools,
    Long ttlMs)
    implements McpServerResult {
  /** Validates and copies the protocol fields. */
  public McpListToolsResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(cacheScope, "cacheScope");
    Objects.requireNonNull(nextCursor, "nextCursor");
    Objects.requireNonNull(resultType, "resultType");
    tools = McpProtocol.copy(List.copyOf(tools));
    Objects.requireNonNull(ttlMs, "ttlMs");
  }

  /**
   * Creates a builder for {@link McpListToolsResult}.
   *
   * @return a new builder
   */
  public static Builder mcpListToolsResult() {
    return new Builder();
  }

  /** Builds {@link McpListToolsResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private String cacheScope;
    private Optional<String> nextCursor = Optional.empty();
    private String resultType;
    private List<Map<String, ?>> tools;
    private Long ttlMs;

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
     * Sets {@code cacheScope}.
     *
     * @param cacheScope the value
     * @return this builder
     */
    public Builder cacheScope(String cacheScope) {
      this.cacheScope = cacheScope;
      return this;
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
     * Sets {@code tools}.
     *
     * @param tools the value
     * @return this builder
     */
    public Builder tools(List<Map<String, ?>> tools) {
      this.tools = tools;
      return this;
    }

    /**
     * Sets {@code ttlMs}.
     *
     * @param ttlMs the value
     * @return this builder
     */
    public Builder ttlMs(Long ttlMs) {
      this.ttlMs = ttlMs;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpListToolsResult build() {
      return new McpListToolsResult(meta, cacheScope, nextCursor, resultType, tools, ttlMs);
    }
  }
}
