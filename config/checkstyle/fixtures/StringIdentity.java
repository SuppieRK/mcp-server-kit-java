package checkstyle.fixtures;

class StringIdentity {
  boolean same(String value) {
    return value == "literal"; // expect: StringLiteralEquality
  }
}
