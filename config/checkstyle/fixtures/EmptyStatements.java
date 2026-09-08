package checkstyle.fixtures;

class EmptyStatements {
  void run(boolean ready) {
    if (ready); // expect: EmptyStatement
  }
}
