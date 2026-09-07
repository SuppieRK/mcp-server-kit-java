package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Optional presentation and prioritization hints for clients.
 *
 * @param audience the intended audiences
 * @param lastModified the optional ISO 8601 modification timestamp
 * @param priority the optional finite priority from 0 (least important) to 1 (most important)
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpAnnotations(
    Optional<List<McpRole>> audience, Optional<String> lastModified, Optional<Double> priority) {
  /**
   * Validates and copies the protocol fields.
   *
   * @throws NullPointerException if an optional value is {@code null}
   * @throws IllegalArgumentException if the priority is not a finite number from 0 through 1
   */
  public McpAnnotations {
    audience = Objects.requireNonNull(audience, "audience").map(List::copyOf);
    Objects.requireNonNull(lastModified, "lastModified");
    Objects.requireNonNull(priority, "priority");
    if (priority.isPresent()) {
      double value = priority.orElseThrow();
      if (!Double.isFinite(value) || value < 0D || value > 1D) {
        throw new IllegalArgumentException(
            "An annotation priority must be a finite number from 0 through 1");
      }
    }
  }

  /**
   * Creates a builder for {@link McpAnnotations}.
   *
   * @return a new builder
   */
  public static Builder mcpAnnotations() {
    return new Builder();
  }

  /** Builds {@link McpAnnotations} values. */
  public static final class Builder {
    private Optional<List<McpRole>> audience = Optional.empty();
    private Optional<String> lastModified = Optional.empty();
    private Optional<Double> priority = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code audience}.
     *
     * @param audience the optional value
     * @return this builder
     */
    public Builder audience(Optional<List<McpRole>> audience) {
      this.audience = audience;
      return this;
    }

    /**
     * Sets {@code audience}.
     *
     * @param audience the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder audience(List<McpRole> audience) {
      return audience(Optional.ofNullable(audience));
    }

    /**
     * Sets {@code lastModified}.
     *
     * @param lastModified the optional value
     * @return this builder
     */
    public Builder lastModified(Optional<String> lastModified) {
      this.lastModified = lastModified;
      return this;
    }

    /**
     * Sets {@code lastModified}.
     *
     * @param lastModified the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder lastModified(String lastModified) {
      return lastModified(Optional.ofNullable(lastModified));
    }

    /**
     * Sets {@code priority}.
     *
     * @param priority the optional value
     * @return this builder
     */
    public Builder priority(Optional<Double> priority) {
      this.priority = priority;
      return this;
    }

    /**
     * Sets {@code priority}.
     *
     * @param priority the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder priority(Double priority) {
      return priority(Optional.ofNullable(priority));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpAnnotations build() {
      return new McpAnnotations(audience, lastModified, priority);
    }
  }
}
