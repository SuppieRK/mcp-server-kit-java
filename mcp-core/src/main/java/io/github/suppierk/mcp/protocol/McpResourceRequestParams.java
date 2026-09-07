package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;

/**
 * Common params for resource-related requests.
 *
 * @param meta the optional protocol metadata
 * @param uri The URI of the resource. The URI can use any protocol; it is up to the server how to
 *     interpret it.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResourceRequestParams(@JsonProperty("_meta") McpRequestMetaObject meta, URI uri)
    implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpResourceRequestParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(uri, "uri");
  }

  /**
   * Creates a builder for {@link McpResourceRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpResourceRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpResourceRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;
    private URI uri;

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
     * Sets {@code uri}.
     *
     * @param uri the value
     * @return this builder
     */
    public Builder uri(URI uri) {
      this.uri = uri;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpResourceRequestParams build() {
      return new McpResourceRequestParams(meta, uri);
    }
  }
}
