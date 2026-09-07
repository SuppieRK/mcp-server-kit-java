package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes an argument that a prompt can accept.
 *
 * @param description the optional argument description
 * @param name the programmatic argument name and fallback display name
 * @param required whether the argument is required
 * @param title the optional human-readable title for a user interface
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPromptArgument(
    Optional<String> description, String name, Optional<Boolean> required, Optional<String> title)
    implements McpBaseMetadata {
  /** Validates and copies the protocol fields. */
  public McpPromptArgument {
    Objects.requireNonNull(description, "description");
    if (Objects.requireNonNull(name, "name").isBlank()) {
      throw new IllegalArgumentException("A prompt argument name must not be blank");
    }
    Objects.requireNonNull(required, "required");
    Objects.requireNonNull(title, "title");
  }

  /**
   * Creates a prompt argument without a title.
   *
   * @param name the argument name
   * @param description the optional description
   * @param required whether the argument is required
   */
  public McpPromptArgument(String name, Optional<String> description, boolean required) {
    this(description, name, Optional.of(required), Optional.empty());
  }

  /**
   * Creates a builder for {@link McpPromptArgument}.
   *
   * @return a new builder
   */
  public static Builder mcpPromptArgument() {
    return new Builder();
  }

  /** Builds {@link McpPromptArgument} values. */
  public static final class Builder {
    private Optional<String> description = Optional.empty();
    private String name;
    private Optional<Boolean> required = Optional.empty();
    private Optional<String> title = Optional.empty();

    private Builder() {}

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
     * Sets {@code name}.
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code required}.
     *
     * @param required the optional value
     * @return this builder
     */
    public Builder required(Optional<Boolean> required) {
      this.required = required;
      return this;
    }

    /**
     * Sets {@code required}.
     *
     * @param required the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder required(Boolean required) {
      return required(Optional.ofNullable(required));
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
    public McpPromptArgument build() {
      return new McpPromptArgument(description, name, required, title);
    }
  }
}
