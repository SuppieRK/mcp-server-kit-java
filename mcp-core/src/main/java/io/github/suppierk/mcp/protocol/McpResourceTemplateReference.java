package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * A reference to a resource or resource template definition.
 *
 * @param uri The URI or URI template of the resource.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResourceTemplateReference(String uri) {

  private static final String TYPE = "ref/resource";

  /** Validates and copies the protocol fields. */
  public McpResourceTemplateReference {
    Objects.requireNonNull(uri, "uri");
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
   * Creates a builder for {@link McpResourceTemplateReference}.
   *
   * @return a new builder
   */
  public static Builder mcpResourceTemplateReference() {
    return new Builder();
  }

  /** Builds {@link McpResourceTemplateReference} values. */
  public static final class Builder {
    private String uri;

    private Builder() {}

    /**
     * Sets {@code uri}.
     *
     * @param uri the value
     * @return this builder
     */
    public Builder uri(String uri) {
      this.uri = uri;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpResourceTemplateReference build() {
      return new McpResourceTemplateReference(uri);
    }
  }
}
