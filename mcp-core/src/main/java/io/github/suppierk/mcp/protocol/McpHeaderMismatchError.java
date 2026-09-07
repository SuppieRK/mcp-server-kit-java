package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Reports that HTTP routing headers do not match the JSON-RPC body.
 *
 * @param error the error details
 * @param id the optional request identifier
 * @see <a
 *     href="https://modelcontextprotocol.io/specification/2026-07-28/basic/transports/streamable-http">MCP
 *     Streamable HTTP transport</a>
 */
public record McpHeaderMismatchError(Error error, Optional<Object> id) implements JsonRpcMessage {
  /** Validates the response. */
  public McpHeaderMismatchError {
    Objects.requireNonNull(error, "error");
    id = Objects.requireNonNull(id, "id").map(McpProtocol::copy);
  }

  /**
   * Returns a copy of the optional request identifier.
   *
   * @return the copied identifier
   */
  public Optional<Object> id() {
    return McpProtocol.copy(id);
  }

  /**
   * Gets the JSON-RPC version.
   *
   * @return {@code 2.0}
   */
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /**
   * Contains header-mismatch details.
   *
   * @param message the error message
   * @param data the optional error data
   */
  public record Error(String message, Optional<Object> data) {
    /** Validates the error. */
    public Error {
      Objects.requireNonNull(message, "message");
      data = Objects.requireNonNull(data, "data").map(McpProtocol::copy);
    }

    /**
     * Returns a copy of the optional error data.
     *
     * @return the copied error data
     */
    public Optional<Object> data() {
      return McpProtocol.copy(data);
    }

    /**
     * Gets the error code.
     *
     * @return {@code -32020}
     */
    public long code() {
      return -32020;
    }

    /**
     * Creates a builder for {@link Error}.
     *
     * @return a new builder
     */
    public static Builder error() {
      return new Builder();
    }

    /** Builds {@link Error} values. */
    public static final class Builder {
      private String message;
      private Optional<Object> data = Optional.empty();

      private Builder() {}

      /**
       * Sets {@code message}.
       *
       * @param message the value
       * @return this builder
       */
      public Builder message(String message) {
        this.message = message;
        return this;
      }

      /**
       * Sets {@code data}.
       *
       * @param data the optional value
       * @return this builder
       */
      public Builder data(Optional<Object> data) {
        this.data = data;
        return this;
      }

      /**
       * Sets {@code data}.
       *
       * @param data the value, or {@code null} to clear it
       * @return this builder
       */
      public Builder data(Object data) {
        return data(Optional.ofNullable(data));
      }

      /**
       * Builds the value.
       *
       * @return the built value
       */
      public Error build() {
        return new Error(message, data);
      }
    }
  }

  /**
   * Creates a builder for {@link McpHeaderMismatchError}.
   *
   * @return a new builder
   */
  public static Builder mcpHeaderMismatchError() {
    return new Builder();
  }

  /** Builds {@link McpHeaderMismatchError} values. */
  public static final class Builder {
    private Error error;
    private Optional<Object> id = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code error}.
     *
     * @param error the value
     * @return this builder
     */
    public Builder error(Error error) {
      this.error = error;
      return this;
    }

    /**
     * Sets {@code id}.
     *
     * @param id the optional value
     * @return this builder
     */
    public Builder id(Optional<Object> id) {
      this.id = id;
      return this;
    }

    /**
     * Sets {@code id}.
     *
     * @param id the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder id(Object id) {
      return id(Optional.ofNullable(id));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpHeaderMismatchError build() {
      return new McpHeaderMismatchError(error, id);
    }
  }
}
