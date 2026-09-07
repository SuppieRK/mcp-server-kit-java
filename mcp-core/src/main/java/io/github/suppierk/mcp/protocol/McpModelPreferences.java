package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Advisory model-selection preferences for sampling. The client may ignore them.
 *
 * @param costPriority optional cost importance from 0 (not important) to 1 (most important)
 * @param hints ordered model hints; clients must evaluate them in order, use the first match, and
 *     should prefer them to numeric priorities
 * @param intelligencePriority optional capability importance from 0 to 1
 * @param speedPriority optional latency importance from 0 to 1
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpModelPreferences(
    Optional<Double> costPriority,
    Optional<List<McpModelHint>> hints,
    Optional<Double> intelligencePriority,
    Optional<Double> speedPriority) {
  /** Validates and copies the protocol fields. */
  public McpModelPreferences {
    Objects.requireNonNull(costPriority, "costPriority");
    hints = Objects.requireNonNull(hints, "hints").map(List::copyOf);
    Objects.requireNonNull(intelligencePriority, "intelligencePriority");
    Objects.requireNonNull(speedPriority, "speedPriority");
  }

  /**
   * Creates a builder for {@link McpModelPreferences}.
   *
   * @return a new builder
   */
  public static Builder mcpModelPreferences() {
    return new Builder();
  }

  /** Builds {@link McpModelPreferences} values. */
  public static final class Builder {
    private Optional<Double> costPriority = Optional.empty();
    private Optional<List<McpModelHint>> hints = Optional.empty();
    private Optional<Double> intelligencePriority = Optional.empty();
    private Optional<Double> speedPriority = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code costPriority}.
     *
     * @param costPriority the optional value
     * @return this builder
     */
    public Builder costPriority(Optional<Double> costPriority) {
      this.costPriority = costPriority;
      return this;
    }

    /**
     * Sets {@code costPriority}.
     *
     * @param costPriority the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder costPriority(Double costPriority) {
      return costPriority(Optional.ofNullable(costPriority));
    }

    /**
     * Sets {@code hints}.
     *
     * @param hints the optional value
     * @return this builder
     */
    public Builder hints(Optional<List<McpModelHint>> hints) {
      this.hints = hints;
      return this;
    }

    /**
     * Sets {@code hints}.
     *
     * @param hints the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder hints(List<McpModelHint> hints) {
      return hints(Optional.ofNullable(hints));
    }

    /**
     * Sets {@code intelligencePriority}.
     *
     * @param intelligencePriority the optional value
     * @return this builder
     */
    public Builder intelligencePriority(Optional<Double> intelligencePriority) {
      this.intelligencePriority = intelligencePriority;
      return this;
    }

    /**
     * Sets {@code intelligencePriority}.
     *
     * @param intelligencePriority the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder intelligencePriority(Double intelligencePriority) {
      return intelligencePriority(Optional.ofNullable(intelligencePriority));
    }

    /**
     * Sets {@code speedPriority}.
     *
     * @param speedPriority the optional value
     * @return this builder
     */
    public Builder speedPriority(Optional<Double> speedPriority) {
      this.speedPriority = speedPriority;
      return this;
    }

    /**
     * Sets {@code speedPriority}.
     *
     * @param speedPriority the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder speedPriority(Double speedPriority) {
      return speedPriority(Optional.ofNullable(speedPriority));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpModelPreferences build() {
      return new McpModelPreferences(costPriority, hints, intelligencePriority, speedPriority);
    }
  }
}
