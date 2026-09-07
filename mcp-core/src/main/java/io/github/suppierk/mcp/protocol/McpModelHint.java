package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Hints to use for model selection. Keys not declared here are currently left unspecified by the
 * spec and are up to the client to interpret.
 *
 * @param name a model-name hint. A client should match it as a substring of a model name. For
 *     example, {@code sonnet} matches {@code claude-3-5-sonnet-20241022}. A client can also map the
 *     hint to a similar model from another provider.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpModelHint(Optional<String> name) {
  /** Validates and copies the protocol fields. */
  public McpModelHint {
    Objects.requireNonNull(name, "name");
  }

  /**
   * Creates a builder for {@link McpModelHint}.
   *
   * @return a new builder
   */
  public static Builder mcpModelHint() {
    return new Builder();
  }

  /** Builds {@link McpModelHint} values. */
  public static final class Builder {
    private Optional<String> name = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code name}.
     *
     * @param name the optional value
     * @return this builder
     */
    public Builder name(Optional<String> name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code name}.
     *
     * @param name the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder name(String name) {
      return name(Optional.ofNullable(name));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpModelHint build() {
      return new McpModelHint(name);
    }
  }
}
