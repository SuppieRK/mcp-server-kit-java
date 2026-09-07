package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;

/**
 * A JSON-RPC notification.
 *
 * @param method the method name
 * @param params the notification parameters
 * @see <a href="https://www.jsonrpc.org/specification#notification">JSON-RPC 2.0 notification</a>
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record JsonRpcNotification(String method, Map<String, ?> params) implements JsonRpcMessage {
  /**
   * Validates the notification fields.
   *
   * @param method the method name
   * @param params the method parameters
   * @throws NullPointerException if a parameter is {@code null}
   */
  public JsonRpcNotification {
    Objects.requireNonNull(method, "method");
    params = McpProtocol.copyObject(params);
    if (method.isBlank()) {
      throw new IllegalArgumentException("A notification needs a method");
    }
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
   * Returns the deeply immutable notification parameters.
   *
   * @return the notification parameters
   */
  @Override
  public Map<String, ?> params() {
    return params;
  }

  /**
   * Creates a builder for {@link JsonRpcNotification}.
   *
   * @return a new builder
   */
  public static Builder jsonRpcNotification() {
    return new Builder();
  }

  /** Builds {@link JsonRpcNotification} values. */
  public static final class Builder {
    private String method;
    private Map<String, ?> params;

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
     * @param params the value
     * @return this builder
     */
    public Builder params(Map<String, ?> params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public JsonRpcNotification build() {
      return new JsonRpcNotification(method, params);
    }
  }
}
