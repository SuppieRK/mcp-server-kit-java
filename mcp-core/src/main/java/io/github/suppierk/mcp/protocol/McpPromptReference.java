package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Identifies a prompt.
 *
 * @param name the programmatic prompt name and fallback display name
 * @param title the optional human-readable title for a user interface
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPromptReference(String name, Optional<String> title) implements McpBaseMetadata {

  private static final String TYPE = "ref/prompt";

  /** Validates and copies the protocol fields. */
  public McpPromptReference {
    Objects.requireNonNull(name, "name");
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
   * Creates a builder for {@link McpPromptReference}.
   *
   * @return a new builder
   */
  public static Builder mcpPromptReference() {
    return new Builder();
  }

  /** Builds {@link McpPromptReference} values. */
  public static final class Builder {
    private String name;
    private Optional<String> title = Optional.empty();

    private Builder() {}

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
    public McpPromptReference build() {
      return new McpPromptReference(name, title);
    }
  }
}
