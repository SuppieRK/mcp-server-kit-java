package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * A successful response from the server for a {@link McpCallToolRequest} ({@code tools/call})
 * request.
 *
 * @param id the JSON-RPC request identifier
 * @param result the successful result
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCallToolResultResponse(Object id, Result result) implements JsonRpcResponse {
  /** Validates and copies the protocol fields. */
  public McpCallToolResultResponse {
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

  /** A result permitted for {@code tools/call}. */
  public sealed interface Result permits McpCallToolResult, McpInputRequiredResult {}

  /**
   * Creates a builder for {@link McpCallToolResultResponse}.
   *
   * @return a new builder
   */
  public static Builder mcpCallToolResultResponse() {
    return new Builder();
  }

  /** Builds {@link McpCallToolResultResponse} values. */
  public static final class Builder {
    private Object id;
    private Result result;

    private Builder() {}

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
    public Builder result(Result result) {
      this.result = result;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCallToolResultResponse build() {
      return new McpCallToolResultResponse(id, result);
    }
  }
}
