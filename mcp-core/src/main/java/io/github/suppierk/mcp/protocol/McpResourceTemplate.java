package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A template description for resources available on the server.
 *
 * @param meta the optional protocol metadata
 * @param annotations optional client annotations
 * @param description the optional resource-template description
 * @param icons optional icons for a user interface
 * @param mimeType the optional media type for matching resources
 * @param name the programmatic template name and fallback display name
 * @param title the optional human-readable title for a user interface
 * @param uriTemplate the RFC 6570 resource URI template
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResourceTemplate(
    @JsonProperty("_meta") Optional<McpMetaObject> meta,
    Optional<McpAnnotations> annotations,
    Optional<String> description,
    Optional<List<McpIcon>> icons,
    Optional<String> mimeType,
    String name,
    Optional<String> title,
    String uriTemplate)
    implements McpBaseMetadata {
  /** Validates and copies the protocol fields. */
  public McpResourceTemplate {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(annotations, "annotations");
    Objects.requireNonNull(description, "description");
    icons = Objects.requireNonNull(icons, "icons").map(List::copyOf);
    Objects.requireNonNull(mimeType, "mimeType");
    if (Objects.requireNonNull(name, "name").isBlank()) {
      throw new IllegalArgumentException("A resource template name must not be blank");
    }
    Objects.requireNonNull(title, "title");
    if (Objects.requireNonNull(uriTemplate, "uriTemplate").isBlank()) {
      throw new IllegalArgumentException("A resource URI template must not be blank");
    }
  }

  /**
   * Creates a minimal resource-template declaration.
   *
   * @param uriTemplate the URI template
   * @param name the template name
   */
  public McpResourceTemplate(String uriTemplate, String name) {
    this(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        name,
        Optional.empty(),
        uriTemplate);
  }

  /**
   * Creates a builder for {@link McpResourceTemplate}.
   *
   * @return a new builder
   */
  public static Builder mcpResourceTemplate() {
    return new Builder();
  }

  /** Builds {@link McpResourceTemplate} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<McpAnnotations> annotations = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<List<McpIcon>> icons = Optional.empty();
    private Optional<String> mimeType = Optional.empty();
    private String name;
    private Optional<String> title = Optional.empty();
    private String uriTemplate;

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
     * Sets {@code description}.
     *
     * @param description the optional value
     * @return this builder
     */
    public Builder description(Optional<String> description) {
      this.description = description;
      return this;
    }

    /**
     * Sets {@code description}.
     *
     * @param description the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder description(String description) {
      return description(Optional.ofNullable(description));
    }

    /**
     * Sets {@code icons}.
     *
     * @param icons the optional value
     * @return this builder
     */
    public Builder icons(Optional<List<McpIcon>> icons) {
      this.icons = icons;
      return this;
    }

    /**
     * Sets {@code icons}.
     *
     * @param icons the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder icons(List<McpIcon> icons) {
      return icons(Optional.ofNullable(icons));
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
     * Sets {@code name}.
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code title}.
     *
     * @param title the optional value
     * @return this builder
     */
    public Builder title(Optional<String> title) {
      this.title = title;
      return this;
    }

    /**
     * Sets {@code title}.
     *
     * @param title the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder title(String title) {
      return title(Optional.ofNullable(title));
    }

    /**
     * Sets {@code uriTemplate}.
     *
     * @param uriTemplate the value
     * @return this builder
     */
    public Builder uriTemplate(String uriTemplate) {
      this.uriTemplate = uriTemplate;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpResourceTemplate build() {
      return new McpResourceTemplate(
          meta, annotations, description, icons, mimeType, name, title, uriTemplate);
    }
  }
}
