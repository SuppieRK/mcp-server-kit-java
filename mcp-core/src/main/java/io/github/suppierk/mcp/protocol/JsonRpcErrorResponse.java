package io.github.suppierk.mcp.protocol;

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
public record JsonRpcErrorResponse(Object id, McpError error) implements JsonRpcResponse {
  /** Validates and copies the response fields. */
  public JsonRpcErrorResponse {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
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
  public JsonRpcErrorResponse(Object id, int code, String message, Optional<Object> data) {
    this(id, new McpError((long) code, data, message));
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
  public Optional<Object> data() {
    return error.data();
  }

  /** Returns a copy of the request identifier. */
  @Override
  public Object id() {
    return McpProtocol.copy(id);
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
    private Object id;
    private McpError error;

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
