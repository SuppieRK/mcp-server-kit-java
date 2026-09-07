package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.Optional;

/**
 * Contains shared fields for text and binary resource contents.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/server/resources">MCP
 *     resources</a>
 */
public sealed interface McpResourceContents
    permits McpTextResourceContents, McpBlobResourceContents {
  /**
   * Gets the optional protocol metadata.
   *
   * @return the metadata
   */
  Optional<McpMetaObject> meta();

  /**
   * Gets the optional MIME type.
   *
   * @return the MIME type
   */
  Optional<String> mimeType();

  /**
   * Gets the resource URI.
   *
   * @return the resource URI
   */
  URI uri();
}
