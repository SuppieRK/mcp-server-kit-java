package checkstyle.fixtures;

class SwitchFallThrough {
  int run(int value) {
    switch (value) {
      case 1:
        value++;
      case 2: // expect: FallThrough
        return value;
      default:
        return 0;
    }
  }
}
