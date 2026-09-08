package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A request from the client to the server, to ask for completion options.
 *
 * @param id the JSON-RPC request identifier
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCompleteRequest(Object id, McpCompleteRequestParams params)
    implements McpClientRequest {

  /** The request wire-method name. */
  public static final String METHOD = "completion/complete";

  /** Validates and copies the protocol fields. */
  public McpCompleteRequest {
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
   * Creates a builder for {@link McpCompleteRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpCompleteRequest() {
    return new Builder();
  }

  /** Builds {@link McpCompleteRequest} values. */
  public static final class Builder {
    private Object id;
    private McpCompleteRequestParams params;

    private Builder() {}

    /**
     * Sets {@code params} using a {@link McpCompleteRequestParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder params(Consumer<McpCompleteRequestParams.Builder> configure) {
      var child = McpCompleteRequestParams.mcpCompleteRequestParams();
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
    public Builder params(McpCompleteRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCompleteRequest build() {
      return new McpCompleteRequest(id, params);
    }
  }
}
