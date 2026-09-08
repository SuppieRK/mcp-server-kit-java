package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Describes the MCP implementation.
 *
 * @param description the optional implementation description
 * @param icons optional icons for a user interface
 * @param name the programmatic implementation name and fallback display name
 * @param title the optional human-readable title for a user interface
 * @param version the implementation version
 * @param websiteUrl the optional implementation website
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpImplementation(
    Optional<String> description,
    Optional<List<McpIcon>> icons,
    String name,
    Optional<String> title,
    String version,
    Optional<URI> websiteUrl)
    implements McpBaseMetadata {
  /** Validates and copies the protocol fields. */
  public McpImplementation {
    Objects.requireNonNull(description, "description");
    icons = Objects.requireNonNull(icons, "icons").map(List::copyOf);
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(websiteUrl, "websiteUrl");
  }

  /**
   * Creates a builder for {@link McpImplementation}.
   *
   * @return a new builder
   */
  public static Builder mcpImplementation() {
    return new Builder();
  }

  /** Builds {@link McpImplementation} values. */
  public static final class Builder {
    private Optional<String> description = Optional.empty();
    private Optional<List<McpIcon>> icons = Optional.empty();
    private String name;
    private Optional<String> title = Optional.empty();
    private String version;
    private Optional<URI> websiteUrl = Optional.empty();

    private Builder() {}

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
     * Sets {@code version}.
     *
     * @param version the value
     * @return this builder
     */
    public Builder version(String version) {
      this.version = version;
      return this;
    }

    /**
     * Sets {@code websiteUrl}.
     *
     * @param websiteUrl the optional value
     * @return this builder
     */
    public Builder websiteUrl(Optional<URI> websiteUrl) {
      this.websiteUrl = websiteUrl;
      return this;
    }

    /**
     * Sets {@code websiteUrl}.
     *
     * @param websiteUrl the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder websiteUrl(URI websiteUrl) {
      return websiteUrl(Optional.ofNullable(websiteUrl));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpImplementation build() {
      return new McpImplementation(description, icons, name, title, version, websiteUrl);
    }
  }
}
