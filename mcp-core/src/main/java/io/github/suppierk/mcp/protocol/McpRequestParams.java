package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * Common params for any request.
 *
 * @param meta the optional protocol metadata
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpRequestParams(McpRequestMetaObject meta) implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpRequestParams {
    Objects.requireNonNull(meta, "meta");
  }

  /**
   * Creates a builder for {@link McpRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;

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
     * Builds the value.
     *
     * @return the built value
     */
    public McpRequestParams build() {
      return new McpRequestParams(meta);
    }
  }
}
