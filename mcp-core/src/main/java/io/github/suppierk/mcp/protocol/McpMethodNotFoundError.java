package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * A JSON-RPC error indicating that the requested method does not exist or is not available. In MCP,
 * a server returns this error when a client invokes a method the server does not implement — either
 * a genuinely unknown method, or one gated behind a server capability the server did not advertise
 * (e.g., calling {@code prompts/list} when the {@code prompts} capability was not advertised). A
 * request that requires an undeclared client capability uses {@link
 * McpMissingRequiredClientCapabilityError} ({@code -32021}) instead.
 *
 * @param data Additional information about the error. The value of this member is defined by the
 *     sender (e.g. detailed error information, nested errors etc.).
 * @param message A short description of the error. The message SHOULD be limited to a concise
 *     single sentence.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpMethodNotFoundError(Optional<Object> data, String message) {
  /** Validates and copies the protocol fields. */
  public McpMethodNotFoundError {
    data = McpProtocol.copy(data);
    Objects.requireNonNull(message, "message");
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
   * Gets the constant {@code code} value.
   *
   * @return the constant value
   */
  public Long code() {
    return -32601L;
  }

  /**
   * Creates a builder for {@link McpMethodNotFoundError}.
   *
   * @return a new builder
   */
  public static Builder mcpMethodNotFoundError() {
    return new Builder();
  }

  /** Builds {@link McpMethodNotFoundError} values. */
  public static final class Builder {
    private Optional<Object> data = Optional.empty();
    private String message;

    private Builder() {}

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
    public McpMethodNotFoundError build() {
      return new McpMethodNotFoundError(data, message);
    }
  }
}
