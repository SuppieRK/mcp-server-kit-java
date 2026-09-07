package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;

/**
 * An unsuccessful JSON-RPC response.
 *
 * @param id the request identifier, or JSON null when it is not known
 * @param error the error details
 * @see <a href="https://www.jsonrpc.org/specification#response_object">JSON-RPC 2.0 response
 *     object</a>
 * @see <a href="https://www.jsonrpc.org/specification#error_object">JSON-RPC 2.0 error object</a>
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record JsonRpcErrorResponse(JsonNode id, McpError error) implements JsonRpcResponse {
  /** Validates and copies the response fields. */
  public JsonRpcErrorResponse {
    id = Objects.requireNonNull(id, "id").deepCopy();
    Objects.requireNonNull(error, "error");
  }

  /**
   * Creates an error response from its error fields.
   *
   * @param id the request identifier, or JSON null when it is not known
   * @param code the error code
   * @param message the error message
   * @param data the optional error data
   */
  public JsonRpcErrorResponse(JsonNode id, int code, String message, Optional<JsonNode> data) {
    this(id, new McpError((long) code, data, message));
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
   * Gets the error code.
   *
   * @return the error code
   */
  public int code() {
    return error.code().intValue();
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public String message() {
    return error.message();
  }

  /**
   * Gets a copy of the optional error data.
   *
   * @return the optional error data
   */
  public Optional<JsonNode> data() {
    return error.data();
  }

  /** Returns a copy of the request identifier. */
  @Override
  public JsonNode id() {
    return id.deepCopy();
  }

  /**
   * Creates a builder for {@link JsonRpcErrorResponse}.
   *
   * @return a new builder
   */
  public static Builder jsonRpcErrorResponse() {
    return new Builder();
  }

  /** Builds {@link JsonRpcErrorResponse} values. */
  public static final class Builder {
    private JsonNode id;
    private McpError error;

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
     * Sets {@code error}.
     *
     * @param error the value
     * @return this builder
     */
    public Builder error(McpError error) {
      this.error = error;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public JsonRpcErrorResponse build() {
      return new JsonRpcErrorResponse(id, error);
    }
  }
}
