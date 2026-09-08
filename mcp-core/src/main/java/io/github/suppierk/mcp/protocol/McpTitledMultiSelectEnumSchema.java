package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Schema for multiple-selection enumeration with display titles for each option.
 *
 * @param defaultValue Optional default value.
 * @param description Optional description for the enum field.
 * @param items Schema for array items with enum options and display labels.
 * @param maxItems Maximum number of items to select.
 * @param minItems Minimum number of items to select.
 * @param title Optional title for the enum field.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpTitledMultiSelectEnumSchema(
    Optional<List<String>> defaultValue,
    Optional<String> description,
    Map<String, ?> items,
    Optional<Long> maxItems,
    Optional<Long> minItems,
    Optional<String> title)
    implements McpEnumSchema, McpMultiSelectEnumSchema, McpPrimitiveSchemaDefinition {

  private static final String TYPE = "array";

  /** Validates and copies the protocol fields. */
  public McpTitledMultiSelectEnumSchema {
    defaultValue = Objects.requireNonNull(defaultValue, "defaultValue").map(List::copyOf);
    Objects.requireNonNull(description, "description");
    items = McpProtocol.copy(Objects.requireNonNull(items, "items"));
    Objects.requireNonNull(maxItems, "maxItems");
    Objects.requireNonNull(minItems, "minItems");
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
   * Creates a builder for {@link McpTitledMultiSelectEnumSchema}.
   *
   * @return a new builder
   */
  public static Builder mcpTitledMultiSelectEnumSchema() {
    return new Builder();
  }

  /** Builds {@link McpTitledMultiSelectEnumSchema} values. */
  public static final class Builder {
    private Optional<List<String>> defaultValue = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Map<String, ?> items;
    private Optional<Long> maxItems = Optional.empty();
    private Optional<Long> minItems = Optional.empty();
    private Optional<String> title = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the optional value
     * @return this builder
     */
    public Builder defaultValue(Optional<List<String>> defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder defaultValue(List<String> defaultValue) {
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
     * Sets {@code items}.
     *
     * @param items the value
     * @return this builder
     */
    public Builder items(Map<String, ?> items) {
      this.items = items;
      return this;
    }

    /**
     * Sets {@code maxItems}.
     *
     * @param maxItems the optional value
     * @return this builder
     */
    public Builder maxItems(Optional<Long> maxItems) {
      this.maxItems = maxItems;
      return this;
    }

    /**
     * Sets {@code maxItems}.
     *
     * @param maxItems the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder maxItems(Long maxItems) {
      return maxItems(Optional.ofNullable(maxItems));
    }

    /**
     * Sets {@code minItems}.
     *
     * @param minItems the optional value
     * @return this builder
     */
    public Builder minItems(Optional<Long> minItems) {
      this.minItems = minItems;
      return this;
    }

    /**
     * Sets {@code minItems}.
     *
     * @param minItems the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder minItems(Long minItems) {
      return minItems(Optional.ofNullable(minItems));
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
    public McpTitledMultiSelectEnumSchema build() {
      return new McpTitledMultiSelectEnumSchema(
          defaultValue, description, items, maxItems, minItems, title);
    }
  }
}
