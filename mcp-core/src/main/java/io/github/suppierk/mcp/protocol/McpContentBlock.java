package io.github.suppierk.mcp.protocol;

/**
 * A content block returned by an MCP operation.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpContentBlock
    permits McpTextContent,
        McpImageContent,
        McpAudioContent,
        McpResourceLink,
        McpEmbeddedResource {}
