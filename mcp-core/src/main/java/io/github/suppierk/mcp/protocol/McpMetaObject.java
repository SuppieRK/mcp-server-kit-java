package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;

/**
 * Additional protocol metadata. A key can contain an optional reverse-DNS prefix and a name. A
 * slash separates the prefix from the name. Prefix labels start with a letter and end with a letter
 * or digit. The second prefix label {@code modelcontextprotocol} or {@code mcp} is reserved for
 * MCP. A non-empty name starts and ends with an alphanumeric character. A name can also contain
 * hyphens, underscores, and dots.
 *
 * @param values the metadata fields
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpMetaObject(Map<String, ?> values) {
  /**
   * Creates metadata from a JSON object.
   *
   * @param values the metadata fields
   */
  public McpMetaObject {
    values = McpProtocol.copy(Objects.requireNonNull(values, "values"));
  }

  /**
   * Returns a copy of the metadata fields.
   *
   * @return the metadata fields
   */
  @Override
  public Map<String, ?> values() {
    return McpProtocol.copy(values);
  }

  /**
   * Creates a builder for {@link McpMetaObject}.
   *
   * @return a new builder
   */
  public static Builder mcpMetaObject() {
    return new Builder();
  }

  /** Builds {@link McpMetaObject} values. */
  public static final class Builder {
    private Map<String, ?> values;

    private Builder() {}

    /**
     * Sets {@code values}.
     *
     * @param values the value
     * @return this builder
     */
    public Builder values(Map<String, ?> values) {
      this.values = values;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpMetaObject build() {
      return new McpMetaObject(values);
    }
  }
}
