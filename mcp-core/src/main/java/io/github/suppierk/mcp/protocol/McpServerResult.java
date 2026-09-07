package io.github.suppierk.mcp.protocol;

/**
 * A typed result returned by an MCP server.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpServerResult
    permits McpResult,
        McpInputRequiredResult,
        McpDiscoverResult,
        McpListResourcesResult,
        McpListResourceTemplatesResult,
        McpReadResourceResult,
        McpSubscriptionsListenResult,
        McpListPromptsResult,
        McpGetPromptResult,
        McpListToolsResult,
        McpCallToolResult,
        McpCompleteResult {}
