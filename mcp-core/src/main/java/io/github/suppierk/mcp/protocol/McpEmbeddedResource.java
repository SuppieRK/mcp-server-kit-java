package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * The contents of a resource, embedded into a prompt or tool call result. It is up to the client
 * how best to render embedded resources for the benefit of the LLM and/or the user.
 *
 * @param meta the optional protocol metadata
 * @param annotations Optional annotations for the client.
 * @param resource the embedded resource
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpEmbeddedResource(
    Optional<McpMetaObject> meta, Optional<McpAnnotations> annotations, Object resource)
    implements McpContentBlock {

  private static final String TYPE = "resource";

  /** Validates and copies the protocol fields. */
  public McpEmbeddedResource {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(annotations, "annotations");
    resource = McpProtocol.copy(Objects.requireNonNull(resource, "resource"));
  }

  /**
   * Returns a copy of the embedded resource.
   *
   * @return the copied resource
   */
  public Object resource() {
    return McpProtocol.copy(resource);
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
   * Creates a builder for {@link McpEmbeddedResource}.
   *
   * @return a new builder
   */
  public static Builder mcpEmbeddedResource() {
    return new Builder();
  }

  /** Builds {@link McpEmbeddedResource} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<McpAnnotations> annotations = Optional.empty();
    private Object resource;

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
     * Sets {@code resource}.
     *
     * @param resource the value
     * @return this builder
     */
    public Builder resource(Object resource) {
      this.resource = resource;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpEmbeddedResource build() {
      return new McpEmbeddedResource(meta, annotations, resource);
    }
  }
}
