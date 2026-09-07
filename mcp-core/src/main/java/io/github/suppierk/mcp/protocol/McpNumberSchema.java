package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

/**
 * A numeric property schema for form elicitation.
 *
 * @param defaultValue the optional default value
 * @param description the optional description
 * @param maximum the optional maximum numeric value
 * @param minimum the optional minimum numeric value
 * @param title the optional display title
 * @param type the numeric schema type
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpNumberSchema(
    @JsonProperty("default") Optional<Double> defaultValue,
    Optional<String> description,
    Optional<Double> maximum,
    Optional<Double> minimum,
    Optional<String> title,
    String type)
    implements McpPrimitiveSchemaDefinition {
  /** Validates and copies the protocol fields. */
  public McpNumberSchema {
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(maximum, "maximum");
    Objects.requireNonNull(minimum, "minimum");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(type, "type");
  }

  /**
   * Creates a builder for {@link McpNumberSchema}.
   *
   * @return a new builder
   */
  public static Builder mcpNumberSchema() {
    return new Builder();
  }

  /** Builds {@link McpNumberSchema} values. */
  public static final class Builder {
    private Optional<Double> defaultValue = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<Double> maximum = Optional.empty();
    private Optional<Double> minimum = Optional.empty();
    private Optional<String> title = Optional.empty();
    private String type;

    private Builder() {}

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the optional value
     * @return this builder
     */
    public Builder defaultValue(Optional<Double> defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    /**
     * Sets {@code defaultValue}.
     *
     * @param defaultValue the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder defaultValue(Double defaultValue) {
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
     * Sets {@code maximum}.
     *
     * @param maximum the optional value
     * @return this builder
     */
    public Builder maximum(Optional<Double> maximum) {
      this.maximum = maximum;
      return this;
    }

    /**
     * Sets {@code maximum}.
     *
     * @param maximum the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder maximum(Double maximum) {
      return maximum(Optional.ofNullable(maximum));
    }

    /**
     * Sets {@code minimum}.
     *
     * @param minimum the optional value
     * @return this builder
     */
    public Builder minimum(Optional<Double> minimum) {
      this.minimum = minimum;
      return this;
    }

    /**
     * Sets {@code minimum}.
     *
     * @param minimum the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder minimum(Double minimum) {
      return minimum(Optional.ofNullable(minimum));
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
     * Sets {@code type}.
     *
     * @param type the value
     * @return this builder
     */
    public Builder type(String type) {
      this.type = type;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpNumberSchema build() {
      return new McpNumberSchema(defaultValue, description, maximum, minimum, title, type);
    }
  }
}
