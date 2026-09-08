package checkstyle.fixtures;

import java.util.function.Supplier;

// These accepted cases document upstream limitations, not the recommended source style.
class KnownQualifiedNameGaps {
  java.util.Map.Entry<String, String> entry;
  Class<?> type = java.util.List.class;
  Object values = java.util.List.of("value");
  Object empty = java.util.Collections.EMPTY_LIST;
  Supplier<?> constructor = java.util.ArrayList::new;
  Supplier<?> method = java.util.Collections::emptyList;

  @java.lang.Override
  public String toString() {
    return "known Checkstyle limitations";
  }

  sealed interface Part permits checkstyle.fixtures.KnownQualifiedNameGaps.Child {}

  record Child() implements Part {}
}
