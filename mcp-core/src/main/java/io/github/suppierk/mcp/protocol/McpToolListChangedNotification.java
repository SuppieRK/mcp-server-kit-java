package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An optional notification from the server to the client, informing it that the list of tools it
 * offers has changed. The server sends it on a {@link McpSubscriptionsListenRequest} ({@code
 * subscriptions/listen}) stream only when the client selects the {@code toolsListChanged} filter
 * field.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpToolListChangedNotification(Optional<McpNotificationParams> params)
    implements McpSubscriptionNotification {

  private static final String METHOD = "notifications/tools/list_changed";

  /** Validates and copies the protocol fields. */
  public McpToolListChangedNotification {
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
   * Creates a builder for {@link McpToolListChangedNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpToolListChangedNotification() {
    return new Builder();
  }

  /** Builds {@link McpToolListChangedNotification} values. */
  public static final class Builder {
    private Optional<McpNotificationParams> params = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code params} using a {@link McpNotificationParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder params(Consumer<McpNotificationParams.Builder> configure) {
      var child = McpNotificationParams.mcpNotificationParams();
      configure.accept(child);
      return params(child.build());
    }

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
    public McpToolListChangedNotification build() {
      return new McpToolListChangedNotification(params);
    }
  }
}
