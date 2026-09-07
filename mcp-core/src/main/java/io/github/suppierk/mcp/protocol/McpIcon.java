package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An optionally-sized icon that can be displayed in a user interface.
 *
 * @param mimeType Optional MIME type override if the source MIME type is missing or generic. For
 *     example: {@code "image/png"}, {@code "image/jpeg"}, or {@code "image/svg+xml"}.
 * @param sizes Optional array of strings that specify sizes at which the icon can be used. Each
 *     string should be in WxH format (e.g., {@code "48x48"}, {@code "96x96"}) or {@code "any"} for
 *     scalable formats like SVG. If not provided, the client should assume that the icon can be
 *     used at any size.
 * @param src A standard URI pointing to an icon resource. May be an HTTP/HTTPS URL or a {@code
 *     data:} URI with Base64-encoded image data. Consumers SHOULD take steps to ensure URLs serving
 *     icons are from the same domain as the client/server or a trusted domain. Consumers SHOULD
 *     take appropriate precautions when consuming SVGs as they can contain executable JavaScript.
 * @param theme Optional specifier for the theme this icon is designed for. {@code "light"}
 *     indicates the icon is designed to be used with a light background, and {@code "dark"}
 *     indicates the icon is designed to be used with a dark background. If not provided, the client
 *     should assume the icon can be used with any theme.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpIcon(
    Optional<String> mimeType, Optional<List<String>> sizes, URI src, Optional<String> theme) {
  /** Validates and copies the protocol fields. */
  public McpIcon {
    Objects.requireNonNull(mimeType, "mimeType");
    sizes = Objects.requireNonNull(sizes, "sizes").map(List::copyOf);
    Objects.requireNonNull(src, "src");
    Objects.requireNonNull(theme, "theme");
    if (theme.isPresent() && !"light".equals(theme.get()) && !"dark".equals(theme.get())) {
      throw new IllegalArgumentException("An icon theme must be light or dark");
    }
  }

  /**
   * Creates an icon without optional hints.
   *
   * @param source the icon URI
   */
  public McpIcon(URI source) {
    this(Optional.empty(), Optional.empty(), source, Optional.empty());
  }

  /**
   * Creates a builder for {@link McpIcon}.
   *
   * @return a new builder
   */
  public static Builder mcpIcon() {
    return new Builder();
  }

  /** Builds {@link McpIcon} values. */
  public static final class Builder {
    private Optional<String> mimeType = Optional.empty();
    private Optional<List<String>> sizes = Optional.empty();
    private URI src;
    private Optional<String> theme = Optional.empty();

    private Builder() {}

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
     * Sets {@code sizes}.
     *
     * @param sizes the optional value
     * @return this builder
     */
    public Builder sizes(Optional<List<String>> sizes) {
      this.sizes = sizes;
      return this;
    }

    /**
     * Sets {@code sizes}.
     *
     * @param sizes the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder sizes(List<String> sizes) {
      return sizes(Optional.ofNullable(sizes));
    }

    /**
     * Sets {@code src}.
     *
     * @param src the value
     * @return this builder
     */
    public Builder src(URI src) {
      this.src = src;
      return this;
    }

    /**
     * Sets {@code theme}.
     *
     * @param theme the optional value
     * @return this builder
     */
    public Builder theme(Optional<String> theme) {
      this.theme = theme;
      return this;
    }

    /**
     * Sets {@code theme}.
     *
     * @param theme the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder theme(String theme) {
      return theme(Optional.ofNullable(theme));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpIcon build() {
      return new McpIcon(mimeType, sizes, src, theme);
    }
  }
}
