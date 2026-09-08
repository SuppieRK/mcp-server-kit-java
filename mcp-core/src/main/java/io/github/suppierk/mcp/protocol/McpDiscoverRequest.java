package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A request for the protocol versions and capabilities that a server supports. A server must
 * implement {@code server/discover}. A client can also negotiate a version through request
 * metadata.
 *
 * @param id the JSON-RPC request identifier
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpDiscoverRequest(Object id, McpRequestParams params) implements McpClientRequest {

  /** The request wire-method name. */
  public static final String METHOD = "server/discover";

  /** Validates and copies the protocol fields. */
  public McpDiscoverRequest {
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
   * Creates a builder for {@link McpDiscoverRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpDiscoverRequest() {
    return new Builder();
  }

  /** Builds {@link McpDiscoverRequest} values. */
  public static final class Builder {
    private Object id;
    private McpRequestParams params;

    private Builder() {}

    /**
     * Sets {@code params} using a {@link McpRequestParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder params(Consumer<McpRequestParams.Builder> configure) {
      var child = McpRequestParams.mcpRequestParams();
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
    public Builder params(McpRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpDiscoverRequest build() {
      return new McpDiscoverRequest(id, params);
    }
  }
}
