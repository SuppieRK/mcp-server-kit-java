package io.github.suppierk.mcp.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Validates library-owned JSON Schema boundaries without exposing a validator SPI. */
final class JsonSchemaValidator {
  private static final int MAXIMUM_BYTES = 1_048_576;
  private static final int MAXIMUM_DEPTH = 64;
  private static final int MAXIMUM_NODES = 10_000;

  private final ObjectMapper mapper;
  private final SchemaRegistry registry;

  /** Creates the internal validator with fixed safe reference handling. */
  JsonSchemaValidator(ObjectMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    registry =
        SchemaRegistry.withDefaultDialect(
            SpecificationVersion.DRAFT_2020_12,
            builder ->
                builder.schemaLoader(
                    loader -> loader.fetchRemoteResources(false).block(iri -> true)));
  }

  /** Compiles one safe schema or throws a registration error. */
  Compiled compile(JsonNode schema) {
    Objects.requireNonNull(schema, "schema");
    String unsafe = inspect(schema, true);
    if (unsafe != null) {
      throw new IllegalArgumentException(unsafe);
    }
    try {
      Schema compiled = registry.getSchema(schema.deepCopy());
      compiled.initializeValidators();
      return value -> {
        String invalid = inspect(value, false);
        if (invalid != null) {
          return List.of(invalid);
        }
        return compiled.validate(value).stream()
            .sorted(Comparator.comparing(error -> error.getInstanceLocation().toString()))
            .map(JsonSchemaValidator::message)
            .toList();
      };
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("The JSON Schema cannot be compiled", exception);
    }
  }

  /** Applies fixed size and reference rules. */
  private String inspect(JsonNode root, boolean schema) {
    if (schema && !root.isObject() && !root.isBoolean()) {
      return "A JSON Schema must be an object or boolean";
    }
    try {
      if (mapper.writeValueAsBytes(root).length > MAXIMUM_BYTES) {
        return "The JSON document is too large";
      }
    } catch (JsonProcessingException exception) {
      return "The JSON document cannot be measured";
    }
    ArrayDeque<NodeAtDepth> pending = new ArrayDeque<>();
    pending.push(new NodeAtDepth(root, 1));
    int nodes = 0;
    while (!pending.isEmpty()) {
      NodeAtDepth current = pending.pop();
      if (++nodes > MAXIMUM_NODES) {
        return "The JSON document has too many nodes";
      }
      if (current.depth() > MAXIMUM_DEPTH) {
        return "The JSON document is nested too deeply";
      }
      if (schema && current.node().isObject()) {
        JsonNode reference = current.node().get("$ref");
        JsonNode dynamicReference = current.node().get("$dynamicRef");
        if (isExternalReference(reference) || isExternalReference(dynamicReference)) {
          return "External JSON Schema references are not allowed";
        }
      }
      current
          .node()
          .properties()
          .forEach(entry -> pending.push(new NodeAtDepth(entry.getValue(), current.depth() + 1)));
      if (current.node().isArray()) {
        current.node().forEach(child -> pending.push(new NodeAtDepth(child, current.depth() + 1)));
      }
    }
    return null;
  }

  /** Tests whether a textual reference identifies another document. */
  private static boolean isExternalReference(JsonNode reference) {
    if (reference == null || !reference.isTextual()) {
      return false;
    }
    try {
      URI uri = URI.create(reference.textValue());
      return uri.isAbsolute()
          || uri.getRawAuthority() != null
          || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
          || uri.getRawQuery() != null;
    } catch (IllegalArgumentException exception) {
      return true;
    }
  }

  /** Creates one stable validation message. */
  private static String message(Error error) {
    return error.getInstanceLocation() + ": " + error.getMessage();
  }

  /** Validates one JSON value against a compiled schema. */
  @FunctionalInterface
  interface Compiled {
    /** Returns validation messages, or an empty list. */
    List<String> validate(JsonNode value);
  }

  /** Stores one value and its current depth. */
  private record NodeAtDepth(JsonNode node, int depth) {}
}
