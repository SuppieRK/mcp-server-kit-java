package io.github.suppierk.mcp.protocol;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
  @SuppressWarnings("unchecked")
  static Map<String, ?> copyObject(Map<String, ?> value) {
    return (Map<String, ?>) copyJson(Objects.requireNonNull(value, "value"));
  }

  /** Copies containers while retaining only immutable JSON scalars. */
  static Object copyJson(Object value) {
    if (value == null
        || value instanceof String
        || value instanceof Boolean
        || value instanceof Byte
        || value instanceof Short
        || value instanceof Integer
        || value instanceof Long
        || value instanceof BigInteger
        || value instanceof BigDecimal
        || value == McpJsonNull.INSTANCE) {
      return value;
    }
    if (value instanceof Double number && Double.isFinite(number)
        || value instanceof Float floating && Float.isFinite(floating)) {
      return value;
    }
    if (value instanceof Map<?, ?> object) {
      Map<String, Object> copy = new LinkedHashMap<>();
      object.forEach(
          (key, entry) -> {
            if (!(key instanceof String name)) {
              throw new IllegalArgumentException("A JSON object key must be text");
            }
            copy.put(name, entry == McpJsonNull.INSTANCE ? null : copyJson(entry));
          });
      return Collections.unmodifiableMap(copy);
    }
    if (value instanceof List<?> array) {
      List<Object> copy = new ArrayList<>(array.size());
      array.forEach(entry -> copy.add(entry == McpJsonNull.INSTANCE ? null : copyJson(entry)));
      return Collections.unmodifiableList(copy);
    }
    throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass().getName());
  }

  /** Copies one required JSON value. */
  @SuppressWarnings("unchecked")
  static <T> T copy(T value) {
    return (T) copyJson(Objects.requireNonNull(value, "value"));
  }

  /** Copies one optional JSON value. */
  static <T> Optional<T> copy(Optional<T> value) {
    return Objects.requireNonNull(value, "value").map(McpProtocol::copy);
  }

  /** Copies a list and each JSON value in it. */
  @SuppressWarnings("unchecked")
  static <T> List<T> copy(List<T> value) {
    return (List<T>) copyJson(Objects.requireNonNull(value, "value"));
  }
}
