package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Schema for single-selection enumeration without display titles for options.
 *
 * @param defaultValue Optional default value.
 * @param description Optional description for the enum field.
 * @param values Array of enum values to choose from.
 * @param title Optional title for the enum field.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpUntitledSingleSelectEnumSchema(
    Optional<String> defaultValue,
    Optional<String> description,
    List<String> values,
    Optional<String> title)
    implements McpEnumSchema, McpPrimitiveSchemaDefinition, McpSingleSelectEnumSchema {

  private static final String TYPE = "string";

  /** Validates and copies the protocol fields. */
  public McpUntitledSingleSelectEnumSchema {
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(description, "description");
    values = List.copyOf(values);
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
   * Creates a builder for {@link McpUntitledSingleSelectEnumSchema}.
   *
   * @return a new builder
   */
  public static Builder mcpUntitledSingleSelectEnumSchema() {
    return new Builder();
  }

  /** Builds {@link McpUntitledSingleSelectEnumSchema} values. */
  public static final class Builder {
    private Optional<String> defaultValue = Optional.empty();
    private Optional<String> description = Optional.empty();
    private List<String> values;
    private Optional<String> title = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the optional value
     * @return this builder
     */
    public Builder defaultValue(Optional<String> defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder defaultValue(String defaultValue) {
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
     * Sets {@code values}.
     *
     * @param values the value
     * @return this builder
     */
    public Builder values(List<String> values) {
      this.values = values;
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
     * Builds the value.
     *
     * @return the built value
     */
    public McpUntitledSingleSelectEnumSchema build() {
      return new McpUntitledSingleSelectEnumSchema(defaultValue, description, values, title);
    }
  }
}
