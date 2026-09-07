package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * Sent from the client to request a list of resource templates the server has.
 *
 * @param id the JSON-RPC request identifier
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListResourceTemplatesRequest(JsonNode id, McpPaginatedRequestParams params)
    implements McpClientRequest {

  /** The request wire-method name. */
  public static final String METHOD = "resources/templates/list";

  /** Validates and copies the protocol fields. */
  public McpListResourceTemplatesRequest {
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
   * Creates a builder for {@link McpListResourceTemplatesRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpListResourceTemplatesRequest() {
    return new Builder();
  }

  /** Builds {@link McpListResourceTemplatesRequest} values. */
  public static final class Builder {
    private JsonNode id;
    private McpPaginatedRequestParams params;

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
    public Builder params(McpPaginatedRequestParams params) {
      this.params = params;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpListResourceTemplatesRequest build() {
      return new McpListResourceTemplatesRequest(id, params);
    }
  }
}
