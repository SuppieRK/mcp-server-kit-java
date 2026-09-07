package io.github.suppierk.mcp.protocol;

import java.util.Objects;

/**
 * A request from the server to elicit additional information from the user via the client.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpElicitRequest(McpElicitRequestParams params) implements McpInputRequest {

  private static final String METHOD = "elicitation/create";

  /** Validates and copies the protocol fields. */
  public McpElicitRequest {
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
   * Creates a builder for {@link McpElicitRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpElicitRequest() {
    return new Builder();
  }

  /** Builds {@link McpElicitRequest} values. */
  public static final class Builder {
    private McpElicitRequestParams params;

    private Builder() {}

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpElicitRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpElicitRequest build() {
      return new McpElicitRequest(params);
    }
  }
}
