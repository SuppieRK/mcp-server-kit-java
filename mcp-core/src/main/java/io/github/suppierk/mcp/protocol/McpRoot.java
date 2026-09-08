package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a root directory or file that the server can operate on.
 *
 * @param meta the optional protocol metadata
 * @param name the optional display name
 * @param uri the root URI; this protocol revision requires a {@code file://} URI
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpRoot(Optional<McpMetaObject> meta, Optional<String> name, URI uri) {
  /** Validates and copies the protocol fields. */
  public McpRoot {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(uri, "uri");
  }

  /**
   * Creates a builder for {@link McpRoot}.
   *
   * @return a new builder
   */
  public static Builder mcpRoot() {
    return new Builder();
  }

  /** Builds {@link McpRoot} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<String> name = Optional.empty();
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
     * Sets {@code name}.
     *
     * @param name the optional value
     * @return this builder
     */
    public Builder name(Optional<String> name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code name}.
     *
     * @param name the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder name(String name) {
      return name(Optional.ofNullable(name));
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
    public McpRoot build() {
      return new McpRoot(meta, name, uri);
    }
  }
}
