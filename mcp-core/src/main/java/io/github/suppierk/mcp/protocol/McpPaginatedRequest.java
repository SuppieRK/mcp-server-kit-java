package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A request that supports cursor pagination.
 *
 * @param id the JSON-RPC request identifier
 * @param method the protocol method name
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPaginatedRequest(Object id, String method, McpPaginatedRequestParams params)
    implements JsonRpcMessage {
  /** Validates and copies the protocol fields. */
  public McpPaginatedRequest {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(params, "params");
  }

  /**
   * Returns a copy of the request identifier.
   *
   * @return the copied identifier
   */
  public Object id() {
    return McpProtocol.copy(id);
  }

  /**
   * Gets the constant {@code jsonrpc} value.
   *
   * @return the constant value
   */
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /**
   * Creates a builder for {@link McpPaginatedRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpPaginatedRequest() {
    return new Builder();
  }

  /** Builds {@link McpPaginatedRequest} values. */
  public static final class Builder {
    private Object id;
    private String method;
    private McpPaginatedRequestParams params;

    private Builder() {}

    /**
     * Sets {@code params} using a {@link McpPaginatedRequestParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder params(Consumer<McpPaginatedRequestParams.Builder> configure) {
      var child = McpPaginatedRequestParams.mcpPaginatedRequestParams();
      configure.accept(child);
      return params(child.build());
    }

    /**
     * Sets {@code id}.
     *
     * @param id the value
     * @return this builder
     */
    public Builder id(Object id) {
      this.id = id;
      return this;
    }

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
    public Builder params(McpPaginatedRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpPaginatedRequest build() {
      return new McpPaginatedRequest(id, method, params);
    }
  }
}
