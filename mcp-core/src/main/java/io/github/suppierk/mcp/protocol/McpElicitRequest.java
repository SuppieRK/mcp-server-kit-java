package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.function.Consumer;

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
     * Sets {@code params} using a {@link McpElicitRequestFormParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder elicitRequestFormParams(Consumer<McpElicitRequestFormParams.Builder> configure) {
      var child = McpElicitRequestFormParams.mcpElicitRequestFormParams();
      configure.accept(child);
      return params(child.build());
    }

    /**
     * Sets {@code params} using a {@link McpElicitRequestURLParams} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder elicitRequestURLParams(Consumer<McpElicitRequestURLParams.Builder> configure) {
      var child = McpElicitRequestURLParams.mcpElicitRequestURLParams();
      configure.accept(child);
      return params(child.build());
    }

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
