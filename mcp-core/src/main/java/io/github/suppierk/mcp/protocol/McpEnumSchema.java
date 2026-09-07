package io.github.suppierk.mcp.protocol;

/**
 * An enum property schema for form elicitation.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpEnumSchema
    permits McpUntitledSingleSelectEnumSchema,
        McpTitledSingleSelectEnumSchema,
        McpUntitledMultiSelectEnumSchema,
        McpTitledMultiSelectEnumSchema,
        McpLegacyTitledEnumSchema {}
