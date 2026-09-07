package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
public record JsonRpcResultResponse(JsonNode id, JsonNode result) implements JsonRpcResponse {
  /**
   * Validates the response fields.
   *
   * @param id the request identifier
   * @param result the result value
   * @throws NullPointerException if {@code id} or {@code result} is {@code null}
   */
  public JsonRpcResultResponse {
    id = Objects.requireNonNull(id, "id").deepCopy();
    result = Objects.requireNonNull(result, "result").deepCopy();
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

  /** Returns a copy of the request identifier. */
  @Override
  public JsonNode id() {
    return id.deepCopy();
  }

  /**
   * Returns a copy of the result.
   *
   * @return the result
   */
  @Override
  public JsonNode result() {
    return result.deepCopy();
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
    private JsonNode id;
    private JsonNode result;

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
     * Sets {@code result}.
     *
     * @param result the value
     * @return this builder
     */
    public Builder result(JsonNode result) {
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
