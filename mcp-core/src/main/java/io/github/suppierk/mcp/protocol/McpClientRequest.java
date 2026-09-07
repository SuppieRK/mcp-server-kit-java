package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A typed request sent by an MCP client.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpClientRequest extends JsonRpcMessage
    permits McpDiscoverRequest,
        McpListResourcesRequest,
        McpListResourceTemplatesRequest,
        McpReadResourceRequest,
        McpSubscriptionsListenRequest,
        McpListPromptsRequest,
        McpGetPromptRequest,
        McpListToolsRequest,
        McpCallToolRequest,
        McpCompleteRequest {
  /**
   * Gets the request identifier.
   *
   * @return the request identifier
   */
  JsonNode id();

  /**
   * Gets the constant method name.
   *
   * @return the method name
   */
  String method();

  /**
   * Gets the typed request parameters.
   *
   * @return the request parameters
   */
  McpRequestParameters params();
}
