package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A successful response for a {@link McpListResourceTemplatesRequest} ({@code
 * resources/templates/list}) request.
 *
 * @param id the JSON-RPC request identifier
 * @param result the successful result
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListResourceTemplatesResultResponse(
    Object id, McpListResourceTemplatesResult result) implements JsonRpcResponse {
  /** Validates and copies the protocol fields. */
  public McpListResourceTemplatesResultResponse {
    id = McpProtocol.copy(Objects.requireNonNull(id, "id"));
    Objects.requireNonNull(result, "result");
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
   * Creates a builder for {@link McpListResourceTemplatesResultResponse}.
   *
   * @return a new builder
   */
  public static Builder mcpListResourceTemplatesResultResponse() {
    return new Builder();
  }

  /** Builds {@link McpListResourceTemplatesResultResponse} values. */
  public static final class Builder {
    private Object id;
    private McpListResourceTemplatesResult result;

    private Builder() {}

    /**
     * Sets {@code result} using a {@link McpListResourceTemplatesResult} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder result(Consumer<McpListResourceTemplatesResult.Builder> configure) {
      var child = McpListResourceTemplatesResult.mcpListResourceTemplatesResult();
      configure.accept(child);
      return result(child.build());
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
     * Sets {@code result}.
     *
     * @param result the value
     * @return this builder
     */
    public Builder result(McpListResourceTemplatesResult result) {
      this.result = result;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpListResourceTemplatesResultResponse build() {
      return new McpListResourceTemplatesResultResponse(id, result);
    }
  }
}
