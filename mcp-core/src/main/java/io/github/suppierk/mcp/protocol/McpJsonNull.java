package io.github.suppierk.mcp.protocol;

/**
 * Represents an explicit JSON null in a required or optional arbitrary-JSON member.
 *
 * <p>Use {@code Optional.empty()} for an absent optional member and {@code
 * Optional.of(McpJsonNull.INSTANCE)} for a present JSON null. JSON objects and arrays may contain
 * ordinary Java {@code null} entries.
 */
public enum McpJsonNull {
  /** The explicit JSON null value. */
  INSTANCE
}
