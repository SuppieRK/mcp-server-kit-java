package io.github.suppierk.mcp.protocol;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

/**
 * A JSON-RPC request.
 *
 * @param id the string or integer request identifier
 * @param method the method name
 * @param params the request parameters
 * @see <a href="https://www.jsonrpc.org/specification#request_object">JSON-RPC 2.0 request
 *     object</a>
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record JsonRpcRequest(Object id, String method, Map<String, ?> params)
    implements JsonRpcMessage {
  /**
   * Validates the request fields.
   *
   * @param id the request identifier
   * @param method the method name
   * @param params the method parameters
   * @throws NullPointerException if a parameter is {@code null}
   */
  public JsonRpcRequest {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
    Objects.requireNonNull(method, "method");
    params = McpProtocol.copy(Objects.requireNonNull(params, "params"));
    if (!(id instanceof String
            || id instanceof Byte
            || id instanceof Short
            || id instanceof Integer
            || id instanceof Long
            || id instanceof BigInteger)
        || method.isBlank()) {
      throw new IllegalArgumentException("A request needs a valid ID and method");
    }
  }

  /**
   * Gets the constant JSON-RPC version.
   *
   * @return {@code 2.0}
   */
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /**
   * Returns a copy of the request identifier.
   *
   * @return the request identifier
   */
  @Override
  public Object id() {
    return McpProtocol.copy(id);
  }

  /**
   * Returns a copy of the request parameters.
   *
   * @return the request parameters
   */
  @Override
  public Map<String, ?> params() {
    return McpProtocol.copy(params);
  }

  /**
   * Creates a builder for {@link JsonRpcRequest}.
   *
   * @return a new builder
   */
  public static Builder jsonRpcRequest() {
    return new Builder();
  }

  /** Builds {@link JsonRpcRequest} values. */
  public static final class Builder {
    private Object id;
    private String method;
    private Map<String, ?> params;

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
     * Sets {@code method}.
     *
     * @param method the value
     * @return this builder
     */
    public Builder method(String method) {
      this.method = method;
      return this;
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(Map<String, ?> params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public JsonRpcRequest build() {
      return new JsonRpcRequest(id, method, params);
    }
  }
}
