package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This notification is sent by the client to indicate that it is cancelling a request it previously
 * issued. On stdio, the server also sends this notification to terminate a {@link
 * McpSubscriptionsListenRequest} ({@code subscriptions/listen}) stream. It references the ID of the
 * {@code subscriptions/listen} request that opened the stream. Servers MUST NOT use this
 * notification to cancel any other request. The request SHOULD still be in-flight, but due to
 * communication latency, it is always possible that this notification MAY arrive after the request
 * has already finished. This notification indicates that the result will be unused, so any
 * associated processing SHOULD cease.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpClientNotification(McpCancelledNotificationParams params)
    implements JsonRpcMessage {

  /** The notification wire-method name. */
  public static final String METHOD = "notifications/cancelled";

  /** Validates and copies the protocol fields. */
  public McpClientNotification {
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
   * Creates a builder for {@link McpClientNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpClientNotification() {
    return new Builder();
  }

  /** Builds {@link McpClientNotification} values. */
  public static final class Builder {
    private McpCancelledNotificationParams params;

    private Builder() {}

    /**
     * Sets {@code params} using a {@link McpCancelledNotificationParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder params(Consumer<McpCancelledNotificationParams.Builder> configure) {
      var child = McpCancelledNotificationParams.mcpCancelledNotificationParams();
      configure.accept(child);
      return params(child.build());
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpCancelledNotificationParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpClientNotification build() {
      return new McpClientNotification(params);
    }
  }
}
