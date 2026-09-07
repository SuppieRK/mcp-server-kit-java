package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Parameters for a {@link McpSubscriptionsListenRequest} ({@code subscriptions/listen}) request.
 *
 * @param meta the optional protocol metadata
 * @param notifications the notification types selected by the client. The server must not send a
 *     type that the client did not select.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionsListenRequestParams(
    @JsonProperty("_meta") McpRequestMetaObject meta, McpSubscriptionFilter notifications)
    implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpSubscriptionsListenRequestParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(notifications, "notifications");
  }

  /**
   * Creates a builder for {@link McpSubscriptionsListenRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionsListenRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionsListenRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;
    private McpSubscriptionFilter notifications;

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the value
     * @return this builder
     */
    public Builder meta(McpRequestMetaObject meta) {
      this.meta = meta;
      return this;
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
    public McpSubscriptionsListenRequestParams build() {
      return new McpSubscriptionsListenRequestParams(meta, notifications);
    }
  }
}
