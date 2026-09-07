package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Controls tool selection behavior for sampling requests.
 *
 * @param mode the tool-use mode. {@code auto} lets the model decide. {@code required} requires at
 *     least one tool call. {@code none} prohibits tool calls.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpToolChoice(Optional<String> mode) {
  /** Validates and copies the protocol fields. */
  public McpToolChoice {
    Objects.requireNonNull(mode, "mode");
  }

  /**
   * Creates a builder for {@link McpToolChoice}.
   *
   * @return a new builder
   */
  public static Builder mcpToolChoice() {
    return new Builder();
  }

  /** Builds {@link McpToolChoice} values. */
  public static final class Builder {
    private Optional<String> mode = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code mode}.
     *
     * @param mode the optional value
     * @return this builder
     */
    public Builder mode(Optional<String> mode) {
      this.mode = mode;
      return this;
    }

    /**
     * Sets {@code mode}.
     *
     * @param mode the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder mode(String mode) {
      return mode(Optional.ofNullable(mode));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpToolChoice build() {
      return new McpToolChoice(mode);
    }
  }
}
