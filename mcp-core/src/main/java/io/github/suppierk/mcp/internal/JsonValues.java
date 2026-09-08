package io.github.suppierk.mcp.internal;

import io.github.suppierk.mcp.protocol.McpJsonNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Internal JSON-value snapshots shared by protocol records and registrations. */
public final class JsonValues {
  private JsonValues() {}

  /**
   * Copies a JSON object into deeply immutable JDK values.
   *
   * @param value the JSON object
   * @return an immutable copy
   */
  public static Map<String, Object> copyObject(Map<String, ?> value) {
    return copyMembers(Objects.requireNonNull(value, "value"));
  }

  /**
   * Copies containers while retaining only immutable JSON scalars.
   *
   * @param value the JSON value
   * @return the immutable value
   * @throws IllegalArgumentException if a value is not a supported JSON value or a key is not text
   */
  public static Object copyJson(Object value) {
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
      return copyMembers(object);
    }
    if (value instanceof List<?> array) {
      List<Object> copy = new ArrayList<>(array.size());
      array.forEach(entry -> copy.add(entry == McpJsonNull.INSTANCE ? null : copyJson(entry)));
      return Collections.unmodifiableList(copy);
    }
    throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass().getName());
  }

  /** Validates object keys and detaches every member from caller-owned containers. */
  private static Map<String, Object> copyMembers(Map<?, ?> object) {
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
}
