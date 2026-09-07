package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import java.util.Optional;

/**
 * A generic JSON-RPC request.
 *
 * @param method the protocol method name
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpRequest(String method, Optional<ObjectNode> params) {
  /** Validates and copies the protocol fields. */
  public McpRequest {
    Objects.requireNonNull(method, "method");
    params = McpProtocol.copy(params);
  }

  /**
   * Returns a copy of the optional request parameters.
   *
   * @return the copied parameters
   */
  public Optional<ObjectNode> params() {
    return McpProtocol.copy(params);
  }

  /**
   * Creates a builder for {@link McpRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpRequest() {
    return new Builder();
  }

  /** Builds {@link McpRequest} values. */
  public static final class Builder {
    private String method;
    private Optional<ObjectNode> params = Optional.empty();

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
    public Builder params(Optional<ObjectNode> params) {
      this.params = params;
      return this;
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder params(ObjectNode params) {
      return params(Optional.ofNullable(params));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpRequest build() {
      return new McpRequest(method, params);
    }
  }
}
