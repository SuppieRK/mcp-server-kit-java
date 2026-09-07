package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

/**
 * A string property schema for form elicitation.
 *
 * @param defaultValue the optional default value
 * @param description the optional description
 * @param format the optional string format
 * @param maxLength the optional maximum string length
 * @param minLength the optional minimum string length
 * @param title the optional display title
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpStringSchema(
    @JsonProperty("default") Optional<String> defaultValue,
    Optional<String> description,
    Optional<String> format,
    Optional<Long> maxLength,
    Optional<Long> minLength,
    Optional<String> title)
    implements McpPrimitiveSchemaDefinition {

  private static final String TYPE = "string";

  /** Validates and copies the protocol fields. */
  public McpStringSchema {
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(format, "format");
    Objects.requireNonNull(maxLength, "maxLength");
    Objects.requireNonNull(minLength, "minLength");
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
   * Creates a builder for {@link McpStringSchema}.
   *
   * @return a new builder
   */
  public static Builder mcpStringSchema() {
    return new Builder();
  }

  /** Builds {@link McpStringSchema} values. */
  public static final class Builder {
    private Optional<String> defaultValue = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<String> format = Optional.empty();
    private Optional<Long> maxLength = Optional.empty();
    private Optional<Long> minLength = Optional.empty();
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
     * Sets {@code format}.
     *
     * @param format the optional value
     * @return this builder
     */
    public Builder format(Optional<String> format) {
      this.format = format;
      return this;
    }

    /**
     * Sets {@code format}.
     *
     * @param format the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder format(String format) {
      return format(Optional.ofNullable(format));
    }

    /**
     * Sets {@code maxLength}.
     *
     * @param maxLength the optional value
     * @return this builder
     */
    public Builder maxLength(Optional<Long> maxLength) {
      this.maxLength = maxLength;
      return this;
    }

    /**
     * Sets {@code maxLength}.
     *
     * @param maxLength the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder maxLength(Long maxLength) {
      return maxLength(Optional.ofNullable(maxLength));
    }

    /**
     * Sets {@code minLength}.
     *
     * @param minLength the optional value
     * @return this builder
     */
    public Builder minLength(Optional<Long> minLength) {
      this.minLength = minLength;
      return this;
    }

    /**
     * Sets {@code minLength}.
     *
     * @param minLength the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder minLength(Long minLength) {
      return minLength(Optional.ofNullable(minLength));
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
    public McpStringSchema build() {
      return new McpStringSchema(defaultValue, description, format, maxLength, minLength, title);
    }
  }
}
