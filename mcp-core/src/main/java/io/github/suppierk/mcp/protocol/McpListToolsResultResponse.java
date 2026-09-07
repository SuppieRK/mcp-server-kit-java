package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * A successful response from the server for a {@link McpListToolsRequest} ({@code tools/list})
 * request.
 *
 * @param id the JSON-RPC request identifier
 * @param result the successful result
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListToolsResultResponse(JsonNode id, McpListToolsResult result)
    implements JsonRpcResponse {
  /** Validates and copies the protocol fields. */
  public McpListToolsResultResponse {
    id = Objects.requireNonNull(id, "id").deepCopy();
    Objects.requireNonNull(result, "result");
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
   * Creates a builder for {@link McpListToolsResultResponse}.
   *
   * @return a new builder
   */
  public static Builder mcpListToolsResultResponse() {
    return new Builder();
  }

  /** Builds {@link McpListToolsResultResponse} values. */
  public static final class Builder {
    private JsonNode id;
    private McpListToolsResult result;

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
     * Sets {@code result}.
     *
     * @param result the value
     * @return this builder
     */
    public Builder result(McpListToolsResult result) {
      this.result = result;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpListToolsResultResponse build() {
      return new McpListToolsResultResponse(id, result);
    }
  }
}
