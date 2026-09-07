package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Schema for single-selection enumeration with display titles for each option.
 *
 * @param defaultValue Optional default value.
 * @param description Optional description for the enum field.
 * @param oneOf Array of enum options with values and display labels.
 * @param title Optional title for the enum field.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpTitledSingleSelectEnumSchema(
    Optional<String> defaultValue,
    Optional<String> description,
    List<Map<String, ?>> oneOf,
    Optional<String> title)
    implements McpEnumSchema, McpPrimitiveSchemaDefinition, McpSingleSelectEnumSchema {

  private static final String TYPE = "string";

  /** Validates and copies the protocol fields. */
  public McpTitledSingleSelectEnumSchema {
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(description, "description");
    oneOf = McpProtocol.copy(oneOf);
    Objects.requireNonNull(title, "title");
  }

  /**
   * Returns a list that contains copies of the option definitions.
   *
   * @return the copied option definitions
   */
  public List<Map<String, ?>> oneOf() {
    return McpProtocol.copy(oneOf);
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
   * Creates a builder for {@link McpTitledSingleSelectEnumSchema}.
   *
   * @return a new builder
   */
  public static Builder mcpTitledSingleSelectEnumSchema() {
    return new Builder();
  }

  /** Builds {@link McpTitledSingleSelectEnumSchema} values. */
  public static final class Builder {
    private Optional<String> defaultValue = Optional.empty();
    private Optional<String> description = Optional.empty();
    private List<Map<String, ?>> oneOf;
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
     * Sets {@code oneOf}.
     *
     * @param oneOf the value
     * @return this builder
     */
    public Builder oneOf(List<Map<String, ?>> oneOf) {
      this.oneOf = oneOf;
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
    public McpTitledSingleSelectEnumSchema build() {
      return new McpTitledSingleSelectEnumSchema(defaultValue, description, oneOf, title);
    }
  }
}
