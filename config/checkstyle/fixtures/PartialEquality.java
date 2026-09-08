package checkstyle.fixtures;

class PartialEquality {
  @Override public boolean equals(Object other) { // expect: EqualsHashCode
    return this == other;
  }

  record PartialRecord(int value) {
    @Override public int hashCode() { // expect: EqualsHashCode
      return value;
    }
  }
}
