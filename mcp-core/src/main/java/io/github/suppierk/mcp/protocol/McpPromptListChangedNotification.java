package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

/**
 * An optional notification from the server to the client, informing it that the list of prompts it
 * offers has changed. The server sends it on a {@link McpSubscriptionsListenRequest} ({@code
 * subscriptions/listen}) stream only when the client selects the {@code promptsListChanged} filter
 * field.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPromptListChangedNotification(Optional<McpNotificationParams> params)
    implements McpSubscriptionNotification {

  private static final String METHOD = "notifications/prompts/list_changed";

  /** Validates and copies the protocol fields. */
  public McpPromptListChangedNotification {
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
   * Creates a builder for {@link McpPromptListChangedNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpPromptListChangedNotification() {
    return new Builder();
  }

  /** Builds {@link McpPromptListChangedNotification} values. */
  public static final class Builder {
    private Optional<McpNotificationParams> params = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code params}.
     *
     * @param params the optional value
     * @return this builder
     */
    public Builder params(Optional<McpNotificationParams> params) {
      this.params = params;
      return this;
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder params(McpNotificationParams params) {
      return params(Optional.ofNullable(params));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpPromptListChangedNotification build() {
      return new McpPromptListChangedNotification(params);
    }
  }
}
