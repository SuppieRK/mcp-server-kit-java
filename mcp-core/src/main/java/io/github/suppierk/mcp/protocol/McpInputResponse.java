package io.github.suppierk.mcp.protocol;

/**
 * A client result that supplies requested application input.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpInputResponse
    permits McpCreateMessageResult, McpListRootsResult, McpElicitResult {}
