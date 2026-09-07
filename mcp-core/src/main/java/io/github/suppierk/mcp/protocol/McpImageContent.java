package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * An image provided to or from an LLM.
 *
 * @param meta the optional protocol metadata
 * @param annotations Optional annotations for the client.
 * @param data The base64-encoded image data.
 * @param mimeType The MIME type of the image. Different providers may support different image
 *     types.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpImageContent(
    Optional<McpMetaObject> meta,
    Optional<McpAnnotations> annotations,
    String data,
    String mimeType)
    implements McpContentBlock, McpSamplingMessageContentBlock {

  private static final String TYPE = "image";

  /** Validates and copies the protocol fields. */
  public McpImageContent {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(annotations, "annotations");
    Objects.requireNonNull(data, "data");
    Objects.requireNonNull(mimeType, "mimeType");
  }

  /**
   * Gets the constant {@code type} value.
   *
   * @return the constant value
   */
  public String type() {
    return TYPE;
  }

  /**
   * Creates a builder for {@link McpImageContent}.
   *
   * @return a new builder
   */
  public static Builder mcpImageContent() {
    return new Builder();
  }

  /** Builds {@link McpImageContent} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<McpAnnotations> annotations = Optional.empty();
    private String data;
    private String mimeType;

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
     * Sets {@code annotations}.
     *
     * @param annotations the optional value
     * @return this builder
     */
    public Builder annotations(Optional<McpAnnotations> annotations) {
      this.annotations = annotations;
      return this;
    }

    /**
     * Sets {@code annotations}.
     *
     * @param annotations the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder annotations(McpAnnotations annotations) {
      return annotations(Optional.ofNullable(annotations));
    }

    /**
     * Sets {@code data}.
     *
     * @param data the value
     * @return this builder
     */
    public Builder data(String data) {
      this.data = data;
      return this;
    }

    /**
     * Sets {@code mimeType}.
     *
     * @param mimeType the value
     * @return this builder
     */
    public Builder mimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpImageContent build() {
      return new McpImageContent(meta, annotations, data, mimeType);
    }
  }
}
