package checkstyle.fixtures;

class EmptyCatch {
  void run() {
    try {
      throw new IllegalStateException();
    // expect-next: EmptyCatchBlock
    } catch (IllegalStateException ignored) {
    }
  }
}
