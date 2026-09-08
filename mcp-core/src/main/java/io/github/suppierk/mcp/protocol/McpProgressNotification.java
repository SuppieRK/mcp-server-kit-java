package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An out-of-band notification used to inform the receiver of a progress update for a long-running
 * request.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpProgressNotification(McpProgressNotificationParams params)
    implements McpServerNotification, JsonRpcMessage {

  private static final String METHOD_NAME = "notifications/progress";

  /** Validates and copies the protocol fields. */
  public McpProgressNotification {
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
   * Creates a builder for {@link McpProgressNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpProgressNotification() {
    return new Builder();
  }

  /** Builds {@link McpProgressNotification} values. */
  public static final class Builder {
    private McpProgressNotificationParams params;

    private Builder() {}

    /**
     * Sets {@code params} using a {@link McpProgressNotificationParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder params(Consumer<McpProgressNotificationParams.Builder> configure) {
      var child = McpProgressNotificationParams.mcpProgressNotificationParams();
      configure.accept(child);
      return params(child.build());
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpProgressNotificationParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpProgressNotification build() {
      return new McpProgressNotification(params);
    }
  }
}
