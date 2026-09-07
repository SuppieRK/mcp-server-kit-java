package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * A Boolean property schema for form elicitation.
 *
 * @param defaultValue the optional default value
 * @param description the optional description
 * @param title the optional display title
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpBooleanSchema(
    Optional<Boolean> defaultValue, Optional<String> description, Optional<String> title)
    implements McpPrimitiveSchemaDefinition {

  private static final String TYPE = "boolean";

  /** Validates and copies the protocol fields. */
  public McpBooleanSchema {
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(title, "title");
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
   * Creates a builder for {@link McpBooleanSchema}.
   *
   * @return a new builder
   */
  public static Builder mcpBooleanSchema() {
    return new Builder();
  }

  /** Builds {@link McpBooleanSchema} values. */
  public static final class Builder {
    private Optional<Boolean> defaultValue = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<String> title = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the optional value
     * @return this builder
     */
    public Builder defaultValue(Optional<Boolean> defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder defaultValue(Boolean defaultValue) {
      return defaultValue(Optional.ofNullable(defaultValue));
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
     * Builds the value.
     *
     * @return the built value
     */
    public McpBooleanSchema build() {
      return new McpBooleanSchema(defaultValue, description, title);
    }
  }
}
