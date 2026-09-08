package checkstyle.fixtures;

import static java.util.List.of;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

class Valid {
  Date sqlDate;
  java.util.Date utilDate; // Required: Date refers to a different imported type.
  Map.Entry<String, String> entry;
  String className = "java.util.List";
  String example = """
      java.util.List<String> is text, not a qualified Java type here.
      """;

  /** Documents a type used only in Javadoc: {@link Instant}. */
  List<String> values() {
    var values = of("first", "second");
    return values;
  }

  boolean same(String value) {
    return "literal".equals(value);
  }

  int select(int value) {
    return switch (value) {
      case 1 -> 2;
      default -> 3;
    };
  }

  void explainedCatch() {
    try {
      throw new IllegalStateException("expected fixture failure");
    } catch (IllegalStateException exception) {
      // This fixture deliberately throws to demonstrate a documented empty catch.
    }
  }

  record ExplicitEquality(int value) {
    @Override
    public boolean equals(Object other) {
      return other instanceof ExplicitEquality record && record.value == value;
    }

    @Override
    public int hashCode() {
      return Integer.hashCode(value);
    }
  }

  record Value(String text) {
    Value {
      text = Objects.requireNonNull(text);
    }
  }

  sealed interface Part permits ValuePart {}

  record ValuePart(Value value) implements Part {}

  static final class Builder<C> {
    Builder<C> child(Consumer<Builder<C>> configure) {
      configure.accept(this);
      return this;
    }
  }
}
