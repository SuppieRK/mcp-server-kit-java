package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * A JSON-RPC error for invalid method parameters. Examples include an unknown tool, invalid tool
 * arguments, an unknown prompt, a missing prompt argument, an invalid cursor, and an invalid log
 * level. A server also uses this error for an undeclared elicitation mode or invalid sampling
 * content.
 *
 * @param data Additional information about the error. The value of this member is defined by the
 *     sender (e.g. detailed error information, nested errors etc.).
 * @param message A short description of the error. The message SHOULD be limited to a concise
 *     single sentence.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpInvalidParamsError(Optional<Object> data, String message) {
  /** Validates and copies the protocol fields. */
  public McpInvalidParamsError {
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
    return -32602L;
  }

  /**
   * Creates a builder for {@link McpInvalidParamsError}.
   *
   * @return a new builder
   */
  public static Builder mcpInvalidParamsError() {
    return new Builder();
  }

  /** Builds {@link McpInvalidParamsError} values. */
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
    public McpInvalidParamsError build() {
      return new McpInvalidParamsError(data, message);
    }
  }
}
