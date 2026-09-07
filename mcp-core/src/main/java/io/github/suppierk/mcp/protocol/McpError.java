package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * An MCP error value.
 *
 * @param code The error type that occurred.
 * @param data Additional information about the error. The value of this member is defined by the
 *     sender (e.g. detailed error information, nested errors etc.).
 * @param message A short description of the error. The message SHOULD be limited to a concise
 *     single sentence.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpError(Long code, Optional<Object> data, String message) {
  /** Validates and copies the protocol fields. */
  public McpError {
    Objects.requireNonNull(code, "code");
    data = Objects.requireNonNull(data, "data").map(McpProtocol::copy);
    Objects.requireNonNull(message, "message");
  }

  /**
   * Returns a copy of the optional error data.
   *
   * @return the optional error data
   */
  @Override
  public Optional<Object> data() {
    return data.map(McpProtocol::copy);
  }

  /**
   * Creates a builder for {@link McpError}.
   *
   * @return a new builder
   */
  public static Builder mcpError() {
    return new Builder();
  }

  /** Builds {@link McpError} values. */
  public static final class Builder {
    private Long code;
    private Optional<Object> data = Optional.empty();
    private String message;

    private Builder() {}

    /**
     * Sets {@code code}.
     *
     * @param code the value
     * @return this builder
     */
    public Builder code(Long code) {
      this.code = code;
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
    public McpError build() {
      return new McpError(code, data, message);
    }
  }
}
