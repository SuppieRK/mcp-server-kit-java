package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A successful response for a {@link McpSubscriptionsListenRequest} ({@code subscriptions/listen})
 * request. The server sends it when it closes the subscription down gracefully.
 *
 * @param id the JSON-RPC request identifier
 * @param result the successful result
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionsListenResultResponse(Object id, McpSubscriptionsListenResult result)
    implements JsonRpcResponse {
  /** Validates and copies the protocol fields. */
  public McpSubscriptionsListenResultResponse {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
    Objects.requireNonNull(result, "result");
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
   * Creates a builder for {@link McpSubscriptionsListenResultResponse}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionsListenResultResponse() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionsListenResultResponse} values. */
  public static final class Builder {
    private Object id;
    private McpSubscriptionsListenResult result;

    private Builder() {}

    /**
     * Sets {@code result} using a {@link McpSubscriptionsListenResult} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder result(Consumer<McpSubscriptionsListenResult.Builder> configure) {
      var child = McpSubscriptionsListenResult.mcpSubscriptionsListenResult();
      configure.accept(child);
      return result(child.build());
    }

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
     * Sets {@code result}.
     *
     * @param result the value
     * @return this builder
     */
    public Builder result(McpSubscriptionsListenResult result) {
      this.result = result;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpSubscriptionsListenResultResponse build() {
      return new McpSubscriptionsListenResultResponse(id, result);
    }
  }
}
