package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Common params for paginated requests.
 *
 * @param meta the optional protocol metadata
 * @param cursor An opaque token representing the current pagination position. If provided, the
 *     server should return results starting after this cursor.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPaginatedRequestParams(McpRequestMetaObject meta, Optional<String> cursor)
    implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpPaginatedRequestParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(cursor, "cursor");
  }

  /**
   * Creates a builder for {@link McpPaginatedRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpPaginatedRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpPaginatedRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;
    private Optional<String> cursor = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the value
     * @return this builder
     */
    public Builder meta(McpRequestMetaObject meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code cursor}.
     *
     * @param cursor the optional value
     * @return this builder
     */
    public Builder cursor(Optional<String> cursor) {
      this.cursor = cursor;
      return this;
    }

    /**
     * Sets {@code cursor}.
     *
     * @param cursor the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder cursor(String cursor) {
      return cursor(Optional.ofNullable(cursor));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpPaginatedRequestParams build() {
      return new McpPaginatedRequestParams(meta, cursor);
    }
  }
}
