package io.github.suppierk.mcp.protocol;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Composes JSON Schema objects as deeply immutable JDK maps. Object schemas are closed by default.
 * These helpers construct schemas; the server kit performs full schema compilation and validation.
 */
public final class McpJsonSchema {
  private McpJsonSchema() {}

  /**
   * Creates a closed empty-object schema.
   *
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonObjectSchema() {
    return mcpJsonObjectSchema(schema -> {});
  }

  /**
   * Configures an object schema immediately and snapshots it after the callback returns.
   *
   * @param configure the configuration callback
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonObjectSchema(Consumer<ObjectBuilder> configure) {
    return configure(new ObjectBuilder(), configure);
  }

  /**
   * Creates an unconstrained string schema.
   *
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonStringSchema() {
    return mcpJsonStringSchema(schema -> {});
  }

  /**
   * Configures a string schema.
   *
   * @param configure the configuration callback
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonStringSchema(Consumer<StringBuilder> configure) {
    return configure(new StringBuilder(), configure);
  }

  /**
   * Creates an unconstrained integer schema.
   *
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonIntegerSchema() {
    return mcpJsonIntegerSchema(schema -> {});
  }

  /**
   * Configures an integer schema.
   *
   * @param configure the configuration callback
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonIntegerSchema(Consumer<NumberBuilder> configure) {
    return configure(new NumberBuilder("integer"), configure);
  }

  /**
   * Creates an unconstrained number schema.
   *
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonNumberSchema() {
    return mcpJsonNumberSchema(schema -> {});
  }

  /**
   * Configures a number schema.
   *
   * @param configure the configuration callback
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonNumberSchema(Consumer<NumberBuilder> configure) {
    return configure(new NumberBuilder("number"), configure);
  }

  /**
   * Creates an unconstrained boolean schema.
   *
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonBooleanSchema() {
    return mcpJsonBooleanSchema(schema -> {});
  }

  /**
   * Configures a boolean schema.
   *
   * @param configure the configuration callback
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonBooleanSchema(Consumer<BooleanBuilder> configure) {
    return configure(new BooleanBuilder(), configure);
  }

  /**
   * Creates an unconstrained array schema.
   *
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonArraySchema() {
    return mcpJsonArraySchema(schema -> {});
  }

  /**
   * Configures an array schema.
   *
   * @param configure the configuration callback
   * @return the immutable schema
   */
  public static Map<String, ?> mcpJsonArraySchema(Consumer<ArrayBuilder> configure) {
    return configure(new ArrayBuilder(), configure);
  }

  /** Invokes the callback once, then detaches its JSON values. */
  private static <B extends Builder<B>> Map<String, ?> configure(B builder, Consumer<B> configure) {
    Objects.requireNonNull(configure, "configure").accept(builder);
    return McpProtocol.copyObject(builder.values);
  }

  /**
   * Shared schema annotations and an escape hatch for unsupported keywords.
   *
   * @param <B> the concrete builder type
   */
  public abstract static sealed class Builder<B extends Builder<B>>
      permits ObjectBuilder, StringBuilder, NumberBuilder, BooleanBuilder, ArrayBuilder {
    private static final Set<String> ANNOTATIONS =
        Set.of("type", "title", "description", "enum", "const", "default");
    final Map<String, Object> values = new LinkedHashMap<>();
    private final Set<String> managed;

    private Builder(String type, String... managed) {
      values.put("type", type);
      this.managed = Set.of(managed);
    }

    /** Stores a non-null JSON value and retains the concrete fluent type. */
    @SuppressWarnings("unchecked")
    final B put(String name, Object value) {
      values.put(name, Objects.requireNonNull(value, name));
      return (B) this;
    }

    /**
     * Sets a human-readable title.
     *
     * @param value the title
     * @return this builder
     */
    public final B title(String value) {
      return put("title", value);
    }

    /**
     * Sets a schema description.
     *
     * @param value the description
     * @return this builder
     */
    public final B description(String value) {
      return put("description", value);
    }

    /**
     * Restricts values to an enumeration.
     *
     * @param values the nonempty list of JSON values
     * @return this builder
     * @throws IllegalArgumentException if the enumeration is empty
     */
    public final B enumValues(List<?> values) {
      if (Objects.requireNonNull(values, "values").isEmpty()) {
        throw new IllegalArgumentException("An enumeration must not be empty");
      }
      return put("enum", values);
    }

    /**
     * Restricts values to one JSON constant.
     *
     * @param value the constant; use {@link McpJsonNull#INSTANCE} for JSON null
     * @return this builder
     */
    public final B constValue(Object value) {
      return put("const", value);
    }

    /**
     * Annotates a default without inserting missing application arguments.
     *
     * @param value the default; use {@link McpJsonNull#INSTANCE} for JSON null
     * @return this builder
     */
    public final B defaultValue(Object value) {
      return put("default", value);
    }

    /**
     * Adds an unsupported keyword, such as {@code $defs} or an application extension.
     *
     * @param name the keyword, which must not be managed by this builder's typed setters
     * @param value the JSON value; use {@link McpJsonNull#INSTANCE} for JSON null
     * @return this builder
     * @throws IllegalArgumentException if the keyword has a typed setter
     */
    public final B keyword(String name, Object value) {
      Objects.requireNonNull(name, "name");
      if (ANNOTATIONS.contains(name) || managed.contains(name)) {
        throw new IllegalArgumentException("Use the typed setter for keyword: " + name);
      }
      return put(name, value);
    }

    /** Stores a nonnegative count constraint. */
    final B count(String name, int value) {
      if (value < 0) {
        throw new IllegalArgumentException(name + " must not be negative");
      }
      return put(name, value);
    }
  }

  /** Configures a string schema. Pattern and format semantics are checked by schema validation. */
  public static final class StringBuilder extends Builder<StringBuilder> {
    private StringBuilder() {
      super("string", "minLength", "maxLength", "pattern", "format");
    }

    /**
     * Sets the minimum string length.
     *
     * @param value the nonnegative length
     * @return this builder
     */
    public StringBuilder minLength(int value) {
      return count("minLength", value);
    }

    /**
     * Sets the maximum string length.
     *
     * @param value the nonnegative length
     * @return this builder
     */
    public StringBuilder maxLength(int value) {
      return count("maxLength", value);
    }

    /**
     * Sets a JSON Schema regular-expression constraint.
     *
     * @param value the pattern
     * @return this builder
     */
    public StringBuilder pattern(String value) {
      return put("pattern", value);
    }

    /**
     * Sets a JSON Schema format, such as {@code date-time}.
     *
     * @param value the format name
     * @return this builder
     */
    public StringBuilder format(String value) {
      return put("format", value);
    }
  }

  /** Configures JSON numeric constraints, shared by integer and number schemas. */
  public static final class NumberBuilder extends Builder<NumberBuilder> {
    private NumberBuilder(String type) {
      super(type, "minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum", "multipleOf");
    }

    /**
     * Sets the inclusive lower bound.
     *
     * @param value the finite JSON number
     * @return this builder
     */
    public NumberBuilder minimum(Number value) {
      return put("minimum", McpProtocol.copy(value));
    }

    /**
     * Sets the inclusive upper bound.
     *
     * @param value the finite JSON number
     * @return this builder
     */
    public NumberBuilder maximum(Number value) {
      return put("maximum", McpProtocol.copy(value));
    }

    /**
     * Sets the exclusive lower bound.
     *
     * @param value the finite JSON number
     * @return this builder
     */
    public NumberBuilder exclusiveMinimum(Number value) {
      return put("exclusiveMinimum", McpProtocol.copy(value));
    }

    /**
     * Sets the exclusive upper bound.
     *
     * @param value the finite JSON number
     * @return this builder
     */
    public NumberBuilder exclusiveMaximum(Number value) {
      return put("exclusiveMaximum", McpProtocol.copy(value));
    }

    /**
     * Restricts numbers to multiples of a positive value.
     *
     * @param value the finite positive JSON number
     * @return this builder
     * @throws IllegalArgumentException if the value is not positive
     */
    public NumberBuilder multipleOf(Number value) {
      Number number = McpProtocol.copy(value);
      if (new BigDecimal(number.toString()).signum() <= 0) {
        throw new IllegalArgumentException("multipleOf must be positive");
      }
      return put("multipleOf", number);
    }
  }

  /** Configures boolean schema annotations. */
  public static final class BooleanBuilder extends Builder<BooleanBuilder> {
    private BooleanBuilder() {
      super("boolean");
    }
  }

  /** Configures a homogeneous array schema. */
  public static final class ArrayBuilder extends Builder<ArrayBuilder> {
    private ArrayBuilder() {
      super("array", "items", "minItems", "maxItems", "uniqueItems");
    }

    /**
     * Sets the schema used to validate every item.
     *
     * @param value the item schema
     * @return this builder
     */
    public ArrayBuilder items(Map<String, ?> value) {
      return put("items", value);
    }

    /**
     * Sets the minimum number of items.
     *
     * @param value the nonnegative item count
     * @return this builder
     */
    public ArrayBuilder minItems(int value) {
      return count("minItems", value);
    }

    /**
     * Sets the maximum number of items.
     *
     * @param value the nonnegative item count
     * @return this builder
     */
    public ArrayBuilder maxItems(int value) {
      return count("maxItems", value);
    }

    /**
     * Controls whether duplicate items are forbidden.
     *
     * @param value whether items must be unique
     * @return this builder
     */
    public ArrayBuilder uniqueItems(boolean value) {
      return put("uniqueItems", value);
    }
  }

  /**
   * Configures a closed object schema. Property schemas may come from any map-producing library.
   */
  public static final class ObjectBuilder extends Builder<ObjectBuilder> {
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final ArrayList<String> required = new ArrayList<>();

    private ObjectBuilder() {
      super("object", "properties", "required", "additionalProperties");
      values.put("additionalProperties", false);
    }

    /**
     * Controls whether undeclared properties are accepted.
     *
     * @param value whether to accept additional properties
     * @return this builder
     */
    public ObjectBuilder additionalProperties(boolean value) {
      return put("additionalProperties", value);
    }

    /**
     * Validates every undeclared property against a schema.
     *
     * @param value the additional-property schema
     * @return this builder
     */
    public ObjectBuilder additionalProperties(Map<String, ?> value) {
      return put("additionalProperties", value);
    }

    /**
     * Adds a required property.
     *
     * @param name the property name
     * @param schema the property schema
     * @return this builder
     */
    public ObjectBuilder required(String name, Map<String, ?> schema) {
      optional(name, schema);
      required.add(name);
      values.put("required", required);
      return this;
    }

    /**
     * Adds an optional property.
     *
     * @param name the property name
     * @param schema the property schema
     * @return this builder
     * @throws IllegalArgumentException if the property has already been declared
     */
    public ObjectBuilder optional(String name, Map<String, ?> schema) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(schema, "schema");
      if (properties.putIfAbsent(name, schema) != null) {
        throw new IllegalArgumentException("Duplicate property: " + name);
      }
      values.put("properties", properties);
      return this;
    }
  }
}
