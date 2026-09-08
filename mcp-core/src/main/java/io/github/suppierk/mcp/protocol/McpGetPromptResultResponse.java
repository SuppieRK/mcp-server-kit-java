package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A successful response from the server for a {@link McpGetPromptRequest} ({@code prompts/get})
 * request.
 *
 * @param id the JSON-RPC request identifier
 * @param result the successful result
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpGetPromptResultResponse(Object id, Result result) implements JsonRpcResponse {
  /** Validates and copies the protocol fields. */
  public McpGetPromptResultResponse {
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

  /** A result permitted for {@code prompts/get}. */
  public sealed interface Result permits McpGetPromptResult, McpInputRequiredResult {}

  /**
   * Creates a builder for {@link McpGetPromptResultResponse}.
   *
   * @return a new builder
   */
  public static Builder mcpGetPromptResultResponse() {
    return new Builder();
  }

  /** Builds {@link McpGetPromptResultResponse} values. */
  public static final class Builder {
    private Object id;
    private Result result;

    private Builder() {}

    /**
     * Sets {@code result} using a {@link McpGetPromptResult} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder getPromptResult(Consumer<McpGetPromptResult.Builder> configure) {
      var child = McpGetPromptResult.mcpGetPromptResult();
      configure.accept(child);
      return result(child.build());
    }

    /**
     * Sets {@code result} using a {@link McpInputRequiredResult} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder inputRequiredResult(Consumer<McpInputRequiredResult.Builder> configure) {
      var child = McpInputRequiredResult.mcpInputRequiredResult();
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
    public Builder result(Result result) {
      this.result = result;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpGetPromptResultResponse build() {
      return new McpGetPromptResultResponse(id, result);
    }
  }
}
