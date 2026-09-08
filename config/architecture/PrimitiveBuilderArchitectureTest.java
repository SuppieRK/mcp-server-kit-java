package io.github.suppierk.mcp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Checks construction capabilities, not constructor call sites. Public concrete values include
 * records and ordinary classes. Services and utilities have explicit exclusions; a new data class
 * cannot escape the contract just by not being a record.
 */
class PrimitiveBuilderArchitectureTest {
  // A correctly typed signature without a behavior case must not make a production edge pass.
  private static final List<PrimitiveBuilderCase<?, ?>> DELEGATION_CASES =
      Stream.concat(
              ToolResultBuilderTest.cases().stream(),
              ProtocolBuilderDelegationTest.cases().stream())
          .toList();

  private static final Set<String> NON_VALUES =
      Set.of(
          "io.github.suppierk.mcp.protocol.McpProtocol",
          "io.github.suppierk.mcp.protocol.McpJsonSchema",
          "io.github.suppierk.mcp.server.McpServerKit",
          "io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport",
          "io.github.suppierk.mcp.transport.http.HttpEventStreamResponse",
          "io.github.suppierk.mcp.transport.stdio.StdioMcpTransport",
          "io.github.suppierk.mcp.spring.webmvc.SpringWebMvcMcpAdapter",
          "io.github.suppierk.mcp.spring.webflux.SpringWebFluxMcpAdapter",
          "io.github.suppierk.mcp.javalin.JavalinMcpAdapter");

  private static final JavaClasses PRODUCTION =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("io.github.suppierk.mcp");

  private static final ArchRule BUILDERS =
      classes()
          .that()
          .arePublic()
          .should(
              new ArchCondition<JavaClass>(
                  "expose a class-named factory and a typed Builder.build") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                  Class<?> primitive = item.reflect();
                  if (!isPrimitive(primitive)) {
                    return;
                  }
                  Optional<Class<?>> builder = builderOf(primitive);
                  boolean valid =
                      builder.isPresent()
                          && Arrays.stream(primitive.getMethods())
                              .anyMatch(
                                  method ->
                                      Modifier.isStatic(method.getModifiers())
                                          && method.getParameterCount() == 0
                                          && method
                                              .getName()
                                              .equals(
                                                  FluentBuilderArchitectureTest.factoryName(
                                                      primitive))
                                          && method.getReturnType() == builder.get())
                          && Arrays.stream(builder.get().getMethods())
                              .anyMatch(
                                  method ->
                                      !Modifier.isStatic(method.getModifiers())
                                          && method.getName().equals("build")
                                          && method.getParameterCount() == 0
                                          && method.getReturnType() == primitive);
                  events.add(
                      new SimpleConditionEvent(
                          item,
                          valid,
                          primitive.getName()
                              + " must expose a public Builder, "
                              + FluentBuilderArchitectureTest.factoryName(primitive)
                              + "(), and Builder.build() returning "
                              + primitive.getSimpleName()));
                }
              });

  @Test
  void everyPublicDataPrimitiveHasABuilder() {
    BUILDERS.check(PRODUCTION);
  }

  @Test
  void everyCompositePrimitiveOffersFluentChildBuilderConfiguration() {
    composites(PRODUCTION).check(PRODUCTION);
  }

  @Test
  void builderRuleDetectsMissingBuildersIncludingNestedRecords() {
    var missing = new ClassFileImporter().importClasses(MissingBuilder.class);
    AssertionError failure = assertThrows(AssertionError.class, () -> BUILDERS.check(missing));
    assertTrue(failure.getMessage().contains(MissingBuilder.class.getName()));
    var valid = new ClassFileImporter().importClasses(Child.class, OtherChild.class);
    assertDoesNotThrow(() -> BUILDERS.check(valid));
  }

  @Test
  void builderRuleAlsoRejectsNonRecordDataClassesWithoutBuilders() {
    var missing = new ClassFileImporter().importClasses(PlainValue.class, Child.class);
    AssertionError failure = assertThrows(AssertionError.class, () -> BUILDERS.check(missing));
    assertTrue(failure.getMessage().contains(PlainValue.class.getName()));
  }

  @Test
  void compositeRuleRejectsBuiltValuesRawCallbacksAndBrokenFluentReturns() {
    var invalid =
        new ClassFileImporter()
            .importClasses(NonDelegatingComposite.class, Child.class, OtherChild.class);
    AssertionError failure =
        assertThrows(AssertionError.class, () -> composites(invalid).check(invalid));
    for (String property : List.of("direct", "optional", "items", "byName")) {
      assertTrue(
          failure.getMessage().contains(NonDelegatingComposite.class.getName() + "." + property));
    }
    assertTrue(failure.getMessage().contains(Child.Builder.class.getTypeName()));
    assertTrue(failure.getMessage().contains(OtherChild.Builder.class.getTypeName()));
  }

  @Test
  void compositeRuleAcceptsTypedFluentDelegationIncludingEveryUnionVariant() {
    var valid =
        new ClassFileImporter()
            .importClasses(DelegatingComposite.class, Child.class, OtherChild.class);
    var cases =
        List.of(
            new PrimitiveBuilderCase<>(
                DelegatingComposite.class,
                "items",
                Child.class,
                "child",
                DelegatingComposite::delegatingComposite,
                (Child.Builder child) -> child.value("configured"),
                List.of(new Child("configured")),
                List.of()),
            new PrimitiveBuilderCase<>(
                DelegatingComposite.class,
                "items",
                OtherChild.class,
                "otherChild",
                DelegatingComposite::delegatingComposite,
                (OtherChild.Builder child) -> child.value("configured"),
                List.of(new OtherChild("configured")),
                List.of()));
    assertDoesNotThrow(() -> composites(valid, cases).check(valid));
  }

  @Test
  void compositeRuleRequiresSeparateCoverageForPropertiesSharingAChildType() {
    var invalid = new ClassFileImporter().importClasses(SameTypeComposite.class, Child.class);
    var first =
        new PrimitiveBuilderCase<>(
            SameTypeComposite.class,
            "first",
            Child.class,
            "first",
            SameTypeComposite::sameTypeComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    AssertionError failure =
        assertThrows(
            AssertionError.class, () -> composites(invalid, List.of(first)).check(invalid));
    assertTrue(failure.getMessage().contains(SameTypeComposite.class.getName() + ".second"));
    assertFalse(failure.getMessage().contains(SameTypeComposite.class.getName() + ".first"));
  }

  @Test
  void compositeRuleRejectsCorrectlyTypedCallbacksThatDoNothing() {
    var invalid = new ClassFileImporter().importClasses(NoOpComposite.class, Child.class);
    var example =
        new PrimitiveBuilderCase<>(
            NoOpComposite.class,
            "child",
            Child.class,
            "child",
            NoOpComposite::noOpComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    AssertionError failure =
        assertThrows(
            AssertionError.class, () -> composites(invalid, List.of(example)).check(invalid));
    assertTrue(failure.getMessage().contains(NoOpComposite.class.getName() + ".child"));
  }

  @Test
  void compositeRuleRejectsCallbacksWhoseConfiguredChildIsDiscarded() {
    var invalid = new ClassFileImporter().importClasses(DiscardingComposite.class, Child.class);
    var example =
        new PrimitiveBuilderCase<>(
            DiscardingComposite.class,
            "child",
            Child.class,
            "child",
            DiscardingComposite::discardingComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    AssertionError failure =
        assertThrows(
            AssertionError.class, () -> composites(invalid, List.of(example)).check(invalid));
    assertTrue(failure.getMessage().contains(DiscardingComposite.class.getName() + ".child"));
  }

  @Test
  void compositeRuleRejectsDelegationThatChangesASiblingProperty() {
    var invalid = new ClassFileImporter().importClasses(CorruptingComposite.class, Child.class);
    var example =
        new PrimitiveBuilderCase<>(
            CorruptingComposite.class,
            "child",
            Child.class,
            "child",
            CorruptingComposite::corruptingComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    AssertionError failure =
        assertThrows(
            AssertionError.class, () -> composites(invalid, List.of(example)).check(invalid));
    assertTrue(failure.getMessage().contains("label"));
  }

  @Test
  void compositeRuleIncludesPublicFieldsOfNonRecordDataClasses() {
    var invalid = new ClassFileImporter().importClasses(PlainComposite.class, Child.class);
    AssertionError failure =
        assertThrows(AssertionError.class, () -> composites(invalid).check(invalid));
    assertTrue(failure.getMessage().contains(PlainComposite.class.getName() + ".child"));
  }

  @Test
  void compositeRuleAcceptsAnExecutedPublicFieldCaseForANonRecordValue() {
    var valid = new ClassFileImporter().importClasses(PlainComposite.class, Child.class);
    var example =
        new PrimitiveBuilderCase<>(
            PlainComposite.class,
            "child",
            Child.class,
            "child",
            PlainComposite::plainComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    assertDoesNotThrow(() -> BUILDERS.check(valid));
    assertDoesNotThrow(() -> composites(valid, List.of(example)).check(valid));
  }

  @Test
  void compositeRuleDoesNotLetOneTestedMethodHideAnUntestedCallback() {
    var invalid = new ClassFileImporter().importClasses(PartlyTestedComposite.class, Child.class);
    var example =
        new PrimitiveBuilderCase<>(
            PartlyTestedComposite.class,
            "child",
            Child.class,
            "child",
            PartlyTestedComposite::partlyTestedComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    AssertionError failure =
        assertThrows(
            AssertionError.class, () -> composites(invalid, List.of(example)).check(invalid));
    assertTrue(failure.getMessage().contains("ignoredChild"));
  }

  @Test
  void compositeRuleAcceptsDirectOptionalCollectionMapAndArrayConstruction() {
    var valid = new ClassFileImporter().importClasses(ContainerComposite.class, Child.class);
    assertDoesNotThrow(
        () ->
            composites(valid, containerCases(ContainerComposite::containerComposite)).check(valid));
  }

  @Test
  void compositeRuleRejectsAChildStoredUnderTheWrongMapKey() {
    var invalid = new ClassFileImporter().importClasses(ContainerComposite.class, Child.class);
    AssertionError failure =
        assertThrows(
            AssertionError.class,
            () ->
                composites(
                        invalid, containerCases(() -> ContainerComposite.containerComposite(true)))
                    .check(invalid));
    assertTrue(failure.getMessage().contains(ContainerComposite.class.getName() + ".byName"));
    assertTrue(failure.getMessage().contains("requested-key"));
  }

  @Test
  void compositeRuleRejectsRelabelingOneCallbackAsCoverageForAnotherProperty() {
    var invalid = new ClassFileImporter().importClasses(SameTypeComposite.class, Child.class);
    var first =
        new PrimitiveBuilderCase<>(
            SameTypeComposite.class,
            "first",
            Child.class,
            "first",
            SameTypeComposite::sameTypeComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    var second =
        new PrimitiveBuilderCase<>(
            SameTypeComposite.class,
            "second",
            Child.class,
            "first",
            SameTypeComposite::sameTypeComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    AssertionError failure =
        assertThrows(
            AssertionError.class, () -> composites(invalid, List.of(first, second)).check(invalid));
    assertTrue(failure.getMessage().contains(SameTypeComposite.class.getName() + ".second"));
    assertFalse(failure.getMessage().contains(SameTypeComposite.class.getName() + ".first"));
  }

  @Test
  void compositeRuleAcceptsAccessorBasedValuesAndNonRecordChildrenBehindInterfaces() {
    var valid =
        new ClassFileImporter()
            .importClasses(GetterComposite.class, PlainChildHolder.class, Child.class);
    var inner =
        new PrimitiveBuilderCase<>(
            GetterComposite.class,
            "child",
            Child.class,
            "child",
            GetterComposite::getterComposite,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of());
    var outer =
        new PrimitiveBuilderCase<>(
            PlainChildHolder.class,
            "child",
            GetterComposite.class,
            "nested",
            PlainChildHolder::plainChildHolder,
            (GetterComposite.Builder child) -> child.child(value -> value.value("configured")),
            new GetterComposite(new Child("configured")),
            List.of());
    assertDoesNotThrow(() -> BUILDERS.check(valid));
    assertDoesNotThrow(() -> composites(valid, List.of(inner, outer)).check(valid));
  }

  private static List<PrimitiveBuilderCase<?, ?>> containerCases(
      Supplier<ContainerComposite.Builder> factory) {
    return List.of(
        new PrimitiveBuilderCase<>(
            ContainerComposite.class,
            "direct",
            Child.class,
            "direct",
            factory,
            (Child.Builder child) -> child.value("configured"),
            new Child("configured"),
            List.of()),
        new PrimitiveBuilderCase<>(
            ContainerComposite.class,
            "optional",
            Child.class,
            "optional",
            factory,
            (Child.Builder child) -> child.value("configured"),
            Optional.of(new Child("configured")),
            List.of()),
        new PrimitiveBuilderCase<>(
            ContainerComposite.class,
            "items",
            Child.class,
            "add",
            factory,
            (Child.Builder child) -> child.value("configured"),
            List.of(new Child("configured")),
            List.of()),
        new PrimitiveBuilderCase<>(
            ContainerComposite.class,
            "byName",
            Child.class,
            "entry",
            factory,
            (Child.Builder child) -> child.value("configured"),
            Map.of("requested-key", new Child("configured")),
            List.of("requested-key")),
        new PrimitiveBuilderCase<>(
            ContainerComposite.class,
            "array",
            Child.class,
            "element",
            factory,
            (Child.Builder child) -> child.value("configured"),
            new Child[] {new Child("configured")},
            List.of()));
  }

  private static ArchRule composites(JavaClasses imported) {
    return composites(imported, DELEGATION_CASES);
  }

  private static ArchRule composites(
      JavaClasses imported, List<? extends PrimitiveBuilderCase<?, ?>> cases) {
    List<Class<?>> primitives =
        imported.stream()
            .map(JavaClass::reflect)
            .filter(PrimitiveBuilderArchitectureTest::isPrimitive)
            .toList();
    return classes()
        .that()
        .arePublic()
        .should(
            new ArchCondition<JavaClass>("offer typed fluent configuration of each child builder") {
              @Override
              public void check(JavaClass item, ConditionEvents events) {
                Class<?> primitive = item.reflect();
                if (!isPrimitive(primitive)) {
                  return;
                }
                Optional<Class<?>> parentBuilder = builderOf(primitive);
                Set<Method> callbacks = new LinkedHashSet<>();
                Set<Method> exercised = new LinkedHashSet<>();
                for (var component : properties(primitive)) {
                  for (Class<?> child : nestedValues(component.type(), primitives)) {
                    Optional<Class<?>> childBuilder = builderOf(child);
                    List<Method> candidates =
                        parentBuilder.isPresent() && childBuilder.isPresent()
                            ? Arrays.stream(parentBuilder.get().getMethods())
                                .filter(
                                    method ->
                                        delegatesTo(
                                            method, parentBuilder.get(), childBuilder.get()))
                                .toList()
                            : List.of();
                    callbacks.addAll(candidates);
                    boolean supported = !candidates.isEmpty();
                    List<? extends PrimitiveBuilderCase<?, ?>> matching =
                        cases.stream()
                            .filter(
                                example ->
                                    example.parent() == primitive
                                        && example.property().equals(component.name())
                                        && example.child() == child)
                            .toList();
                    String problem = "";
                    if (supported) {
                      supported = !matching.isEmpty();
                      problem = "; missing property-specific public-API behavior case";
                      try {
                        for (var example : matching) {
                          exercised.add(example.method());
                          example.verify();
                        }
                      } catch (ReflectiveOperationException | AssertionError failure) {
                        supported = false;
                        problem = "; public-API behavior case failed: " + failure;
                      }
                    }
                    events.add(
                        new SimpleConditionEvent(
                            item,
                            supported,
                            primitive.getName()
                                + "."
                                + component.name()
                                + " must offer fluent Consumer<"
                                + childBuilder
                                    .map(Class::getTypeName)
                                    .orElse(child.getName() + "$Builder")
                                + "> configuration on its Builder"
                                + problem));
                  }
                }
                for (Method callback : callbacks) {
                  events.add(
                      new SimpleConditionEvent(
                          item,
                          exercised.contains(callback),
                          callback.toGenericString()
                              + " must have a property-specific public-API behavior case"));
                }
              }
            });
  }

  static Optional<Class<?>> builderOf(Class<?> primitive) {
    return Arrays.stream(primitive.getDeclaredClasses())
        .filter(type -> type.getSimpleName().equals("Builder"))
        .filter(type -> Modifier.isPublic(type.getModifiers()))
        .findFirst();
  }

  static List<Property> properties(Class<?> primitive) {
    if (primitive.isRecord()) {
      return Arrays.stream(primitive.getRecordComponents())
          .map(
              component ->
                  new Property(
                      component.getName(), component.getGenericType(), component.getAccessor()))
          .toList();
    }
    Map<String, Property> properties = new LinkedHashMap<>();
    for (Field field : primitive.getFields()) {
      if (!Modifier.isStatic(field.getModifiers())) {
        properties.put(
            field.getName(), new Property(field.getName(), field.getGenericType(), field));
      }
    }
    Arrays.stream(primitive.getMethods())
        .filter(
            method ->
                !Modifier.isStatic(method.getModifiers())
                    && method.getParameterCount() == 0
                    && method.getReturnType() != void.class
                    && !method.isSynthetic()
                    && !FluentBuilderArchitectureTest.isObjectMethod(method))
        .map(method -> new Property(method.getName(), method.getGenericReturnType(), method))
        .forEach(property -> properties.putIfAbsent(property.name(), property));
    return List.copyOf(properties.values());
  }

  record Property(String name, Type type, AccessibleObject accessor) {
    Object read(Object value) throws ReflectiveOperationException {
      return accessor instanceof Method method
          ? method.invoke(value)
          : ((Field) accessor).get(value);
    }
  }

  private static boolean isPrimitive(Class<?> type) {
    return Modifier.isPublic(type.getModifiers())
        && !Modifier.isAbstract(type.getModifiers())
        && !type.isInterface()
        && !type.isEnum()
        && !Throwable.class.isAssignableFrom(type)
        && (type.isRecord() || !type.getSimpleName().endsWith("Builder"))
        && !type.getPackageName().startsWith("io.github.suppierk.mcp.internal")
        && !NON_VALUES.contains(type.getName());
  }

  /**
   * Follows typed values only: Object and untyped JSON maps do not promise a particular primitive.
   */
  private static Set<Class<?>> nestedValues(Type type, List<Class<?>> primitives) {
    Set<Class<?>> nested = new LinkedHashSet<>();
    if (type instanceof ParameterizedType parameterized) {
      Type raw = parameterized.getRawType();
      if (raw == Optional.class
          || raw instanceof Class<?> value && Collection.class.isAssignableFrom(value)) {
        nested.addAll(nestedValues(parameterized.getActualTypeArguments()[0], primitives));
      } else if (raw instanceof Class<?> value && Map.class.isAssignableFrom(value)) {
        nested.addAll(nestedValues(parameterized.getActualTypeArguments()[1], primitives));
      }
    } else if (type instanceof WildcardType wildcard) {
      for (Type bound : wildcard.getUpperBounds()) {
        nested.addAll(nestedValues(bound, primitives));
      }
    } else if (type instanceof Class<?> value) {
      if (value.isArray()) {
        nested.addAll(nestedValues(value.getComponentType(), primitives));
      } else if (value.getPackageName().startsWith("io.github.suppierk.mcp")) {
        primitives.stream().filter(value::isAssignableFrom).forEach(nested::add);
      }
    }
    return nested;
  }

  /** Accepts property setters or subtype-specific adders; does not prescribe their spelling. */
  static boolean delegatesTo(Method method, Class<?> parentBuilder, Class<?> childBuilder) {
    if (Modifier.isStatic(method.getModifiers()) || method.getReturnType() != parentBuilder) {
      return false;
    }
    return Arrays.stream(method.getGenericParameterTypes())
        .anyMatch(
            type -> {
              if (!(type instanceof ParameterizedType parameterized)
                  || parameterized.getRawType() != Consumer.class) {
                return false;
              }
              Type argument = parameterized.getActualTypeArguments()[0];
              return argument.equals(childBuilder)
                  || argument instanceof WildcardType wildcard
                      && Arrays.equals(wildcard.getLowerBounds(), new Type[] {childBuilder});
            });
  }

  public record MissingBuilder(String value) {}

  public static final class PlainValue {
    private final String value;

    public PlainValue(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }
  }

  public sealed interface Part permits Child, OtherChild {}

  public record Child(String value) implements Part {
    public static Builder child() {
      return new Builder();
    }

    public static final class Builder {
      private String value;

      private Builder() {}

      public Builder value(String value) {
        this.value = value;
        return this;
      }

      public Child build() {
        return new Child(value);
      }
    }
  }

  public record OtherChild(String value) implements Part {
    public static Builder otherChild() {
      return new Builder();
    }

    public static final class Builder {
      private String value;

      private Builder() {}

      public Builder value(String value) {
        this.value = value;
        return this;
      }

      public OtherChild build() {
        return new OtherChild(value);
      }
    }
  }

  public record NonDelegatingComposite(
      Child direct, Optional<Child> optional, List<Part> items, Map<String, Child> byName) {
    public static final class Builder {
      private Builder() {}

      public Builder direct(Child value) {
        return this;
      }

      public Builder optional(Consumer configure) {
        return this;
      }

      public Builder items(Consumer<Object> configure) {
        return this;
      }

      public void byName(String name, Consumer<Child.Builder> configure) {}
    }
  }

  public record DelegatingComposite(List<Part> items) {
    public static Builder delegatingComposite() {
      return new Builder();
    }

    public static final class Builder {
      private final List<Part> items = new ArrayList<>();

      private Builder() {}

      public Builder child(Consumer<Child.Builder> configure) {
        var child = Child.child();
        configure.accept(child);
        items.add(child.build());
        return this;
      }

      public Builder otherChild(Consumer<? super OtherChild.Builder> configure) {
        var child = OtherChild.otherChild();
        configure.accept(child);
        items.add(child.build());
        return this;
      }

      public DelegatingComposite build() {
        return new DelegatingComposite(List.copyOf(items));
      }
    }
  }

  public record SameTypeComposite(Child first, Child second) {
    public static Builder sameTypeComposite() {
      return new Builder();
    }

    public static final class Builder {
      private Child first = new Child("initial-first");
      private final Child second = new Child("initial-second");

      private Builder() {}

      public Builder first(Consumer<Child.Builder> configure) {
        var child = Child.child();
        configure.accept(child);
        first = child.build();
        return this;
      }

      public SameTypeComposite build() {
        return new SameTypeComposite(first, second);
      }
    }
  }

  public record NoOpComposite(Child child) {
    public static Builder noOpComposite() {
      return new Builder();
    }

    public static final class Builder {
      private Builder() {}

      public Builder child(Consumer<Child.Builder> configure) {
        return this;
      }

      public NoOpComposite build() {
        return new NoOpComposite(new Child("initial"));
      }
    }
  }

  public record DiscardingComposite(Child child) {
    public static Builder discardingComposite() {
      return new Builder();
    }

    public static final class Builder {
      private Builder() {}

      public Builder child(Consumer<Child.Builder> configure) {
        configure.accept(Child.child());
        return this;
      }

      public DiscardingComposite build() {
        return new DiscardingComposite(new Child("initial"));
      }
    }
  }

  public record CorruptingComposite(Child child, String label) {
    public static Builder corruptingComposite() {
      return new Builder();
    }

    public static final class Builder {
      private Child child = new Child("initial");
      private String label = "untouched";

      private Builder() {}

      public Builder child(Consumer<Child.Builder> configure) {
        var builder = Child.child();
        configure.accept(builder);
        child = builder.build();
        label = "changed";
        return this;
      }

      public CorruptingComposite build() {
        return new CorruptingComposite(child, label);
      }
    }
  }

  public static final class PlainComposite {
    public final Child child;

    private PlainComposite(Child child) {
      this.child = child;
    }

    public static Builder plainComposite() {
      return new Builder();
    }

    public static final class Builder {
      private Child child = new Child("initial");

      private Builder() {}

      public Builder child(Consumer<Child.Builder> configure) {
        var builder = Child.child();
        configure.accept(builder);
        child = builder.build();
        return this;
      }

      public PlainComposite build() {
        return new PlainComposite(child);
      }
    }
  }

  public record PartlyTestedComposite(Child child) {
    public static Builder partlyTestedComposite() {
      return new Builder();
    }

    public static final class Builder {
      private Child child = new Child("initial");

      private Builder() {}

      public Builder child(Consumer<Child.Builder> configure) {
        var builder = Child.child();
        configure.accept(builder);
        child = builder.build();
        return this;
      }

      public Builder ignoredChild(Consumer<Child.Builder> configure) {
        return this;
      }

      public PartlyTestedComposite build() {
        return new PartlyTestedComposite(child);
      }
    }
  }

  public record ContainerComposite(
      Child direct,
      Optional<Child> optional,
      List<Child> items,
      Map<String, Child> byName,
      Child[] array) {
    public static Builder containerComposite() {
      return new Builder(false);
    }

    public static Builder containerComposite(boolean wrongKey) {
      return new Builder(wrongKey);
    }

    public static final class Builder {
      private final boolean wrongKey;
      private Child direct = new Child("initial");
      private Optional<Child> optional = Optional.empty();
      private final List<Child> items = new ArrayList<>();
      private final Map<String, Child> byName = new LinkedHashMap<>();
      private Child[] array = new Child[0];

      private Builder(boolean wrongKey) {
        this.wrongKey = wrongKey;
      }

      public Builder direct(Consumer<Child.Builder> configure) {
        var child = Child.child();
        configure.accept(child);
        direct = child.build();
        return this;
      }

      public Builder optional(Consumer<? super Child.Builder> configure) {
        var child = Child.child();
        configure.accept(child);
        optional = Optional.of(child.build());
        return this;
      }

      public Builder add(Consumer<Child.Builder> configure) {
        var child = Child.child();
        configure.accept(child);
        items.add(child.build());
        return this;
      }

      public Builder entry(String key, Consumer<Child.Builder> configure) {
        var child = Child.child();
        configure.accept(child);
        byName.put(wrongKey ? "wrong-key" : key, child.build());
        return this;
      }

      public Builder element(Consumer<Child.Builder> configure) {
        var child = Child.child();
        configure.accept(child);
        array = new Child[] {child.build()};
        return this;
      }

      public ContainerComposite build() {
        return new ContainerComposite(
            direct, optional, List.copyOf(items), Map.copyOf(byName), array.clone());
      }
    }
  }

  public interface PlainPart {}

  public static final class GetterComposite implements PlainPart {
    private final Child child;

    private GetterComposite(Child child) {
      this.child = child;
    }

    public Child child() {
      return child;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof GetterComposite value && Objects.equals(child, value.child);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(child);
    }

    public static Builder getterComposite() {
      return new Builder();
    }

    public static final class Builder {
      private Child child = new Child("initial");

      private Builder() {}

      public Builder child(Consumer<Child.Builder> configure) {
        var builder = Child.child();
        configure.accept(builder);
        child = builder.build();
        return this;
      }

      public GetterComposite build() {
        return new GetterComposite(child);
      }
    }
  }

  public record PlainChildHolder(PlainPart child) {
    public static Builder plainChildHolder() {
      return new Builder();
    }

    public static final class Builder {
      private PlainPart child = new GetterComposite(new Child("initial"));

      private Builder() {}

      public Builder nested(Consumer<GetterComposite.Builder> configure) {
        var builder = GetterComposite.getterComposite();
        configure.accept(builder);
        child = builder.build();
        return this;
      }

      public PlainChildHolder build() {
        return new PlainChildHolder(child);
      }
    }
  }
}
