package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;

/**
 * A JSON-RPC error indicating that the request is not a valid request object. This error is
 * returned when the message structure does not conform to the JSON-RPC 2.0 specification
 * requirements for a request (e.g., missing required fields like {@code jsonrpc} or {@code method},
 * or using invalid types for these fields).
 *
 * @param data Additional information about the error. The value of this member is defined by the
 *     sender (e.g. detailed error information, nested errors etc.).
 * @param message A short description of the error. The message SHOULD be limited to a concise
 *     single sentence.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpInvalidRequestError(Optional<JsonNode> data, String message) {
  /** Validates and copies the protocol fields. */
  public McpInvalidRequestError {
    data = McpProtocol.copy(data);
    Objects.requireNonNull(message, "message");
  }

  /**
   * Returns a copy of the optional error data.
   *
   * @return the copied error data
   */
  public Optional<JsonNode> data() {
    return McpProtocol.copy(data);
  }

  /**
   * Gets the constant {@code code} value.
   *
   * @return the constant value
   */
  @JsonProperty("code")
  public Long code() {
    return -32600L;
  }

  /**
   * Creates a builder for {@link McpInvalidRequestError}.
   *
   * @return a new builder
   */
  public static Builder mcpInvalidRequestError() {
    return new Builder();
  }

  /** Builds {@link McpInvalidRequestError} values. */
  public static final class Builder {
    private Optional<JsonNode> data = Optional.empty();
    private String message;

    private Builder() {}

    /**
     * Sets {@code data}.
     *
     * @param data the optional value
     * @return this builder
     */
    public Builder data(Optional<JsonNode> data) {
      this.data = data;
      return this;
    }

    /**
     * Sets {@code data}.
     *
     * @param data the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder data(JsonNode data) {
      return data(Optional.ofNullable(data));
    }

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
     * Builds the value.
     *
     * @return the built value
     */
    public McpInvalidRequestError build() {
      return new McpInvalidRequestError(data, message);
    }
  }
}
