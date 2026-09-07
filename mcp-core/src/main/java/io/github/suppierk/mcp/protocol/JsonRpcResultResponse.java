package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * A successful JSON-RPC response.
 *
 * @param id the request identifier
 * @param result the result value
 * @see <a href="https://www.jsonrpc.org/specification#response_object">JSON-RPC 2.0 response
 *     object</a>
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record JsonRpcResultResponse(Object id, Object result) implements JsonRpcResponse {
  /**
   * Validates the response fields.
   *
   * @param id the request identifier
   * @param result the result value
   * @throws NullPointerException if {@code id} or {@code result} is {@code null}
   */
  public JsonRpcResultResponse {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
    result = McpProtocol.copy(Objects.requireNonNull(result, "result"));
  }

  /**
   * Gets the constant JSON-RPC version.
   *
   * @return {@code 2.0}
   */
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /** Returns a copy of the request identifier. */
  @Override
  public Object id() {
    return McpProtocol.copy(id);
  }

  /**
   * Returns a copy of the result.
   *
   * @return the result
   */
  @Override
  public Object result() {
    return McpProtocol.copy(result);
  }

  /**
   * Creates a builder for {@link JsonRpcResultResponse}.
   *
   * @return a new builder
   */
  public static Builder jsonRpcResultResponse() {
    return new Builder();
  }

  /** Builds {@link JsonRpcResultResponse} values. */
  public static final class Builder {
    private Object id;
    private Object result;

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
     * Sets {@code result}.
     *
     * @param result the value
     * @return this builder
     */
    public Builder result(Object result) {
      this.result = result;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public JsonRpcResultResponse build() {
      return new JsonRpcResultResponse(id, result);
    }
  }
}
