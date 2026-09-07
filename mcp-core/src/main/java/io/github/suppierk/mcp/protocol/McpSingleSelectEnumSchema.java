package io.github.suppierk.mcp.protocol;

/**
 * An enum property schema that permits one selection.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpSingleSelectEnumSchema
    permits McpUntitledSingleSelectEnumSchema, McpTitledSingleSelectEnumSchema {}
