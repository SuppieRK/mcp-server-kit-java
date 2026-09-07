package io.github.suppierk.mcp.protocol;

/**
 * A client request that asks for application input.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpInputRequest
    permits McpCreateMessageRequest, McpListRootsRequest, McpElicitRequest {}
