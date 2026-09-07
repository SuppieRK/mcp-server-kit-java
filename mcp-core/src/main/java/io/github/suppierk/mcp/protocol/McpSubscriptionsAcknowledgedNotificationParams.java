package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Parameters for a {@link McpSubscriptionsAcknowledgedNotification} ({@code
 * notifications/subscriptions/acknowledged}) notification.
 *
 * @param meta the optional protocol metadata
 * @param notifications The subset of requested notification types the server agreed to honor. Only
 *     includes notification types the server actually supports; if the client requested an
 *     unsupported type (e.g., {@code promptsListChanged} when the server has no prompts), it is
 *     omitted from this set.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionsAcknowledgedNotificationParams(
    Optional<McpNotificationMetaObject> meta, McpSubscriptionFilter notifications) {
  /** Validates and copies the protocol fields. */
  public McpSubscriptionsAcknowledgedNotificationParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(notifications, "notifications");
  }

  /**
   * Creates a builder for {@link McpSubscriptionsAcknowledgedNotificationParams}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionsAcknowledgedNotificationParams() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionsAcknowledgedNotificationParams} values. */
  public static final class Builder {
    private Optional<McpNotificationMetaObject> meta = Optional.empty();
    private McpSubscriptionFilter notifications;

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
     * Sets {@code notifications}.
     *
     * @param notifications the value
     * @return this builder
     */
    public Builder notifications(McpSubscriptionFilter notifications) {
      this.notifications = notifications;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpSubscriptionsAcknowledgedNotificationParams build() {
      return new McpSubscriptionsAcknowledgedNotificationParams(meta, notifications);
    }
  }
}
