package io.github.suppierk.mcp.protocol;

/**
 * The parameters for a request to elicit additional information from the user via the client.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpElicitRequestParams
    permits McpElicitRequestFormParams, McpElicitRequestURLParams {}
