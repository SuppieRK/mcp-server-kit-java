package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * Used by the client to get a prompt provided by the server.
 *
 * @param id the JSON-RPC request identifier
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpGetPromptRequest(JsonNode id, McpGetPromptRequestParams params)
    implements McpClientRequest {

  /** The request wire-method name. */
  public static final String METHOD = "prompts/get";

  /** Validates and copies the protocol fields. */
  public McpGetPromptRequest {
    id = Objects.requireNonNull(id, "id").deepCopy();
    Objects.requireNonNull(params, "params");
  }

  /**
   * Returns a copy of the request identifier.
   *
   * @return the copied identifier
   */
  public JsonNode id() {
    return McpProtocol.copy(id);
  }

  /**
   * Gets the constant {@code jsonrpc} value.
   *
   * @return the constant value
   */
  @JsonProperty("jsonrpc")
  public String jsonrpc() {
    return McpProtocol.JSON_RPC_VERSION;
  }

  /**
   * Gets the constant {@code method} value.
   *
   * @return the constant value
   */
  @JsonProperty("method")
  public String method() {
    return METHOD;
  }

  /**
   * Creates a builder for {@link McpGetPromptRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpGetPromptRequest() {
    return new Builder();
  }

  /** Builds {@link McpGetPromptRequest} values. */
  public static final class Builder {
    private JsonNode id;
    private McpGetPromptRequestParams params;

    private Builder() {}

    /**
     * Sets {@code id}.
     *
     * @param id the value
     * @return this builder
     */
    public Builder id(JsonNode id) {
      this.id = id;
      return this;
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value
     * @return this builder
     */
    public Builder params(McpGetPromptRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpGetPromptRequest build() {
      return new McpGetPromptRequest(id, params);
    }
  }
}
