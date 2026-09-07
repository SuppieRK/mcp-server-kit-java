package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * Sent from the client to open a long-lived channel for receiving notifications outside the context
 * of a specific request. Replaces the previous HTTP GET endpoint and ensures consistent behavior
 * between HTTP and STDIO.
 *
 * @param id the JSON-RPC request identifier
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionsListenRequest(Object id, McpSubscriptionsListenRequestParams params)
    implements McpClientRequest {

  /** The request wire-method name. */
  public static final String METHOD = "subscriptions/listen";

  /** Validates and copies the protocol fields. */
  public McpSubscriptionsListenRequest {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
    Objects.requireNonNull(params, "params");
  }

  /**
   * Returns a copy of the request identifier.
   *
   * @return the copied identifier
   */
  public Object id() {
    return McpProtocol.copy(id);
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
   * Creates a builder for {@link McpSubscriptionsListenRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionsListenRequest() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionsListenRequest} values. */
  public static final class Builder {
    private Object id;
    private McpSubscriptionsListenRequestParams params;

    private Builder() {}

    /**
     * Sets {@code id}.
     *
     * @param id the value
     * @return this builder
     */
    public Builder id(Object id) {
      this.id = id;
      return this;
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpSubscriptionsListenRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpSubscriptionsListenRequest build() {
      return new McpSubscriptionsListenRequest(id, params);
    }
  }
}
