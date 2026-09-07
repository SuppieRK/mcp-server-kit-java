package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Extends {@code MetaObject} with additional notification-specific fields. All key naming rules
 * from {@code MetaObject} apply.
 *
 * @param subscriptionId the optional subscription identifier
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpNotificationMetaObject(Optional<Object> subscriptionId) {
  /** Validates and copies the protocol fields. */
  public McpNotificationMetaObject {
    subscriptionId =
        Objects.requireNonNull(subscriptionId, "subscriptionId").map(McpProtocol::copy);
  }

  /**
   * Returns a copy of the optional subscription identifier.
   *
   * @return the subscription identifier
   */
  @Override
  public Optional<Object> subscriptionId() {
    return subscriptionId.map(McpProtocol::copy);
  }

  /**
   * Creates a builder for {@link McpNotificationMetaObject}.
   *
   * @return a new builder
   */
  public static Builder mcpNotificationMetaObject() {
    return new Builder();
  }

  /** Builds {@link McpNotificationMetaObject} values. */
  public static final class Builder {
    private Optional<Object> subscriptionId = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code subscriptionId}.
     *
     * @param subscriptionId the optional value
     * @return this builder
     */
    public Builder subscriptionId(Optional<Object> subscriptionId) {
      this.subscriptionId = subscriptionId;
      return this;
    }

    /**
     * Sets {@code subscriptionId}.
     *
     * @param subscriptionId the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder subscriptionId(Object subscriptionId) {
      return subscriptionId(Optional.ofNullable(subscriptionId));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpNotificationMetaObject build() {
      return new McpNotificationMetaObject(subscriptionId);
    }
  }
}
