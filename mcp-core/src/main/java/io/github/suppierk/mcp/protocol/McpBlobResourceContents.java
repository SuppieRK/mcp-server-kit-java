package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Binary resource contents encoded as Base64 text.
 *
 * @param meta the optional protocol metadata
 * @param blob A base64-encoded string representing the binary data of the item.
 * @param mimeType The MIME type of this resource, if known.
 * @param uri The URI of this resource.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpBlobResourceContents(
    Optional<McpMetaObject> meta, String blob, Optional<String> mimeType, URI uri)
    implements McpResourceContents {
  /** Validates and copies the protocol fields. */
  public McpBlobResourceContents {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(blob, "blob");
    Objects.requireNonNull(mimeType, "mimeType");
    Objects.requireNonNull(uri, "uri");
  }

  /**
   * Creates a builder for {@link McpBlobResourceContents}.
   *
   * @return a new builder
   */
  public static Builder mcpBlobResourceContents() {
    return new Builder();
  }

  /** Builds {@link McpBlobResourceContents} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private String blob;
    private Optional<String> mimeType = Optional.empty();
    private URI uri;

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Sets {@code blob}.
     *
     * @param blob the value
     * @return this builder
     */
    public Builder blob(String blob) {
      this.blob = blob;
      return this;
    }

    /**
     * Sets {@code mimeType}.
     *
     * @param mimeType the optional value
     * @return this builder
     */
    public Builder mimeType(Optional<String> mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    /**
     * Sets {@code mimeType}.
     *
     * @param mimeType the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder mimeType(String mimeType) {
      return mimeType(Optional.ofNullable(mimeType));
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
    public McpBlobResourceContents build() {
      return new McpBlobResourceContents(meta, blob, mimeType, uri);
    }
  }
}
