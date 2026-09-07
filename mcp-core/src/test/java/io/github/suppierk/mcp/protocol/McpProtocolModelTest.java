package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;

class McpProtocolModelTest {
  private static final String RESOURCE_ROOT = "mcp/2026-07-28/";
  private static final String PACKAGE = "io.github.suppierk.mcp.protocol.";
  private static final ObjectNode OBJECT_SCHEMA =
      JsonNodeFactory.instance.objectNode().put("type", "object");

  @Test
  void mapsEveryFrozenSchemaDefinitionAndDirectField() throws Exception {
    JsonNode definitions =
        JsonMapper.builder().build().readTree(resource("schema.json")).path("$defs");
    Properties mappings = properties("schema-mapping.properties");

    assertEquals(definitions.size(), mappings.size());
    definitions.fieldNames().forEachRemaining(name -> assertTrue(mappings.containsKey(name), name));
    for (String definition : mappings.stringPropertyNames()) {
      String javaType = mappings.getProperty(definition);
      if (!javaType.startsWith("Mcp") && !javaType.startsWith("JsonRpc")) {
        continue;
      }
      Class<?> type = Class.forName(PACKAGE + javaType);
      Set<String> schemaFields = schemaFields(definitions, definitions.path(definition));
      if (schemaFields.isEmpty() || !type.isRecord()) {
        continue;
      }
      Set<String> javaFields = new HashSet<>();
      for (RecordComponent component : type.getRecordComponents()) {
        JsonProperty property = component.getAnnotation(JsonProperty.class);
        javaFields.add(property == null ? component.getName() : property.value());
        schemaFields.stream()
            .filter(field -> field.endsWith("/" + component.getName()))
            .forEach(javaFields::add);
      }
      for (Method method : type.getDeclaredMethods()) {
        JsonProperty property = method.getAnnotation(JsonProperty.class);
        if (property != null) {
          javaFields.add(property.value());
        }
      }
      assertTrue(javaFields.containsAll(schemaFields), definition + " fields " + schemaFields);
    }
  }

  @Test
  void frozenSchemaMatchesItsRecordedDigest() throws Exception {
    Properties source = properties("source.properties");
    byte[] schema = resource("schema.json").readAllBytes();

    assertEquals(
        source.getProperty("sha256"), hex(MessageDigest.getInstance("SHA-256").digest(schema)));
    assertEquals(McpProtocol.REVISION, source.getProperty("revision"));
  }

  @Test
  void constructsAndReadsEveryMappedProtocolRecord() throws Exception {
    Properties mappings = properties("schema-mapping.properties");
    Map<Class<?>, Object> cache = new HashMap<>();

    for (String javaType : mappings.values().stream().map(String::valueOf).sorted().toList()) {
      if (!javaType.startsWith("Mcp") && !javaType.startsWith("JsonRpc")) {
        continue;
      }
      Class<?> type = Class.forName(PACKAGE + javaType);
      Object value = sample(type, cache, new LinkedHashSet<>());
      if (value == null || !value.getClass().isRecord()) {
        continue;
      }
      for (RecordComponent component : value.getClass().getRecordComponents()) {
        component.getAccessor().invoke(value);
      }
      for (Method method : value.getClass().getDeclaredMethods()) {
        if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0) {
          method.invoke(value);
        }
      }
    }
  }

  @Test
  void constructsEveryMappedProtocolRecordWithItsBuilder() throws Exception {
    Properties mappings = properties("schema-mapping.properties");
    Map<Class<?>, Object> cache = new HashMap<>();

    for (String javaType : mappings.values().stream().map(String::valueOf).sorted().toList()) {
      if (!javaType.startsWith("Mcp") && !javaType.startsWith("JsonRpc")) {
        continue;
      }
      Class<?> type = Class.forName(PACKAGE + javaType);
      if (!type.isRecord()) {
        continue;
      }
      buildSample(type, cache);
      for (Class<?> nested : type.getDeclaredClasses()) {
        if (nested.isRecord() && Modifier.isPublic(nested.getModifiers())) {
          buildSample(nested, cache);
        }
      }
    }
  }

  @Test
  void everyProtocolRecordDefensivelyCopiesMutableJsonComponents() throws Exception {
    List<String> failures = new java.util.ArrayList<>();
    for (Class<?> type : mappedRecordTypes()) {
      RecordComponent[] components = type.getRecordComponents();
      for (int target = 0; target < components.length; target++) {
        RecordComponent component = components[target];
        if (!containsMutableJson(component)) {
          continue;
        }
        ObjectNode supplied = mutableJson();
        Object value = constructWith(type, target, supplied);
        if (value == null) {
          continue;
        }

        supplied.put("marker", "changed-input");
        ObjectNode first = extractMutableJson(component.getAccessor().invoke(value));
        String componentName = type.getSimpleName() + "." + component.getName();
        if (!"original".equals(first.path("marker").textValue())) {
          failures.add(componentName + " retains constructor input");
        }

        first.put("marker", "changed-output");
        ObjectNode second = extractMutableJson(component.getAccessor().invoke(value));
        if (!"original".equals(second.path("marker").textValue())) {
          failures.add(componentName + " exposes accessor output");
        }
        if (first == second) {
          failures.add(componentName + " returns the same mutable instance");
        }
      }
    }
    assertEquals(List.of(), failures);
  }

  @Test
  void everyProtocolRecordDefensivelyCopiesCollectionComponents() throws Exception {
    List<String> failures = new java.util.ArrayList<>();
    for (Class<?> type : mappedRecordTypes()) {
      RecordComponent[] components = type.getRecordComponents();
      for (int target = 0; target < components.length; target++) {
        RecordComponent component = components[target];
        if (!containsMutableContainer(component)) {
          continue;
        }
        Object supplied = mutableContainer(component);
        Object value = constructWith(type, target, supplied);
        if (value == null) {
          continue;
        }

        mutateContainer(supplied);
        Object first = unwrapOptional(component.getAccessor().invoke(value));
        String componentName = type.getSimpleName() + "." + component.getName();
        if (containerSize(first) != 0) {
          failures.add(componentName + " retains constructor input");
        }
        if (canMutateContainer(first)) {
          failures.add(componentName + " exposes accessor output");
        }
      }
    }
    assertEquals(List.of(), failures);
  }

  private static List<Class<?>> mappedRecordTypes() throws Exception {
    Properties mappings = properties("schema-mapping.properties");
    Set<Class<?>> types = new LinkedHashSet<>();
    for (String javaType : mappings.values().stream().map(String::valueOf).sorted().toList()) {
      if (!javaType.startsWith("Mcp") && !javaType.startsWith("JsonRpc")) {
        continue;
      }
      Class<?> type = Class.forName(PACKAGE + javaType);
      if (type.isRecord()) {
        types.add(type);
      }
      for (Class<?> nested : type.getDeclaredClasses()) {
        if (nested.isRecord() && Modifier.isPublic(nested.getModifiers())) {
          types.add(nested);
        }
      }
    }
    return List.copyOf(types);
  }

  private static boolean containsMutableJson(RecordComponent component) {
    if (JsonNode.class.isAssignableFrom(component.getType())) {
      return true;
    }
    if (!(component.getGenericType() instanceof ParameterizedType parameterized)) {
      return false;
    }
    Type argument = parameterized.getActualTypeArguments()[0];
    return argument instanceof Class<?> argumentType
        && JsonNode.class.isAssignableFrom(argumentType);
  }

  private static boolean containsMutableContainer(RecordComponent component) {
    if (component.getType() == List.class || component.getType() == Map.class) {
      return true;
    }
    if (component.getType() != Optional.class
        || !(component.getGenericType() instanceof ParameterizedType optional)) {
      return false;
    }
    Type value = optional.getActualTypeArguments()[0];
    return value instanceof ParameterizedType container
        && (container.getRawType() == List.class || container.getRawType() == Map.class);
  }

  private static Object constructWith(Class<?> type, int target, Object supplied)
      throws ReflectiveOperationException {
    RecordComponent[] components = type.getRecordComponents();
    Class<?>[] parameterTypes = new Class<?>[components.length];
    Object[] arguments = new Object[components.length];
    Map<Class<?>, Object> cache = new HashMap<>();
    for (int index = 0; index < components.length; index++) {
      parameterTypes[index] = components[index].getType();
      arguments[index] =
          index == target
              ? supplied instanceof ObjectNode
                  ? mutableComponentValue(components[index], (ObjectNode) supplied)
                  : supplied
              : sample(type, components[index], cache, new LinkedHashSet<>());
    }
    try {
      return type.getDeclaredConstructor(parameterTypes).newInstance(arguments);
    } catch (InvocationTargetException exception) {
      if (exception.getCause() instanceof IllegalArgumentException) {
        return null;
      }
      throw new IllegalStateException("Cannot construct " + type.getName(), exception.getCause());
    }
  }

  private static Object mutableComponentValue(RecordComponent component, ObjectNode supplied) {
    if (JsonNode.class.isAssignableFrom(component.getType())) {
      return supplied;
    }
    if (component.getType() == Optional.class) {
      return Optional.of(supplied);
    }
    return List.of(supplied);
  }

  private static Object mutableContainer(RecordComponent component) {
    boolean isMap =
        component.getType() == Map.class
            || (component.getType() == Optional.class && nestedRawType(component) == Map.class);
    Object container = isMap ? new HashMap<>() : new java.util.ArrayList<>();
    return component.getType() == Optional.class ? Optional.of(container) : container;
  }

  private static Type nestedRawType(RecordComponent component) {
    ParameterizedType optional = (ParameterizedType) component.getGenericType();
    return ((ParameterizedType) optional.getActualTypeArguments()[0]).getRawType();
  }

  private static Object unwrapOptional(Object value) {
    return value instanceof Optional<?> optional ? optional.orElseThrow() : value;
  }

  @SuppressWarnings("unchecked")
  private static void mutateContainer(Object value) {
    Object container = unwrapOptional(value);
    if (container instanceof List<?> list) {
      ((List<Object>) list).add(null);
    } else {
      ((Map<Object, Object>) container).put("later", null);
    }
  }

  private static int containerSize(Object value) {
    return value instanceof List<?> list ? list.size() : ((Map<?, ?>) value).size();
  }

  @SuppressWarnings("unchecked")
  private static boolean canMutateContainer(Object value) {
    try {
      if (value instanceof List<?> list) {
        ((List<Object>) list).add(null);
      } else {
        ((Map<Object, Object>) value).put("later", null);
      }
      return true;
    } catch (UnsupportedOperationException expected) {
      return false;
    }
  }

  private static ObjectNode extractMutableJson(Object value) {
    if (value instanceof Optional<?> optional) {
      return (ObjectNode) optional.orElseThrow();
    }
    if (value instanceof List<?> values) {
      return (ObjectNode) values.get(0);
    }
    return (ObjectNode) value;
  }

  private static ObjectNode mutableJson() {
    return JsonNodeFactory.instance.objectNode().put("type", "object").put("marker", "original");
  }

  private static Set<String> schemaFields(JsonNode definitions, JsonNode definition) {
    Set<String> fields = new HashSet<>();
    definition.path("properties").fieldNames().forEachRemaining(fields::add);
    for (JsonNode part : definition.path("allOf")) {
      JsonNode reference = part.get("$ref");
      if (reference != null) {
        String name = reference.textValue().substring(reference.textValue().lastIndexOf('/') + 1);
        fields.addAll(schemaFields(definitions, definitions.path(name)));
      } else {
        fields.addAll(schemaFields(definitions, part));
      }
    }
    return fields;
  }

  private static Object sample(
      Class<?> rawType, Map<Class<?>, Object> cache, Set<Class<?>> visiting)
      throws ReflectiveOperationException {
    if (rawType == String.class) {
      return "value";
    }
    if (rawType == boolean.class || rawType == Boolean.class) {
      return false;
    }
    if (rawType == int.class || rawType == Integer.class) {
      return 0;
    }
    if (rawType == long.class || rawType == Long.class) {
      return 0L;
    }
    if (rawType == double.class || rawType == Double.class) {
      return 0D;
    }
    if (rawType == URI.class) {
      return URI.create("urn:mcp:test");
    }
    if (rawType == ObjectNode.class) {
      return OBJECT_SCHEMA.deepCopy();
    }
    if (rawType == ArrayNode.class) {
      return JsonNodeFactory.instance.arrayNode();
    }
    if (rawType == JsonNode.class || rawType == Object.class) {
      return JsonNodeFactory.instance.textNode("value");
    }
    if (rawType == Optional.class) {
      return Optional.empty();
    }
    if (rawType == List.class) {
      return List.of();
    }
    if (rawType == Map.class) {
      return Map.of();
    }
    if (rawType.isEnum()) {
      return rawType.getEnumConstants()[0];
    }
    Object cached = cache.get(rawType);
    if (cached != null) {
      return cached;
    }
    if (rawType.isInterface()) {
      Class<?>[] permitted = rawType.getPermittedSubclasses();
      if (permitted == null || permitted.length == 0) {
        return null;
      }
      for (Class<?> implementation : permitted) {
        if (!visiting.contains(implementation)) {
          Object value = sample(implementation, cache, visiting);
          if (value != null) {
            return value;
          }
        }
      }
      return null;
    }
    if (!rawType.isRecord() || !visiting.add(rawType)) {
      return null;
    }
    try {
      RecordComponent[] components = rawType.getRecordComponents();
      Class<?>[] parameterTypes = new Class<?>[components.length];
      Object[] arguments = new Object[components.length];
      for (int index = 0; index < components.length; index++) {
        parameterTypes[index] = components[index].getType();
        arguments[index] = sample(rawType, components[index], cache, new LinkedHashSet<>(visiting));
      }
      Constructor<?> constructor = rawType.getDeclaredConstructor(parameterTypes);
      Object value = constructor.newInstance(arguments);
      cache.put(rawType, value);
      return value;
    } catch (InvocationTargetException exception) {
      throw new IllegalStateException(
          "Cannot construct " + rawType.getName(), exception.getCause());
    } finally {
      visiting.remove(rawType);
    }
  }

  private static Object buildSample(Class<?> type, Map<Class<?>, Object> cache)
      throws ReflectiveOperationException {
    Method factory = type.getDeclaredMethod(lowerCamel(type.getSimpleName()));
    Object builder = factory.invoke(null);
    for (RecordComponent component : type.getRecordComponents()) {
      Method setter =
          builder.getClass().getDeclaredMethod(component.getName(), component.getType());
      Object value = sample(type, component, cache, new LinkedHashSet<>());
      setter.invoke(builder, value);
      if (component.getType() == Optional.class) {
        Type valueType =
            ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
        Method valueSetter =
            builder.getClass().getDeclaredMethod(component.getName(), rawType(valueType));
        valueSetter.invoke(builder, new Object[] {null});
      }
    }
    Object value = builder.getClass().getDeclaredMethod("build").invoke(builder);
    assertEquals(type, value.getClass());
    return value;
  }

  private static Object sample(
      Class<?> owner,
      RecordComponent component,
      Map<Class<?>, Object> cache,
      Set<Class<?>> visiting)
      throws ReflectiveOperationException {
    if (component.getType() == String.class) {
      String prefix = component.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase() + "_";
      for (Field field : owner.getFields()) {
        if (field.getType() == String.class
            && Modifier.isStatic(field.getModifiers())
            && Modifier.isFinal(field.getModifiers())
            && field.getName().startsWith(prefix)) {
          return field.get(null);
        }
      }
    }
    return sample(component.getType(), cache, visiting);
  }

  private static Class<?> rawType(Type type) {
    return type instanceof ParameterizedType parameterizedType
        ? (Class<?>) parameterizedType.getRawType()
        : (Class<?>) type;
  }

  private static String lowerCamel(String value) {
    return Character.toLowerCase(value.charAt(0)) + value.substring(1);
  }

  private static Properties properties(String name) throws IOException {
    Properties properties = new Properties();
    try (InputStream input = resource(name)) {
      properties.load(input);
    }
    return properties;
  }

  private static InputStream resource(String name) {
    InputStream input =
        McpProtocolModelTest.class.getClassLoader().getResourceAsStream(RESOURCE_ROOT + name);
    if (input == null) {
      throw new IllegalStateException("Missing test resource " + name);
    }
    return input;
  }

  private static String hex(byte[] bytes) {
    StringBuilder value = new StringBuilder(bytes.length * 2);
    for (byte current : bytes) {
      value.append(String.format("%02x", current));
    }
    return value.toString();
  }
}
