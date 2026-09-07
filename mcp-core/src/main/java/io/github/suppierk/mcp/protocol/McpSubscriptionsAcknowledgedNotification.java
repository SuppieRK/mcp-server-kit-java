package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * Sent by the server to acknowledge that a {@link McpSubscriptionsListenRequest} ({@code
 * subscriptions/listen}) subscription has been established and to report which notification types
 * it agreed to honor. This notification MUST be the first message the server sends carrying the
 * subscription's ID in {@code io.modelcontextprotocol/subscriptionId}. The server MUST NOT send any
 * notification on the subscription before acknowledging it. On stdio, where every subscription
 * shares one channel, this ordering is defined per subscription ID and not per channel: messages
 * belonging to other subscriptions MAY be interleaved before it.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionsAcknowledgedNotification(
    McpSubscriptionsAcknowledgedNotificationParams params)
    implements McpServerNotification, JsonRpcMessage {

  private static final String METHOD = "notifications/subscriptions/acknowledged";

  /** Validates and copies the protocol fields. */
  public McpSubscriptionsAcknowledgedNotification {
    Objects.requireNonNull(params, "params");
  }

  /**
   * Gets the constant {@code jsonrpc} value.
   *
   * @return the constant value
   */
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /**
   * Gets the constant {@code method} value.
   *
   * @return the constant value
   */
  public String method() {
    return METHOD;
  }

  /**
   * Creates a builder for {@link McpSubscriptionsAcknowledgedNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionsAcknowledgedNotification() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionsAcknowledgedNotification} values. */
  public static final class Builder {
    private McpSubscriptionsAcknowledgedNotificationParams params;

    private Builder() {}

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpSubscriptionsAcknowledgedNotificationParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpSubscriptionsAcknowledgedNotification build() {
      return new McpSubscriptionsAcknowledgedNotification(params);
    }
  }
}
