package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;

/**
 * A JSON-RPC notification.
 *
 * @param method the method name
 * @param params the notification parameters
 * @see <a href="https://www.jsonrpc.org/specification#notification">JSON-RPC 2.0 notification</a>
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record JsonRpcNotification(String method, ObjectNode params) implements JsonRpcMessage {
  /**
   * Validates the notification fields.
   *
   * @param method the method name
   * @param params the method parameters
   * @throws NullPointerException if a parameter is {@code null}
   */
  public JsonRpcNotification {
    Objects.requireNonNull(method, "method");
    params = Objects.requireNonNull(params, "params").deepCopy();
    if (method.isBlank()) {
      throw new IllegalArgumentException("A notification needs a method");
    }
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
   * Returns a copy of the notification parameters.
   *
   * @return the notification parameters
   */
  @Override
  public ObjectNode params() {
    return params.deepCopy();
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
    private ObjectNode params;

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
    public Builder params(ObjectNode params) {
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
