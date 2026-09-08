package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A successful response from the server for a {@link McpCompleteRequest} ({@code
 * completion/complete}) request.
 *
 * @param id the JSON-RPC request identifier
 * @param result the successful result
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCompleteResultResponse(Object id, McpCompleteResult result)
    implements JsonRpcResponse {
  /** Validates and copies the protocol fields. */
  public McpCompleteResultResponse {
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
   * Creates a builder for {@link McpCompleteResultResponse}.
   *
   * @return a new builder
   */
  public static Builder mcpCompleteResultResponse() {
    return new Builder();
  }

  /** Builds {@link McpCompleteResultResponse} values. */
  public static final class Builder {
    private Object id;
    private McpCompleteResult result;

    private Builder() {}

    /**
     * Sets {@code result} using a {@link McpCompleteResult} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder result(Consumer<McpCompleteResult.Builder> configure) {
      var child = McpCompleteResult.mcpCompleteResult();
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
    public Builder result(McpCompleteResult result) {
      this.result = result;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCompleteResultResponse build() {
      return new McpCompleteResultResponse(id, result);
    }
  }
}
