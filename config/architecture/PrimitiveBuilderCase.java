package io.github.suppierk.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** An independent example of one public parent-property/child construction capability. */
record PrimitiveBuilderCase<B, C>(
    Class<?> parent,
    String property,
    Class<?> child,
    String methodName,
    Supplier<B> freshBuilder,
    Consumer<C> configureChild,
    Object expectedProperty,
    List<?> arguments) {

  void verify() throws ReflectiveOperationException {
    Method delegate = method();
    Object builder = freshBuilder.get();
    Object before = builder.getClass().getMethod("build").invoke(builder);
    Class<?> childBuilder = PrimitiveBuilderArchitectureTest.builderOf(child).orElseThrow();
    List<Object> configured = new ArrayList<>();
    Consumer<Object> callback =
        value -> {
          assertTrue(childBuilder.isInstance(value), "callback must receive the child's builder");
          configured.add(value);
          configure(value);
        };
    Object[] supplied = new Object[delegate.getParameterCount()];
    int argument = 0;
    int callbacks = 0;
    for (int index = 0; index < supplied.length; index++) {
      if (delegate.getParameterTypes()[index] == Consumer.class) {
        supplied[index] = callback;
        callbacks++;
      } else {
        supplied[index] = arguments.get(argument++);
      }
    }
    assertEquals(1, callbacks, "case must identify one child callback");
    Object resultBuilder = delegate.invoke(builder, supplied);
    Object result = resultBuilder.getClass().getMethod("build").invoke(resultBuilder);
    assertEquals(parent, result.getClass());
    assertEquals(1, configured.size(), "child configuration must execute exactly once");
    var properties = PrimitiveBuilderArchitectureTest.properties(parent);
    var target =
        properties.stream()
            .filter(candidate -> candidate.name().equals(property))
            .findFirst()
            .orElseThrow();
    Object actual = target.read(result);
    assertTrue(
        Objects.deepEquals(expectedProperty, actual),
        () ->
            property
                + " must contain the configured child; expected "
                + expectedProperty
                + "; got "
                + actual);
    for (var sibling : properties) {
      if (!sibling.name().equals(property)) {
        assertTrue(
            Objects.deepEquals(sibling.read(before), sibling.read(result)),
            () -> "sibling property " + sibling.name() + " must remain unchanged");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void configure(Object builder) {
    configureChild.accept((C) builder);
  }

  Method method() {
    Class<?> builder = PrimitiveBuilderArchitectureTest.builderOf(parent).orElseThrow();
    Class<?> childBuilder = PrimitiveBuilderArchitectureTest.builderOf(child).orElseThrow();
    List<Method> matches =
        Arrays.stream(builder.getMethods())
            .filter(candidate -> candidate.getName().equals(methodName))
            .filter(candidate -> candidate.getParameterCount() == arguments.size() + 1)
            .filter(
                candidate ->
                    PrimitiveBuilderArchitectureTest.delegatesTo(candidate, builder, childBuilder))
            .toList();
    if (matches.size() != 1) {
      throw new AssertionError(
          "Expected one public child-builder method: " + parent.getName() + "." + methodName);
    }
    return matches.get(0);
  }
}
