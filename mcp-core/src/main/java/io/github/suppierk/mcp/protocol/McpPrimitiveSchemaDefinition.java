package io.github.suppierk.mcp.protocol;

/**
 * Restricted schema definitions that only allow primitive types without nested objects or arrays.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public sealed interface McpPrimitiveSchemaDefinition
    permits McpStringSchema,
        McpNumberSchema,
        McpBooleanSchema,
        McpUntitledSingleSelectEnumSchema,
        McpTitledSingleSelectEnumSchema,
        McpUntitledMultiSelectEnumSchema,
        McpTitledMultiSelectEnumSchema,
        McpLegacyTitledEnumSchema {}
