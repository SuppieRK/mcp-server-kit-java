package checkstyle.fixtures;

class QualifiedType {
  java.util.List<String> values; // expect: UnnecessaryFullyQualifiedType
  Object copy = new java.util.ArrayList<>(); // expect: UnnecessaryFullyQualifiedType
  java.lang.String text; // expect: UnnecessaryFullyQualifiedType
}
