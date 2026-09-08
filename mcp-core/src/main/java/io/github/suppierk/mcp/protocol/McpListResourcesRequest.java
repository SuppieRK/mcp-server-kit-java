package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Sent from the client to request a list of resources the server has.
 *
 * @param id the JSON-RPC request identifier
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListResourcesRequest(Object id, McpPaginatedRequestParams params)
    implements McpClientRequest {

  /** The request wire-method name. */
  public static final String METHOD = "resources/list";

  /** Validates and copies the protocol fields. */
  public McpListResourcesRequest {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
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
   * Gets the constant {@code method} value.
   *
   * @return the constant value
   */
  public String method() {
    return METHOD;
  }

  /**
   * Creates a builder for {@link McpListResourcesRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpListResourcesRequest() {
    return new Builder();
  }

  /** Builds {@link McpListResourcesRequest} values. */
  public static final class Builder {
    private Object id;
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
    public McpListResourcesRequest build() {
      return new McpListResourcesRequest(id, params);
    }
  }
}
