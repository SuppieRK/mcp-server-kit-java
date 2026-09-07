package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * A notification from the server to the client, informing it that a resource has changed and may
 * need to be read again. This is only sent for resources the client opted in to via the {@code
 * resourceSubscriptions} field of a {@link McpSubscriptionsListenRequest} ({@code
 * subscriptions/listen}) request.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResourceUpdatedNotification(McpResourceUpdatedNotificationParams params)
    implements McpSubscriptionNotification {

  private static final String METHOD = "notifications/resources/updated";

  /** Validates and copies the protocol fields. */
  public McpResourceUpdatedNotification {
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
   * Creates a builder for {@link McpResourceUpdatedNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpResourceUpdatedNotification() {
    return new Builder();
  }

  /** Builds {@link McpResourceUpdatedNotification} values. */
  public static final class Builder {
    private McpResourceUpdatedNotificationParams params;

    private Builder() {}

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpResourceUpdatedNotificationParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpResourceUpdatedNotification build() {
      return new McpResourceUpdatedNotification(params);
    }
  }
}
