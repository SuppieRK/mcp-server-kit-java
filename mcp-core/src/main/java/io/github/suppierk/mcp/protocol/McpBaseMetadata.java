package io.github.suppierk.mcp.protocol;

import java.util.Optional;

/**
 * Provides the common name and title metadata.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public interface McpBaseMetadata {
  /**
   * Gets the programmatic name.
   *
   * @return the name
   */
  String name();

  /**
   * Gets the optional display title.
   *
   * @return the optional title
   */
  Optional<String> title();
}
