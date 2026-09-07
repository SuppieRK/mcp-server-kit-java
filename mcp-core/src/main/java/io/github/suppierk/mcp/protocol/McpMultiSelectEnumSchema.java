package io.github.suppierk.mcp.protocol;

/**
 * An enum property schema that permits multiple selections.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpMultiSelectEnumSchema
    permits McpUntitledMultiSelectEnumSchema, McpTitledMultiSelectEnumSchema {}
