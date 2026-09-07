package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * A request from the server to sample an LLM via the client. The client has full discretion over
 * which model to select. The client should also inform the user before beginning sampling, to allow
 * them to inspect the request (human in the loop) and decide whether to approve it.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCreateMessageRequest(McpCreateMessageRequestParams params)
    implements McpInputRequest {

  private static final String METHOD = "sampling/createMessage";

  /** Validates and copies the protocol fields. */
  public McpCreateMessageRequest {
    Objects.requireNonNull(params, "params");
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
   * Creates a builder for {@link McpCreateMessageRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpCreateMessageRequest() {
    return new Builder();
  }

  /** Builds {@link McpCreateMessageRequest} values. */
  public static final class Builder {
    private McpCreateMessageRequestParams params;

    private Builder() {}

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpCreateMessageRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCreateMessageRequest build() {
      return new McpCreateMessageRequest(params);
    }
  }
}
