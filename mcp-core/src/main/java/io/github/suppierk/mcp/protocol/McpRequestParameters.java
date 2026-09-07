package io.github.suppierk.mcp.protocol;

/**
 * Parameters for a client request to an MCP server.
 *
 * @see <a
 *     href="https://modelcontextprotocol.io/specification/2026-07-28/basic/index#request-metadata">MCP
 *     request metadata</a>
 */
public interface McpRequestParameters {
  /**
   * Gets the request metadata.
   *
   * @return the request metadata
   */
  McpRequestMetaObject meta();
}
