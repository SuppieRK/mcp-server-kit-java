package io.github.suppierk.mcp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Shared by every published module so new builders outside the core get the same checks. */
class FluentBuilderArchitectureTest {
  private static final JavaClasses PRODUCTION =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("io.github.suppierk.mcp");

  private static final ArchRule FACTORIES =
      classes()
          .that()
          .arePublic()
          .should(
              new ArchCondition<JavaClass>(
                  "name builder factories after their enclosing class path") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                  Class<?> owner = item.reflect();
                  for (Method method : owner.getDeclaredMethods()) {
                    if (Modifier.isPublic(method.getModifiers())
                        && Modifier.isStatic(method.getModifiers())
                        && isBuilder(method.getReturnType())
                        && !method.isSynthetic()) {
                      String expected = factoryName(owner);
                      events.add(
                          new SimpleConditionEvent(
                              method,
                              method.getName().equals(expected),
                              method.toGenericString() + " must be named " + expected));
                      String expectedType =
                          genericName(method.getReturnType(), method.getTypeParameters());
                      events.add(
                          new SimpleConditionEvent(
                              method,
                              returnsType(
                                  method.getGenericReturnType(),
                                  method.getReturnType(),
                                  method.getTypeParameters(),
                                  Map.of()),
                              method.toGenericString() + " must return " + expectedType));
                    }
                  }
                }
              });

  private static final ArchRule FLUENT =
      classes()
          .that()
          .arePublic()
          .and()
          .haveSimpleNameEndingWith("Builder")
          .should(
              new ArchCondition<JavaClass>("preserve their concrete fluent builder type") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                  Class<?> builder = item.reflect();
                  List<String> violations = new ArrayList<>();
                  if (Arrays.stream(builder.getDeclaredConstructors())
                      .anyMatch(constructor -> !Modifier.isPrivate(constructor.getModifiers()))) {
                    violations.add("must use private constructors");
                  }
                  if (!Modifier.isAbstract(builder.getModifiers())) {
                    if (!Modifier.isFinal(builder.getModifiers())) {
                      violations.add("concrete builders must be final");
                    }
                    Map<TypeVariable<?>, Type> bindings = inheritedBindings(builder);
                    for (Method method : builder.getMethods()) {
                      if (isObjectMethod(method)
                          || method.isSynthetic()
                          || Modifier.isStatic(method.getModifiers())) {
                        continue;
                      }
                      Class<?> result =
                          method.getName().equals("build") && method.getParameterCount() == 0
                              ? builder.getEnclosingClass()
                              : builder;
                      String expected = genericName(result, builder.getTypeParameters());
                      if (!returnsType(
                          method.getGenericReturnType(),
                          result,
                          builder.getTypeParameters(),
                          bindings)) {
                        violations.add(
                            method.toGenericString()
                                + " must return "
                                + expected
                                + " without redeclaring or changing its type parameters");
                      }
                    }
                  }
                  events.add(
                      new SimpleConditionEvent(
                          item,
                          violations.isEmpty(),
                          item.getName() + " " + String.join("; ", violations)));
                }
              });

  @Test
  void publicBuildersPreserveTheirConcreteFluentType() {
    FLUENT.check(PRODUCTION);
  }

  @Test
  void fluentRuleRejectsBrokenChainsAndPublicConstruction() {
    var invalid =
        new ClassFileImporter()
            .importClasses(
                BrokenFluent.Builder.class,
                BrokenGenerics.Builder.class,
                WrongResult.Builder.class,
                InheritedBrokenBuilder.class,
                OpenBuilder.class);
    AssertionError error = assertThrows(AssertionError.class, () -> FLUENT.check(invalid));
    assertTrue(error.getMessage().contains("value"));
    assertTrue(error.getMessage().contains("rawContext"));
    assertTrue(error.getMessage().contains("wrongContext"));
    assertTrue(error.getMessage().contains("shadowedContext"));
    assertTrue(error.getMessage().contains("inheritedValue"));
    assertTrue(error.getMessage().contains("build"));
    assertTrue(error.getMessage().contains("private constructor"));
    assertTrue(error.getMessage().contains("final"));
  }

  @Test
  void fluentRuleAcceptsGenericAndInheritedConsumerBuildersWithoutBuildMethods() {
    var valid =
        new ClassFileImporter()
            .importClasses(
                ValidFluent.Builder.class,
                InheritedFluentBuilder.class,
                GenericInheritedBuilder.class);
    assertDoesNotThrow(() -> FLUENT.check(valid));
  }

  @Test
  void fluentRuleAllowsStandardObjectOverrides() {
    var valid = new ClassFileImporter().importClasses(DescribedBuilder.class);
    assertDoesNotThrow(() -> FLUENT.check(valid));
  }

  @Test
  void publicBuilderFactoriesAreUnambiguousUnderStaticImports() {
    FACTORIES.check(PRODUCTION);
  }

  @Test
  void factoryRuleRejectsGenericNamesAndAcceptsClassNamedOverloads() {
    var invalid = new ClassFileImporter().importClasses(GenericFactory.class);
    AssertionError error = assertThrows(AssertionError.class, () -> FACTORIES.check(invalid));
    assertTrue(error.getMessage().contains("must be named genericFactory"));
    var valid = new ClassFileImporter().importClasses(NamedFactory.class);
    assertDoesNotThrow(() -> FACTORIES.check(valid));
  }

  @Test
  void factoryRuleRejectsErasedBuilderTypeParameters() {
    var invalid = new ClassFileImporter().importClasses(ErasedFactory.class);
    AssertionError error = assertThrows(AssertionError.class, () -> FACTORIES.check(invalid));
    assertTrue(error.getMessage().contains("must return"));
    assertTrue(error.getMessage().contains("Builder<C>"));
  }

  /** Includes public enclosing names to distinguish nested Error and Data factories. */
  static String factoryName(Class<?> owner) {
    String name = owner.getSimpleName();
    for (Class<?> enclosing = owner.getEnclosingClass();
        enclosing != null && Modifier.isPublic(enclosing.getModifiers());
        enclosing = enclosing.getEnclosingClass()) {
      name = enclosing.getSimpleName() + name;
    }
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }

  private static boolean isBuilder(Class<?> type) {
    return type.getSimpleName().endsWith("Builder");
  }

  /** Resolves inherited self types without erasing the caller's application-context parameters. */
  private static Map<TypeVariable<?>, Type> inheritedBindings(Class<?> builder) {
    Map<TypeVariable<?>, Type> bindings = new HashMap<>();
    for (Class<?> type = builder; type != null; type = type.getSuperclass()) {
      if (type.getGenericSuperclass() instanceof ParameterizedType parent) {
        TypeVariable<?>[] variables = ((Class<?>) parent.getRawType()).getTypeParameters();
        Type[] arguments = parent.getActualTypeArguments();
        for (int index = 0; index < variables.length; index++) {
          bindings.put(variables[index], arguments[index]);
        }
      }
    }
    return bindings;
  }

  private static Type resolveType(Type type, Map<TypeVariable<?>, Type> bindings) {
    if (type instanceof TypeVariable<?> variable && bindings.containsKey(variable)) {
      return resolveType(bindings.get(variable), bindings);
    }
    return type;
  }

  /** TypeVariable equality includes its declaration, unlike its printed name. */
  private static boolean returnsType(
      Type actual,
      Class<?> expected,
      TypeVariable<?>[] parameters,
      Map<TypeVariable<?>, Type> bindings) {
    if (expected == null) {
      return false;
    }
    Type resolved = resolveType(actual, bindings);
    if (expected.getTypeParameters().length == 0) {
      return resolved.equals(expected);
    }
    if (!(resolved instanceof ParameterizedType parameterized)
        || !parameterized.getRawType().equals(expected)) {
      return false;
    }
    Type[] arguments = parameterized.getActualTypeArguments();
    if (arguments.length != parameters.length) {
      return false;
    }
    for (int index = 0; index < arguments.length; index++) {
      if (!resolveType(arguments[index], bindings).equals(parameters[index])) {
        return false;
      }
    }
    return true;
  }

  /** Object overrides are not fluent configuration methods. */
  static boolean isObjectMethod(Method method) {
    try {
      Object.class.getMethod(method.getName(), method.getParameterTypes());
      return true;
    } catch (NoSuchMethodException ignored) {
      return false;
    }
  }

  private static String genericName(Class<?> raw, TypeVariable<?>[] parameters) {
    if (raw == null) {
      return "the enclosing value type";
    }
    return raw.getTypeName()
        + (raw.getTypeParameters().length == 0
            ? ""
            : "<"
                + Arrays.stream(parameters).map(Type::getTypeName).collect(Collectors.joining(", "))
                + ">");
  }

  public static final class GenericFactory {
    public static Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private Builder() {}
    }
  }

  public static final class ErasedFactory {
    public static <C> Builder erasedFactory(Class<C> context) {
      return new Builder<>();
    }

    public static final class Builder<C> {
      private Builder() {}
    }
  }

  public static final class NamedFactory {
    public static Builder namedFactory() {
      return new Builder();
    }

    public static Builder namedFactory(String value) {
      return new Builder();
    }

    public static final class Builder {
      private Builder() {}
    }
  }

  public static final class BrokenFluent {
    public static final class Builder {
      private Builder() {}

      public void value(String value) {}
    }
  }

  public static final class BrokenGenerics {
    public static final class Builder<C> {
      private Builder() {}

      public Builder rawContext(String value) {
        return this;
      }

      public Builder<String> wrongContext(String value) {
        return null;
      }

      public <C> Builder<C> shadowedContext(C value) {
        return new Builder<>();
      }
    }
  }

  public static final class WrongResult {
    public static final class Builder {
      private Builder() {}

      public Object build() {
        return new Object();
      }
    }
  }

  public static class OpenBuilder {
    public OpenBuilder() {}
  }

  public static final class DescribedBuilder {
    private DescribedBuilder() {}

    @Override
    public String toString() {
      return "described builder";
    }

    @Override
    public boolean equals(Object other) {
      return this == other;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(this);
    }
  }

  public static final class ValidFluent<C> {
    public static final class Builder<C> {
      private Builder() {}

      public Builder<C> value(C value) {
        return this;
      }

      public ValidFluent<C> build() {
        return new ValidFluent<>();
      }
    }
  }

  public abstract static class BaseBuilder<B extends BaseBuilder<B>> {
    private BaseBuilder() {}

    @SuppressWarnings("unchecked")
    public B inheritedValue(String value) {
      return (B) this;
    }
  }

  public static final class InheritedFluentBuilder extends BaseBuilder<InheritedFluentBuilder> {
    private InheritedFluentBuilder() {}

    public InheritedFluentBuilder count(int value) {
      return this;
    }
  }

  public static final class InheritedBrokenBuilder extends BaseBuilder<InheritedFluentBuilder> {
    private InheritedBrokenBuilder() {}
  }

  public abstract static class ContextBuilder<C, B extends ContextBuilder<C, B>> {
    private ContextBuilder() {}

    @SuppressWarnings("unchecked")
    public B context(C value) {
      return (B) this;
    }
  }

  public static final class GenericInheritedBuilder<C>
      extends ContextBuilder<C, GenericInheritedBuilder<C>> {
    private GenericInheritedBuilder() {}
  }
}
