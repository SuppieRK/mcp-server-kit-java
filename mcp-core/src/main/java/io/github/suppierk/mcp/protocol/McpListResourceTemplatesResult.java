package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The result for a {@link McpListResourceTemplatesRequest} ({@code resources/templates/list})
 * request.
 *
 * @param meta the optional protocol metadata
 * @param cacheScope the cache scope. {@code public} permits reuse across authorization contexts.
 *     {@code private} limits reuse to the same authorization context.
 * @param nextCursor An opaque token representing the pagination position after the last returned
 *     result. If present, there may be more results available.
 * @param resourceTemplates the listed resource templates
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @param ttlMs the cache lifetime in milliseconds. Zero means immediately stale. A positive value
 *     specifies how long the result stays fresh. The value must not be negative.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListResourceTemplatesResult(
    @JsonProperty("_meta") Optional<McpResultMetaObject> meta,
    String cacheScope,
    Optional<String> nextCursor,
    List<McpResourceTemplate> resourceTemplates,
    String resultType,
    Long ttlMs)
    implements McpServerResult {
  /** Validates and copies the protocol fields. */
  public McpListResourceTemplatesResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(cacheScope, "cacheScope");
    Objects.requireNonNull(nextCursor, "nextCursor");
    resourceTemplates = List.copyOf(resourceTemplates);
    Objects.requireNonNull(resultType, "resultType");
    Objects.requireNonNull(ttlMs, "ttlMs");
  }

  /**
   * Creates a builder for {@link McpListResourceTemplatesResult}.
   *
   * @return a new builder
   */
  public static Builder mcpListResourceTemplatesResult() {
    return new Builder();
  }

  /** Builds {@link McpListResourceTemplatesResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private String cacheScope;
    private Optional<String> nextCursor = Optional.empty();
    private List<McpResourceTemplate> resourceTemplates;
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
     * Sets {@code resourceTemplates}.
     *
     * @param resourceTemplates the value
     * @return this builder
     */
    public Builder resourceTemplates(List<McpResourceTemplate> resourceTemplates) {
      this.resourceTemplates = resourceTemplates;
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
    public McpListResourceTemplatesResult build() {
      return new McpListResourceTemplatesResult(
          meta, cacheScope, nextCursor, resourceTemplates, resultType, ttlMs);
    }
  }
}
