package io.github.suppierk.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.suppierk.mcp.protocol.McpJsonNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Builds JDK fixture values and reads their JSON representation in assertions. */
public final class JsonTestValues {
  private static final JsonMapper JSON = JsonMapper.builder().build();

  private JsonTestValues() {}

  public static Object value(Object supplied) {
    if (supplied instanceof JsonNode node) {
      return node.isNull() ? McpJsonNull.INSTANCE : JSON.convertValue(node, Object.class);
    }
    if (supplied instanceof Map<?, ?> map) {
      Map<String, Object> copy = new LinkedHashMap<>();
      map.forEach((key, entry) -> copy.put((String) key, value(entry)));
      return copy;
    }
    if (supplied instanceof List<?> list) {
      var copy = new ArrayList<>();
      list.forEach(entry -> copy.add(value(entry)));
      return copy;
    }
    return supplied;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, ?> object(Object supplied) {
    return (Map<String, ?>) value(supplied);
  }

  public static Optional<Object> optionalValue(Optional<?> supplied) {
    return supplied.map(JsonTestValues::value);
  }

  public static Optional<Map<String, ?>> optionalObject(Optional<?> supplied) {
    return supplied.map(JsonTestValues::object);
  }

  public static JsonNode json(Object supplied) {
    return supplied == McpJsonNull.INSTANCE ? JSON.nullNode() : JSON.valueToTree(supplied);
  }
}
