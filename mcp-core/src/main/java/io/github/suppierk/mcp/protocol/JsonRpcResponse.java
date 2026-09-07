package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A successful or unsuccessful JSON-RPC response.
 *
 * @see <a href="https://www.jsonrpc.org/specification#response_object">JSON-RPC 2.0 response
 *     object</a>
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public interface JsonRpcResponse extends JsonRpcMessage {
  /**
   * Gets the request identifier.
   *
   * @return the request identifier, or JSON null when it is not known
   */
  JsonNode id();
}
