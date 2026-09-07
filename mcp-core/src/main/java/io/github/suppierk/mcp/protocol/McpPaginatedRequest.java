package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * A request that supports cursor pagination.
 *
 * @param id the JSON-RPC request identifier
 * @param method the protocol method name
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPaginatedRequest(JsonNode id, String method, McpPaginatedRequestParams params)
    implements JsonRpcMessage {
  /** Validates and copies the protocol fields. */
  public McpPaginatedRequest {
    id = Objects.requireNonNull(id, "id").deepCopy();
    Objects.requireNonNull(method, "method");
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
   * Creates a builder for {@link McpPaginatedRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpPaginatedRequest() {
    return new Builder();
  }

  /** Builds {@link McpPaginatedRequest} values. */
  public static final class Builder {
    private JsonNode id;
    private String method;
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
     * Sets {@code method}.
     *
     * @param method the value
     * @return this builder
     */
    public Builder method(String method) {
      this.method = method;
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
    public McpPaginatedRequest build() {
      return new McpPaginatedRequest(id, method, params);
    }
  }
}
