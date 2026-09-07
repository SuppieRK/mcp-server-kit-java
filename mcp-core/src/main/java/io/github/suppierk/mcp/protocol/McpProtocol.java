package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines protocol constants that this library supports.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28">MCP 2026-07-28
 *     specification</a>
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public final class McpProtocol {
  /** The MCP revision that this release implements. */
  public static final String REVISION = "2026-07-28";

  /** The JSON-RPC version that MCP uses. */
  public static final String JSON_RPC_VERSION = "2.0";

  /** The metadata key for the MCP protocol revision. */
  public static final String PROTOCOL_VERSION_KEY = "io.modelcontextprotocol/protocolVersion";

  /** The metadata key for per-request client capabilities. */
  public static final String CLIENT_CAPABILITIES_KEY = "io.modelcontextprotocol/clientCapabilities";

  /** The supported MCP revisions, from newest to oldest. */
  public static final List<String> SUPPORTED_REVISIONS = List.of(REVISION);

  /** Prevents construction. */
  private McpProtocol() {}

  /** Copies one required JSON value. */
  static <T extends JsonNode> T copy(T value) {
    return Objects.requireNonNull(value, "value").deepCopy();
  }

  /** Copies one optional JSON value. */
  static <T extends JsonNode> Optional<T> copy(Optional<T> value) {
    return Objects.requireNonNull(value, "value").map(McpProtocol::copy);
  }

  /** Copies a list and each JSON value in it. */
  static <T extends JsonNode> List<T> copy(List<T> value) {
    return Objects.requireNonNull(value, "value").stream().map(McpProtocol::copy).toList();
  }
}
