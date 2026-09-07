package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public record JsonRpcRequest(JsonNode id, String method, ObjectNode params)
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
    id = Objects.requireNonNull(id, "id").deepCopy();
    Objects.requireNonNull(method, "method");
    params = Objects.requireNonNull(params, "params").deepCopy();
    if ((!id.isTextual() && !id.isIntegralNumber()) || method.isBlank()) {
      throw new IllegalArgumentException("A request needs a valid ID and method");
    }
  }

  /**
   * Gets the constant JSON-RPC version.
   *
   * @return {@code 2.0}
   */
  @JsonProperty("jsonrpc")
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /**
   * Returns a copy of the request identifier.
   *
   * @return the request identifier
   */
  @Override
  public JsonNode id() {
    return id.deepCopy();
  }

  /**
   * Returns a copy of the request parameters.
   *
   * @return the request parameters
   */
  @Override
  public ObjectNode params() {
    return params.deepCopy();
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
    private JsonNode id;
    private String method;
    private ObjectNode params;

    private Builder() {}

    /**
     * Sets {@code id}.
     *
     * @param id the value
     * @return this builder
     */
    public Builder id(JsonNode id) {
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
    public Builder params(ObjectNode params) {
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
