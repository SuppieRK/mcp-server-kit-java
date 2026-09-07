package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A generic JSON-RPC notification.
 *
 * @param method the protocol method name
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpNotification(String method, Optional<Map<String, ?>> params) {
  /** Validates and copies the protocol fields. */
  public McpNotification {
    Objects.requireNonNull(method, "method");
    params = McpProtocol.copy(params);
  }

  /**
   * Returns a copy of the optional notification parameters.
   *
   * @return the copied parameters
   */
  public Optional<Map<String, ?>> params() {
    return McpProtocol.copy(params);
  }

  /**
   * Creates a builder for {@link McpNotification}.
   *
   * @return a new builder
   */
  public static Builder mcpNotification() {
    return new Builder();
  }

  /** Builds {@link McpNotification} values. */
  public static final class Builder {
    private String method;
    private Optional<Map<String, ?>> params = Optional.empty();

    private Builder() {}

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
     * @param params the optional value
     * @return this builder
     */
    public Builder params(Optional<Map<String, ?>> params) {
      this.params = params;
      return this;
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder params(Map<String, ?> params) {
      return params(Optional.ofNullable(params));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpNotification build() {
      return new McpNotification(method, params);
    }
  }
}
