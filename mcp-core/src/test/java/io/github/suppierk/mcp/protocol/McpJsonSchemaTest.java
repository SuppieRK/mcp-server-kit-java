package io.github.suppierk.mcp.protocol;

import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonArraySchema;
import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonBooleanSchema;
import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonIntegerSchema;
import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonNumberSchema;
import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonObjectSchema;
import static io.github.suppierk.mcp.protocol.McpJsonSchema.mcpJsonStringSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class McpJsonSchemaTest {
  @Test
  void detachesObjectSchemasFromRetainedBuildersAndRejectsConstructionMistakes() {
    var retained = new AtomicReference<McpJsonSchema.ObjectBuilder>();
    var calls = new AtomicInteger();
    var property = new LinkedHashMap<String, Object>(Map.of("type", "string"));
    var schema =
        mcpJsonObjectSchema(
            object -> {
              calls.incrementAndGet();
              retained.set(object);
              object
                  .required("id", property)
                  .keyword("$defs", Map.of("label", mcpJsonStringSchema()));
            });
    property.put("type", "number");
    retained.get().optional("later", mcpJsonBooleanSchema()).additionalProperties(true);
    assertEquals(1, calls.get());
    assertEquals(
        Map.of(
            "type",
            "object",
            "additionalProperties",
            false,
            "properties",
            Map.of("id", Map.of("type", "string")),
            "required",
            List.of("id"),
            "$defs",
            Map.of("label", Map.of("type", "string"))),
        schema);
    assertThrows(
        UnsupportedOperationException.class, () -> ((Map<?, ?>) schema.get("properties")).clear());
    assertThrows(
        UnsupportedOperationException.class, () -> ((List<?>) schema.get("required")).clear());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            mcpJsonObjectSchema(
                object ->
                    object
                        .required("id", mcpJsonStringSchema())
                        .optional("id", mcpJsonStringSchema())));
    assertThrows(NullPointerException.class, () -> mcpJsonObjectSchema(null));
    assertThrows(
        NullPointerException.class,
        () -> mcpJsonObjectSchema(object -> object.optional(null, Map.of())));
    assertThrows(
        NullPointerException.class,
        () -> mcpJsonObjectSchema(object -> object.required("id", null)));
    assertThrows(NullPointerException.class, () -> mcpJsonArraySchema(array -> array.items(null)));
    assertThrows(
        IllegalArgumentException.class,
        () -> mcpJsonStringSchema(string -> string.enumValues(List.of())));
    for (String keyword :
        List.of(
            "type",
            "properties",
            "required",
            "additionalProperties",
            "title",
            "description",
            "enum",
            "const",
            "default")) {
      assertThrows(
          IllegalArgumentException.class,
          () -> mcpJsonObjectSchema(object -> object.keyword(keyword, Map.of())));
    }
    var annotatedNull = mcpJsonStringSchema(string -> string.defaultValue(McpJsonNull.INSTANCE));
    assertEquals(true, annotatedNull.containsKey("default"));
    assertEquals(null, annotatedNull.get("default"));
  }

  @Test
  void composesNumericArrayAndBooleanConstraintsWithExplicitAdditionalProperties() {
    assertEquals(Map.of("type", "number"), mcpJsonNumberSchema());
    assertEquals(Map.of("type", "integer"), mcpJsonIntegerSchema());
    assertEquals(Map.of("type", "array"), mcpJsonArraySchema());
    assertEquals(Map.of("type", "boolean"), mcpJsonBooleanSchema());
    assertEquals(
        Map.of("type", "number", "exclusiveMinimum", 0, "exclusiveMaximum", 10, "multipleOf", 0.5),
        mcpJsonNumberSchema(n -> n.exclusiveMinimum(0).exclusiveMaximum(10).multipleOf(0.5)));
    var schema =
        mcpJsonObjectSchema(
            object ->
                object
                    .required(
                        "counts",
                        mcpJsonArraySchema(
                            array ->
                                array
                                    .items(
                                        mcpJsonIntegerSchema(
                                            number -> number.minimum(1).maximum(10)))
                                    .minItems(1)
                                    .maxItems(5)
                                    .uniqueItems(true)))
                    .optional("enabled", mcpJsonBooleanSchema(bool -> bool.defaultValue(true)))
                    .additionalProperties(mcpJsonStringSchema()));
    assertEquals(
        Map.of(
            "type",
            "object",
            "additionalProperties",
            Map.of("type", "string"),
            "required",
            List.of("counts"),
            "properties",
            Map.of(
                "counts",
                    Map.of(
                        "type",
                        "array",
                        "items",
                        Map.of("type", "integer", "minimum", 1, "maximum", 10),
                        "minItems",
                        1,
                        "maxItems",
                        5,
                        "uniqueItems",
                        true),
                "enabled", Map.of("type", "boolean", "default", true))),
        schema);
    assertEquals(
        Map.of("type", "object", "additionalProperties", true),
        mcpJsonObjectSchema(object -> object.additionalProperties(true)));
    assertThrows(
        IllegalArgumentException.class, () -> mcpJsonNumberSchema(n -> n.minimum(Double.NaN)));
    assertThrows(
        IllegalArgumentException.class,
        () -> mcpJsonIntegerSchema(n -> n.maximum(Double.POSITIVE_INFINITY)));
    assertThrows(IllegalArgumentException.class, () -> mcpJsonNumberSchema(n -> n.multipleOf(0)));
    assertThrows(IllegalArgumentException.class, () -> mcpJsonNumberSchema(n -> n.multipleOf(-1)));
    assertThrows(IllegalArgumentException.class, () -> mcpJsonArraySchema(a -> a.minItems(-1)));
    assertThrows(IllegalArgumentException.class, () -> mcpJsonArraySchema(a -> a.maxItems(-1)));
  }

  @Test
  void snapshotsStringConstraintsAnnotationsAndExtensions() {
    var options = new ArrayList<>(List.of("alice", "bob"));
    var schema =
        mcpJsonStringSchema(
            string ->
                string
                    .title("User")
                    .description("User identifier")
                    .enumValues(options)
                    .constValue("alice")
                    .defaultValue("alice")
                    .minLength(1)
                    .maxLength(20)
                    .pattern("^[a-z]+$")
                    .format("hostname")
                    .keyword("x-mcp-header", "User"));
    options.clear();
    assertEquals(
        Map.ofEntries(
            Map.entry("type", "string"),
            Map.entry("title", "User"),
            Map.entry("description", "User identifier"),
            Map.entry("enum", List.of("alice", "bob")),
            Map.entry("const", "alice"),
            Map.entry("default", "alice"),
            Map.entry("minLength", 1),
            Map.entry("maxLength", 20),
            Map.entry("pattern", "^[a-z]+$"),
            Map.entry("format", "hostname"),
            Map.entry("x-mcp-header", "User")),
        schema);
    assertThrows(UnsupportedOperationException.class, schema::clear);
    assertThrows(UnsupportedOperationException.class, () -> ((List<?>) schema.get("enum")).clear());
    assertThrows(IllegalArgumentException.class, () -> mcpJsonStringSchema(s -> s.minLength(-1)));
    assertThrows(IllegalArgumentException.class, () -> mcpJsonStringSchema(s -> s.maxLength(-1)));
    assertThrows(
        IllegalArgumentException.class,
        () -> mcpJsonStringSchema(s -> s.keyword("type", "number")));
    assertThrows(
        IllegalArgumentException.class, () -> mcpJsonStringSchema(s -> s.keyword("minLength", 0)));
  }

  @Test
  void composesRequiredAndOptionalPropertiesWithClosedObjectDefaults() {
    assertEquals(Map.of("type", "object", "additionalProperties", false), mcpJsonObjectSchema());
    var schema =
        mcpJsonObjectSchema(
            object ->
                object
                    .required(
                        "user",
                        mcpJsonObjectSchema(user -> user.required("id", mcpJsonStringSchema())))
                    .optional("nickname", mcpJsonStringSchema()));
    assertEquals(
        Map.of(
            "type",
            "object",
            "additionalProperties",
            false,
            "required",
            List.of("user"),
            "properties",
            Map.of(
                "user",
                    Map.of(
                        "type",
                        "object",
                        "additionalProperties",
                        false,
                        "required",
                        List.of("id"),
                        "properties",
                        Map.of("id", Map.of("type", "string"))),
                "nickname", Map.of("type", "string"))),
        schema);
  }
}
