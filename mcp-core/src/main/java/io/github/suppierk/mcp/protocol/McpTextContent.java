package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Text provided to or from an LLM.
 *
 * @param meta the optional protocol metadata
 * @param annotations Optional annotations for the client.
 * @param text The text content of the message.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpTextContent(
    Optional<McpMetaObject> meta, Optional<McpAnnotations> annotations, String text)
    implements McpContentBlock, McpSamplingMessageContentBlock {

  private static final String TYPE = "text";

  /** Validates and copies the protocol fields. */
  public McpTextContent {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(annotations, "annotations");
    Objects.requireNonNull(text, "text");
  }

  /**
   * Creates plain text content.
   *
   * @param text the text
   */
  public McpTextContent(String text) {
    this(Optional.empty(), Optional.empty(), text);
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
   * Creates a builder for {@link McpTextContent}.
   *
   * @return a new builder
   */
  public static Builder mcpTextContent() {
    return new Builder();
  }

  /** Builds {@link McpTextContent} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<McpAnnotations> annotations = Optional.empty();
    private String text;

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpMetaObject.Builder> configure) {
      var child = McpMetaObject.mcpMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Sets {@code annotations} using a {@link McpAnnotations} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder annotations(Consumer<McpAnnotations.Builder> configure) {
      var child = McpAnnotations.mcpAnnotations();
      configure.accept(child);
      return annotations(child.build());
    }

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
     * Builds the value.
     *
     * @return the built value
     */
    public McpTextContent build() {
      return new McpTextContent(meta, annotations, text);
    }
  }
}
