package io.github.suppierk.mcp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcResponse;
import io.github.suppierk.mcp.protocol.McpBaseMetadata;
import io.github.suppierk.mcp.protocol.McpCacheableResult;
import io.github.suppierk.mcp.protocol.McpIcon;
import io.github.suppierk.mcp.server.McpProtocolException;
import io.github.suppierk.mcp.server.McpServerKit;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.SubmissionPublisher;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.ScanOption;
import nl.jqno.equalsverifier.Warning;
import nl.jqno.equalsverifier.api.MultipleTypeEqualsVerifierApi;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

class McpArchitectureTest {
  private static final String CORE_PACKAGE = "io.github.suppierk.mcp..";
  private static final String PROTOCOL_PACKAGE = "io.github.suppierk.mcp.protocol";
  private static final String SERVER_BUILDER = "io.github.suppierk.mcp.server.McpServerKit$Builder";
  private static final String SCHEMA_VALIDATOR =
      "io.github.suppierk.mcp.server.JsonSchemaValidator";
  private static final String SERVER_OUTPUT = "io.github.suppierk.mcp.server.McpServerKit$Output";
  private static final JavaClasses CORE_CLASSES =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("io.github.suppierk.mcp");

  @Test
  void serverBuilderDoesNotExposeJsonImplementationConfiguration() {
    List<String> exposed =
        Arrays.stream(McpServerKit.Builder.class.getMethods())
            .filter(method -> method.getDeclaringClass() == McpServerKit.Builder.class)
            .filter(
                method ->
                    Arrays.stream(method.getGenericParameterTypes())
                        .anyMatch(type -> type.getTypeName().contains("jackson")))
            .map(Method::toGenericString)
            .toList();
    assertTrue(exposed.isEmpty(), () -> "JSON implementation configuration leaks: " + exposed);
  }

  @Test
  void publicCoreSignaturesDoNotExposeJacksonTypes() {
    List<String> exposed = new ArrayList<>();
    for (JavaClass imported : CORE_CLASSES) {
      Class<?> type = imported.reflect();
      if (!Modifier.isPublic(type.getModifiers())) {
        continue;
      }
      for (var constructor : type.getDeclaredConstructors()) {
        if (Modifier.isPublic(constructor.getModifiers())
            || Modifier.isProtected(constructor.getModifiers())) {
          exposed.add(constructor.toGenericString());
        }
      }
      for (var method : type.getDeclaredMethods()) {
        if (Modifier.isPublic(method.getModifiers())
            || Modifier.isProtected(method.getModifiers())) {
          exposed.add(method.toGenericString());
        }
      }
      for (var field : type.getDeclaredFields()) {
        if (Modifier.isPublic(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
          exposed.add(field.toGenericString());
        }
      }
      if (type.isRecord()) {
        for (var component : type.getRecordComponents()) {
          exposed.add(type.getName() + " " + component.getGenericType().getTypeName());
        }
      }
    }
    exposed.removeIf(signature -> !signature.contains("jackson"));
    assertTrue(exposed.isEmpty(), () -> "JSON implementation types leak: " + exposed);
  }

  @Test
  void publicCoreDeclarationsDoNotExposeJacksonAnnotations() {
    List<String> exposed = new ArrayList<>();
    for (JavaClass imported : CORE_CLASSES) {
      Class<?> type = imported.reflect();
      if (!Modifier.isPublic(type.getModifiers())) {
        continue;
      }
      collectJacksonAnnotations(type, exposed);
      for (var constructor : type.getConstructors()) {
        collectJacksonAnnotations(constructor, exposed);
        for (var parameter : constructor.getParameters()) {
          collectJacksonAnnotations(parameter, exposed);
        }
      }
      for (var method : type.getMethods()) {
        collectJacksonAnnotations(method, exposed);
        for (var parameter : method.getParameters()) {
          collectJacksonAnnotations(parameter, exposed);
        }
      }
      for (var field : type.getFields()) {
        collectJacksonAnnotations(field, exposed);
      }
      if (type.isRecord()) {
        for (var component : type.getRecordComponents()) {
          collectJacksonAnnotations(component, exposed);
        }
      }
    }
    assertTrue(exposed.isEmpty(), () -> "Jackson annotations leak: " + exposed);
  }

  private static void collectJacksonAnnotations(AnnotatedElement element, List<String> exposed) {
    Arrays.stream(element.getAnnotations())
        .filter(annotation -> annotation.annotationType().getName().contains("jackson"))
        .forEach(annotation -> exposed.add(element + " " + annotation));
  }

  @Test
  void coreExposesOnlyTheServerKitEntryPoint() {
    assertDoesNotThrow(() -> Class.forName("io.github.suppierk.mcp.server.McpServerKit"));
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("io.github.suppierk.mcp.server.McpServer"));
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("io.github.suppierk.mcp.protocol.McpErrorCodes"));
  }

  @Test
  void protocolTypesUseProtocolNames() {
    classes()
        .that()
        .resideInAPackage(PROTOCOL_PACKAGE)
        .and()
        .areTopLevelClasses()
        .should()
        .haveSimpleNameStartingWith("Mcp")
        .orShould()
        .haveSimpleNameStartingWith("JsonRpc")
        .check(CORE_CLASSES);
  }

  @Test
  void protocolDataTypesAreRecords() {
    classes()
        .that()
        .resideInAPackage(PROTOCOL_PACKAGE)
        .and()
        .areTopLevelClasses()
        .and()
        .areNotInterfaces()
        .and()
        .areNotEnums()
        .and()
        .doNotHaveSimpleName("McpProtocol")
        .should()
        .beRecords()
        .check(CORE_CLASSES);
  }

  @Test
  void publicProtocolRecordsUseTheStandardBuilder() {
    classes()
        .that()
        .resideInAPackage(PROTOCOL_PACKAGE)
        .and()
        .arePublic()
        .and()
        .areRecords()
        .should(useStandardBuilder())
        .check(CORE_CLASSES);
  }

  @Test
  void matchingProtocolRecordsImplementBaseMetadata() {
    classes()
        .that()
        .resideInAPackage(PROTOCOL_PACKAGE)
        .and()
        .arePublic()
        .and()
        .areRecords()
        .and(declareBaseMetadataComponents())
        .should()
        .implement(McpBaseMetadata.class)
        .check(CORE_CLASSES);
  }

  @Test
  void builderRuleReportsInvalidContracts() {
    assertTrue(builderViolations(MissingBuilderRecord.class).contains("does not declare Builder"));

    List<String> shapeViolations = builderViolations(BrokenBuilderRecord.class);
    assertTrue(shapeViolations.contains("Builder must have one private no-argument constructor"));
    assertTrue(
        shapeViolations.stream().anyMatch(value -> value.startsWith("Builder is missing value")));
    assertTrue(shapeViolations.contains("Builder field value must start empty"));
    assertTrue(
        shapeViolations.contains("Builder.build must be public and return BrokenBuilderRecord"));

    assertTrue(
        builderViolations(WrongFactoryRecord.class).stream()
            .anyMatch(value -> value.startsWith("wrongFactoryRecord must be")));
    assertTrue(
        builderViolations(WrongFieldRecord.class)
            .contains("Builder field value has the wrong contract"));
  }

  @Test
  void responseTypesImplementTheResponseContract() {
    classes()
        .that()
        .resideInAPackage(PROTOCOL_PACKAGE)
        .and()
        .areTopLevelClasses()
        .and()
        .areNotInterfaces()
        .and()
        .haveSimpleNameEndingWith("Response")
        .should()
        .implement(JsonRpcResponse.class)
        .check(CORE_CLASSES);
  }

  @Test
  void coreDoesNotDependOnServerFrameworks() {
    noClasses()
        .that()
        .resideInAPackage(CORE_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "io.javalin..",
            "io.micronaut..",
            "io.quarkus..",
            "jakarta.servlet..",
            "org.springframework..",
            "reactor..")
        .check(CORE_CLASSES);
  }

  @Test
  void onlyServerBuilderCreatesOrCopiesObjectMappers() {
    noClasses()
        .that()
        .doNotHaveFullyQualifiedName(SERVER_BUILDER)
        .should()
        .callConstructor(ObjectMapper.class)
        .orShould()
        .callMethod(ObjectMapper.class, "copy")
        .check(CORE_CLASSES);
  }

  @Test
  void publicMethodsDoNotReturnObjectMappers() {
    methods()
        .that()
        .areDeclaredInClassesThat()
        .resideInAPackage(CORE_PACKAGE)
        .and()
        .arePublic()
        .should()
        .notHaveRawReturnType(ObjectMapper.class)
        .check(CORE_CLASSES);
  }

  @Test
  void allCoreTypesHonorEqualsAndHashCodeContracts() {
    ObjectNode redObject =
        JsonNodeFactory.instance.objectNode().put("type", "object").put("value", "red");
    ObjectNode blueObject =
        JsonNodeFactory.instance.objectNode().put("type", "object").put("value", "blue");
    try (SubmissionPublisher<JsonRpcMessage> redPublisher = new SubmissionPublisher<>();
        SubmissionPublisher<JsonRpcMessage> bluePublisher = new SubmissionPublisher<>()) {
      MultipleTypeEqualsVerifierApi standardTypes =
          EqualsVerifier.forPackage(
              "io.github.suppierk.mcp",
              ScanOption.recursive(),
              ScanOption.except(McpServerKit.class),
              ScanOption.except(
                  type ->
                      type.getName().equals(SCHEMA_VALIDATOR)
                          || type.getName().equals(SERVER_OUTPUT)),
              ScanOption.except(McpProtocolException.class::isAssignableFrom),
              // Private serializer machinery inherits the implementation library's identity
              // semantics.
              ScanOption.except(ValueDeserializer.class::isAssignableFrom),
              ScanOption.except(
                  type ->
                      isBuilder(type)
                          || isConstantHolder(type)
                          || isTestType(type)
                          || declaresCacheScope(type)));
      verifyEqualsContracts(
          standardTypes, "light", "dark", redObject, blueObject, redPublisher, bluePublisher);

      MultipleTypeEqualsVerifierApi cacheableTypes =
          EqualsVerifier.forPackage(
              "io.github.suppierk.mcp",
              ScanOption.recursive(),
              ScanOption.except(type -> !declaresCacheScope(type)));
      verifyEqualsContracts(
          cacheableTypes,
          McpCacheableResult.CACHE_SCOPE_PRIVATE,
          McpCacheableResult.CACHE_SCOPE_PUBLIC,
          redObject,
          blueObject,
          redPublisher,
          bluePublisher);
    }
  }

  private static void verifyEqualsContracts(
      MultipleTypeEqualsVerifierApi verifier,
      String redString,
      String blueString,
      ObjectNode redObject,
      ObjectNode blueObject,
      SubmissionPublisher<JsonRpcMessage> redPublisher,
      SubmissionPublisher<JsonRpcMessage> bluePublisher) {
    verifier
        .suppress(Warning.NULL_FIELDS)
        .withPrefabValues(String.class, redString, blueString)
        .withPrefabValues(Object.class, "red JSON value", "blue JSON value")
        .withGenericPrefabValues(
            Map.class,
            (key, value) -> {
              var values = new LinkedHashMap<>();
              values.put(key, value);
              if (key instanceof String && value instanceof String) {
                values.put("type", "object");
              }
              return values;
            })
        .withPrefabValues(JsonNode.class, StringNode.valueOf("red"), StringNode.valueOf("blue"))
        .withPrefabValues(ObjectNode.class, redObject, blueObject)
        .withPrefabValues(ObjectMapper.class, new ObjectMapper(), new ObjectMapper())
        .withPrefabValues(SubmissionPublisher.class, redPublisher, bluePublisher)
        .withPrefabValues(
            McpIcon.class,
            new McpIcon(URI.create("https://red.example/icon")),
            new McpIcon(URI.create("https://blue.example/icon")))
        .verify();
  }

  private static boolean declaresCacheScope(Class<?> type) {
    return type.isRecord()
        && Arrays.stream(type.getRecordComponents())
            .anyMatch(component -> component.getName().equals("cacheScope"));
  }

  private static boolean isBuilder(Class<?> type) {
    return type.getName().endsWith("$Builder");
  }

  private static ArchCondition<JavaClass> useStandardBuilder() {
    return new ArchCondition<>("use the standard protocol builder") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        List<String> violations = builderViolations(item.reflect());
        events.add(
            new SimpleConditionEvent(
                item, violations.isEmpty(), item.getName() + " " + String.join("; ", violations)));
      }
    };
  }

  private static DescribedPredicate<JavaClass> declareBaseMetadataComponents() {
    return new DescribedPredicate<>("declare the base metadata components") {
      @Override
      public boolean test(JavaClass item) {
        RecordComponent[] components = item.reflect().getRecordComponents();
        return Arrays.stream(components)
                .anyMatch(
                    component ->
                        component.getName().equals("name")
                            && component.getGenericType().equals(String.class))
            && Arrays.stream(components)
                .anyMatch(
                    component ->
                        component.getName().equals("title")
                            && component.getType() == Optional.class
                            && component.getGenericType()
                                instanceof ParameterizedType parameterizedType
                            && Arrays.equals(
                                parameterizedType.getActualTypeArguments(),
                                new Type[] {String.class}));
      }
    };
  }

  private static List<String> builderViolations(Class<?> recordType) {
    List<String> violations = new ArrayList<>();
    Class<?> builderType =
        Arrays.stream(recordType.getDeclaredClasses())
            .filter(type -> type.getSimpleName().equals("Builder"))
            .findFirst()
            .orElse(null);
    if (builderType == null) {
      return List.of("does not declare Builder");
    }

    int builderModifiers = builderType.getModifiers();
    if (!Modifier.isPublic(builderModifiers)
        || !Modifier.isStatic(builderModifiers)
        || !Modifier.isFinal(builderModifiers)) {
      violations.add("Builder must be public, static, and final");
    }

    Constructor<?>[] constructors = builderType.getDeclaredConstructors();
    if (constructors.length != 1
        || constructors[0].getParameterCount() != 0
        || !Modifier.isPrivate(constructors[0].getModifiers())) {
      violations.add("Builder must have one private no-argument constructor");
    }

    String factoryName = lowerCamel(recordType.getSimpleName());
    Method factory = declaredMethod(recordType, factoryName);
    if (factory == null
        || !Modifier.isPublic(factory.getModifiers())
        || !Modifier.isStatic(factory.getModifiers())
        || factory.getReturnType() != builderType) {
      violations.add(factoryName + " must be a public static Builder factory");
    }

    RecordComponent[] components = recordType.getRecordComponents();
    Field[] fields = builderType.getDeclaredFields();
    if (fields.length != components.length) {
      violations.add("Builder fields must match the record components");
    }

    Object emptyBuilder = invokeFactory(factory, violations);
    for (RecordComponent component : components) {
      Field field = declaredField(builderType, component.getName());
      if (field == null) {
        violations.add("Builder is missing field " + component.getName());
        continue;
      }
      if (!Modifier.isPrivate(field.getModifiers())
          || !field.getGenericType().equals(component.getGenericType())) {
        violations.add("Builder field " + component.getName() + " has the wrong contract");
      }
      requireSetter(builderType, component.getName(), component.getGenericType(), violations);

      if (component.getType() == Optional.class) {
        Type valueType =
            ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
        requireSetter(builderType, component.getName(), valueType, violations);
        if (emptyBuilder != null
            && !Optional.empty().equals(read(field, emptyBuilder, violations))) {
          violations.add("Builder field " + component.getName() + " must start empty");
        }
      }
    }

    Method build = declaredMethod(builderType, "build");
    if (build == null
        || !Modifier.isPublic(build.getModifiers())
        || build.getReturnType() != recordType) {
      violations.add("Builder.build must be public and return " + recordType.getSimpleName());
    }
    return violations;
  }

  private static void requireSetter(
      Class<?> builderType, String name, Type parameterType, List<String> violations) {
    boolean found =
        Arrays.stream(builderType.getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .filter(method -> method.getParameterCount() == 1)
            .filter(method -> method.getGenericParameterTypes()[0].equals(parameterType))
            .anyMatch(
                method ->
                    Modifier.isPublic(method.getModifiers())
                        && method.getReturnType() == builderType);
    if (!found) {
      violations.add("Builder is missing " + name + "(" + parameterType.getTypeName() + ")");
    }
  }

  private static Method declaredMethod(Class<?> type, String name) {
    return Arrays.stream(type.getDeclaredMethods())
        .filter(method -> method.getName().equals(name))
        .filter(method -> method.getParameterCount() == 0)
        .findFirst()
        .orElse(null);
  }

  private static Field declaredField(Class<?> type, String name) {
    return Arrays.stream(type.getDeclaredFields())
        .filter(field -> field.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  private static Object invokeFactory(Method factory, List<String> violations) {
    if (factory == null) {
      return null;
    }
    try {
      return factory.invoke(null);
    } catch (ReflectiveOperationException | RuntimeException exception) {
      violations.add("Builder factory cannot create a builder");
      return null;
    }
  }

  private static Object read(Field field, Object target, List<String> violations) {
    try {
      field.setAccessible(true);
      return field.get(target);
    } catch (ReflectiveOperationException | RuntimeException exception) {
      violations.add("Builder field " + field.getName() + " cannot be inspected");
      return null;
    }
  }

  private static String lowerCamel(String value) {
    return Character.toLowerCase(value.charAt(0)) + value.substring(1);
  }

  private record MissingBuilderRecord(String value) {}

  private record BrokenBuilderRecord(Optional<String> value) {
    public static Builder brokenBuilderRecord() {
      return new Builder();
    }

    public static final class Builder {
      private Optional<String> value;

      public Builder() {}

      public Builder value(Optional<String> value) {
        this.value = value;
        return this;
      }

      public Object build() {
        return new BrokenBuilderRecord(value);
      }
    }
  }

  private record WrongFactoryRecord(String value) {
    public static Builder wrongFactory() {
      return new Builder();
    }

    public static final class Builder {
      private String value;

      private Builder() {}

      public Builder value(String value) {
        this.value = value;
        return this;
      }

      public WrongFactoryRecord build() {
        return new WrongFactoryRecord(value);
      }
    }
  }

  private record WrongFieldRecord(Optional<String> value) {
    public static Builder wrongFieldRecord() {
      return new Builder();
    }

    public static final class Builder {
      private Optional<Object> value = Optional.empty();

      private Builder() {}

      public Builder value(Optional<String> value) {
        this.value = value.map(item -> item);
        return this;
      }

      public Builder value(String value) {
        this.value = Optional.ofNullable(value);
        return this;
      }

      public WrongFieldRecord build() {
        return new WrongFieldRecord(Optional.empty());
      }
    }
  }

  private static boolean isConstantHolder(Class<?> type) {
    return Modifier.isFinal(type.getModifiers())
        && type.getDeclaredFields().length > 0
        && Arrays.stream(type.getDeclaredFields())
            .allMatch(
                field ->
                    Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers()))
        && Arrays.stream(type.getDeclaredConstructors())
            .allMatch(constructor -> Modifier.isPrivate(constructor.getModifiers()));
  }

  private static boolean isTestType(Class<?> type) {
    Class<?> enclosing = type;
    while (enclosing.getEnclosingClass() != null) {
      enclosing = enclosing.getEnclosingClass();
    }
    return enclosing.getSimpleName().endsWith("Test");
  }
}
