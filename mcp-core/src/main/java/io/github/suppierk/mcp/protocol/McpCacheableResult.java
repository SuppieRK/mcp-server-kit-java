package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

/**
 * A result that supports a time-to-live (TTL) hint for client-side caching.
 *
 * @param meta the optional protocol metadata
 * @param cacheScope the cache scope. {@code public} permits reuse across authorization contexts.
 *     {@code private} limits reuse to the same authorization context.
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @param ttlMs the cache lifetime in milliseconds. Zero means immediately stale. A positive value
 *     specifies how long the result stays fresh. The value must not be negative.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCacheableResult(
    @JsonProperty("_meta") Optional<McpResultMetaObject> meta,
    String cacheScope,
    String resultType,
    Long ttlMs) {
  /** The cache scope for one authorization context. */
  public static final String CACHE_SCOPE_PRIVATE = "private";

  /** The cache scope for all authorization contexts. */
  public static final String CACHE_SCOPE_PUBLIC = "public";

  /**
   * Validates and copies the protocol fields.
   *
   * @throws NullPointerException if a required value or optional container is {@code null}
   * @throws IllegalArgumentException if the cache scope is invalid or the cache lifetime is
   *     negative
   */
  public McpCacheableResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(cacheScope, "cacheScope");
    Objects.requireNonNull(resultType, "resultType");
    Objects.requireNonNull(ttlMs, "ttlMs");
    if (!CACHE_SCOPE_PRIVATE.equals(cacheScope) && !CACHE_SCOPE_PUBLIC.equals(cacheScope)) {
      throw new IllegalArgumentException("A cache scope must be private or public");
    }
    if (ttlMs < 0L) {
      throw new IllegalArgumentException("A cache lifetime must not be negative");
    }
  }

  /**
   * Creates a builder for {@link McpCacheableResult}.
   *
   * @return a new builder
   */
  public static Builder mcpCacheableResult() {
    return new Builder();
  }

  /** Builds {@link McpCacheableResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private String cacheScope;
    private String resultType;
    private Long ttlMs;

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
    public McpCacheableResult build() {
      return new McpCacheableResult(meta, cacheScope, resultType, ttlMs);
    }
  }
}
