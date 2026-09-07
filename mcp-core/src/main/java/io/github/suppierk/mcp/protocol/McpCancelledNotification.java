package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

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
public record McpCancelledNotification(McpCancelledNotificationParams params)
    implements McpServerNotification, JsonRpcMessage {

  private static final String METHOD = "notifications/cancelled";

  /** Validates and copies the protocol fields. */
  public McpCancelledNotification {
    Objects.requireNonNull(params, "params");
  }

  /**
   * Gets the constant {@code jsonrpc} value.
   *
   * @return the constant value
   */
  @JsonProperty("jsonrpc")
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /**
   * Gets the constant {@code method} value.
   *
   * @return the constant value
   */
  @JsonProperty("method")
  public String method() {
    return METHOD;
  }

  /**
   * Creates a builder for {@link McpCancelledNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpCancelledNotification() {
    return new Builder();
  }

  /** Builds {@link McpCancelledNotification} values. */
  public static final class Builder {
    private McpCancelledNotificationParams params;

    private Builder() {}

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
    public McpCancelledNotification build() {
      return new McpCancelledNotification(params);
    }
  }
}
