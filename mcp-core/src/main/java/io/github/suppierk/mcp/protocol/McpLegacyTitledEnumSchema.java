package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Use {@code TitledSingleSelectEnumSchema} instead. This interface will be removed in a future
 * version.
 *
 * @param defaultValue the optional default value
 * @param description the optional description
 * @param values the allowed values
 * @param enumNames (Legacy) Display names for enum values. Non-standard according to JSON schema
 *     2020-12.
 * @param title the optional display title
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpLegacyTitledEnumSchema(
    @JsonProperty("default") Optional<String> defaultValue,
    Optional<String> description,
    @JsonProperty("enum") List<String> values,
    Optional<List<String>> enumNames,
    Optional<String> title)
    implements McpEnumSchema, McpPrimitiveSchemaDefinition {

  private static final String TYPE = "string";

  /** Validates and copies the protocol fields. */
  public McpLegacyTitledEnumSchema {
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(description, "description");
    values = List.copyOf(values);
    enumNames = Objects.requireNonNull(enumNames, "enumNames").map(List::copyOf);
    Objects.requireNonNull(title, "title");
  }

  /**
   * Gets the constant {@code type} value.
   *
   * @return the constant value
   */
  @JsonProperty("type")
  public String type() {
    return TYPE;
  }

  /**
   * Creates a builder for {@link McpLegacyTitledEnumSchema}.
   *
   * @return a new builder
   */
  public static Builder mcpLegacyTitledEnumSchema() {
    return new Builder();
  }

  /** Builds {@link McpLegacyTitledEnumSchema} values. */
  public static final class Builder {
    private Optional<String> defaultValue = Optional.empty();
    private Optional<String> description = Optional.empty();
    private List<String> values;
    private Optional<List<String>> enumNames = Optional.empty();
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
     * Sets {@code enumNames}.
     *
     * @param enumNames the optional value
     * @return this builder
     */
    public Builder enumNames(Optional<List<String>> enumNames) {
      this.enumNames = enumNames;
      return this;
    }

    /**
     * Sets {@code enumNames}.
     *
     * @param enumNames the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder enumNames(List<String> enumNames) {
      return enumNames(Optional.ofNullable(enumNames));
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
    public McpLegacyTitledEnumSchema build() {
      return new McpLegacyTitledEnumSchema(defaultValue, description, values, enumNames, title);
    }
  }
}
