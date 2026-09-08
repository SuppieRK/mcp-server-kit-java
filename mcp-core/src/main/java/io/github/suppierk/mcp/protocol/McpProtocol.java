package io.github.suppierk.mcp.protocol;

import io.github.suppierk.mcp.internal.JsonValues;
import java.util.List;
import java.util.Map;
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
  private static final String VALUE_PARAMETER = "value";

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

  /** Copies a JSON object into deeply immutable JDK values. */
  static Map<String, Object> copyObject(Map<String, ?> value) {
    return JsonValues.copyObject(value);
  }

  /** Copies containers while retaining only immutable JSON scalars. */
  static Object copyJson(Object value) {
    return JsonValues.copyJson(value);
  }

  /** Copies one required JSON value. */
  @SuppressWarnings("unchecked")
  static <T> T copy(T value) {
    return (T) copyJson(Objects.requireNonNull(value, VALUE_PARAMETER));
  }

  /** Copies one optional JSON value. */
  static <T> Optional<T> copy(Optional<T> value) {
    return Objects.requireNonNull(value, VALUE_PARAMETER).map(McpProtocol::copy);
  }

  /** Copies a list and each JSON value in it. */
  @SuppressWarnings("unchecked")
  static <T> List<T> copy(List<T> value) {
    return (List<T>) copyJson(Objects.requireNonNull(value, VALUE_PARAMETER));
  }
}
