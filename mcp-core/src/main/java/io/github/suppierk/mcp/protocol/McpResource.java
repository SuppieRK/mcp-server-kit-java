package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A known resource that the server is capable of reading.
 *
 * @param meta the optional protocol metadata
 * @param annotations optional client annotations
 * @param description the optional resource description
 * @param icons optional icons for a user interface
 * @param mimeType the optional resource media type
 * @param name the programmatic resource name and fallback display name
 * @param size the optional raw resource size in bytes
 * @param title the optional human-readable title for a user interface
 * @param uri the resource URI
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResource(
    Optional<McpMetaObject> meta,
    Optional<McpAnnotations> annotations,
    Optional<String> description,
    Optional<List<McpIcon>> icons,
    Optional<String> mimeType,
    String name,
    Optional<Long> size,
    Optional<String> title,
    URI uri)
    implements McpBaseMetadata {
  /** Validates and copies the protocol fields. */
  public McpResource {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(annotations, "annotations");
    Objects.requireNonNull(description, "description");
    icons = Objects.requireNonNull(icons, "icons").map(List::copyOf);
    Objects.requireNonNull(mimeType, "mimeType");
    if (Objects.requireNonNull(name, "name").isBlank()) {
      throw new IllegalArgumentException("A resource name must not be blank");
    }
    Objects.requireNonNull(size, "size");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(uri, "uri");
  }

  /**
   * Creates a minimal resource declaration.
   *
   * @param uri the resource URI
   * @param name the resource name
   */
  public McpResource(URI uri, String name) {
    this(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        name,
        Optional.empty(),
        Optional.empty(),
        uri);
  }

  /**
   * Creates a builder for {@link McpResource}.
   *
   * @return a new builder
   */
  public static Builder mcpResource() {
    return new Builder();
  }

  /** Builds {@link McpResource} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<McpAnnotations> annotations = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<List<McpIcon>> icons = Optional.empty();
    private Optional<String> mimeType = Optional.empty();
    private String name;
    private Optional<Long> size = Optional.empty();
    private Optional<String> title = Optional.empty();
    private URI uri;

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
     * Appends {@code icons} using a {@link McpIcon} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder icon(Consumer<McpIcon.Builder> configure) {
      var child = McpIcon.mcpIcon();
      configure.accept(child);
      var values = new ArrayList<>(this.icons.orElseGet(List::of));
      values.add(child.build());
      return icons(values);
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
     * Sets {@code size}.
     *
     * @param size the optional value
     * @return this builder
     */
    public Builder size(Optional<Long> size) {
      this.size = size;
      return this;
    }

    /**
     * Sets {@code size}.
     *
     * @param size the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder size(Long size) {
      return size(Optional.ofNullable(size));
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
    public McpResource build() {
      return new McpResource(
          meta, annotations, description, icons, mimeType, name, size, title, uri);
    }
  }
}
