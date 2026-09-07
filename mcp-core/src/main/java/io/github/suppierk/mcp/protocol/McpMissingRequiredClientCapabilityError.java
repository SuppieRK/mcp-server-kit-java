package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Reports a missing client capability.
 *
 * @param error the error details
 * @param id the optional request identifier
 * @see <a
 *     href="https://modelcontextprotocol.io/specification/2026-07-28/basic/index#capabilities">MCP
 *     capabilities</a>
 */
public record McpMissingRequiredClientCapabilityError(Error error, Optional<Object> id)
    implements JsonRpcMessage {
  /** Validates the response. */
  public McpMissingRequiredClientCapabilityError {
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
   * Contains capability error details.
   *
   * @param message the error message
   * @param data the required capability data
   */
  public record Error(String message, Data data) {
    /** Validates the error. */
    public Error {
      Objects.requireNonNull(message, "message");
      Objects.requireNonNull(data, "data");
    }

    /**
     * Gets the error code.
     *
     * @return {@code -32021}
     */
    public long code() {
      return -32021;
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
      private Data data;

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
       * @param data the value
       * @return this builder
       */
      public Builder data(Data data) {
        this.data = data;
        return this;
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
   * Contains the required client capabilities.
   *
   * @param requiredCapabilities the required capabilities
   */
  public record Data(McpClientCapabilities requiredCapabilities) {
    /** Validates the capability data. */
    public Data {
      Objects.requireNonNull(requiredCapabilities, "requiredCapabilities");
    }

    /**
     * Creates a builder for {@link Data}.
     *
     * @return a new builder
     */
    public static Builder data() {
      return new Builder();
    }

    /** Builds {@link Data} values. */
    public static final class Builder {
      private McpClientCapabilities requiredCapabilities;

      private Builder() {}

      /**
       * Sets {@code requiredCapabilities}.
       *
       * @param requiredCapabilities the value
       * @return this builder
       */
      public Builder requiredCapabilities(McpClientCapabilities requiredCapabilities) {
        this.requiredCapabilities = requiredCapabilities;
        return this;
      }

      /**
       * Builds the value.
       *
       * @return the built value
       */
      public Data build() {
        return new Data(requiredCapabilities);
      }
    }
  }

  /**
   * Creates a builder for {@link McpMissingRequiredClientCapabilityError}.
   *
   * @return a new builder
   */
  public static Builder mcpMissingRequiredClientCapabilityError() {
    return new Builder();
  }

  /** Builds {@link McpMissingRequiredClientCapabilityError} values. */
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
    public McpMissingRequiredClientCapabilityError build() {
      return new McpMissingRequiredClientCapabilityError(error, id);
    }
  }
}
