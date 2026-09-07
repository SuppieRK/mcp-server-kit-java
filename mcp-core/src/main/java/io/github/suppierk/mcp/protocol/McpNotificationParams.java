package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Common params for any notification.
 *
 * @param meta the optional protocol metadata
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpNotificationParams(Optional<McpNotificationMetaObject> meta) {
  /** Validates and copies the protocol fields. */
  public McpNotificationParams {
    Objects.requireNonNull(meta, "meta");
  }

  /**
   * Creates a builder for {@link McpNotificationParams}.
   *
   * @return a new builder
   */
  public static Builder mcpNotificationParams() {
    return new Builder();
  }

  /** Builds {@link McpNotificationParams} values. */
  public static final class Builder {
    private Optional<McpNotificationMetaObject> meta = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpNotificationMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpNotificationMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpNotificationParams build() {
      return new McpNotificationParams(meta);
    }
  }
}
