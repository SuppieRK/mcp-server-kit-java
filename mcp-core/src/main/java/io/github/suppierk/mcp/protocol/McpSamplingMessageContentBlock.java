package io.github.suppierk.mcp.protocol;

/**
 * A content block permitted in a sampling message.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpSamplingMessageContentBlock
    permits McpTextContent,
        McpImageContent,
        McpAudioContent,
        McpToolUseContent,
        McpToolResultContent {}
