package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Text resource contents.
 *
 * @param meta the optional protocol metadata
 * @param mimeType The MIME type of this resource, if known.
 * @param text The text of the item. This must only be set if the item can actually be represented
 *     as text (not binary data).
 * @param uri The URI of this resource.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpTextResourceContents(
    Optional<McpMetaObject> meta, Optional<String> mimeType, String text, URI uri)
    implements McpResourceContents {
  /** Validates and copies the protocol fields. */
  public McpTextResourceContents {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(mimeType, "mimeType");
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(uri, "uri");
  }

  /**
   * Creates text resource contents.
   *
   * @param uri the resource URI
   * @param text the resource text
   */
  public McpTextResourceContents(URI uri, String text) {
    this(Optional.empty(), Optional.empty(), text, uri);
  }

  /**
   * Creates a builder for {@link McpTextResourceContents}.
   *
   * @return a new builder
   */
  public static Builder mcpTextResourceContents() {
    return new Builder();
  }

  /** Builds {@link McpTextResourceContents} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<String> mimeType = Optional.empty();
    private String text;
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
     * Sets {@code text}.
     *
     * @param text the value
     * @return this builder
     */
    public Builder text(String text) {
      this.text = text;
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
    public McpTextResourceContents build() {
      return new McpTextResourceContents(meta, mimeType, text, uri);
    }
  }
}
