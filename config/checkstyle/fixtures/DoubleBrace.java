package checkstyle.fixtures;

import java.util.ArrayList;
import java.util.List;

class DoubleBrace {
  List<String> values = new ArrayList<>() {{ // expect: AvoidDoubleBraceInitialization
    add("value");
  }};
}
