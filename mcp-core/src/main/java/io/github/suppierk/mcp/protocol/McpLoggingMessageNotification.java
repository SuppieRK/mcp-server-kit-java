package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * JSONRPCNotification of a log message passed from server to client. The client opts in by setting
 * {@code "io.modelcontextprotocol/logLevel"} in a request's {@code _meta}.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpLoggingMessageNotification(McpLoggingMessageNotificationParams params)
    implements McpServerNotification, JsonRpcMessage {

  private static final String METHOD_NAME = "notifications/message";

  /** Validates and copies the protocol fields. */
  public McpLoggingMessageNotification {
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
    return METHOD_NAME;
  }

  /**
   * Creates a builder for {@link McpLoggingMessageNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpLoggingMessageNotification() {
    return new Builder();
  }

  /** Builds {@link McpLoggingMessageNotification} values. */
  public static final class Builder {
    private McpLoggingMessageNotificationParams params;

    private Builder() {}

    /**
     * Sets {@code params} using a {@link McpLoggingMessageNotificationParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder params(Consumer<McpLoggingMessageNotificationParams.Builder> configure) {
      var child = McpLoggingMessageNotificationParams.mcpLoggingMessageNotificationParams();
      configure.accept(child);
      return params(child.build());
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpLoggingMessageNotificationParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpLoggingMessageNotification build() {
      return new McpLoggingMessageNotification(params);
    }
  }
}
