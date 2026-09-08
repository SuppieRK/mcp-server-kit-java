package checkstyle.fixtures;

import static checkstyle.fixtures.StaticImportedBuilder.Model.model;

class StaticImportedBuilder {
  Model value() {
    var builder = model().value("static-imported fluent factory");
    return builder.build();
  }

  record Model(String value) {
    static Builder model() {
      return new Builder();
    }

    static final class Builder {
      private String value;

      Builder value(String value) {
        this.value = value;
        return this;
      }

      Model build() {
        return new Model(value);
      }
    }
  }
}
