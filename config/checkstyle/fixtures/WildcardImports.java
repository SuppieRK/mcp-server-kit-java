package checkstyle.fixtures;

import java.util.*; // expect: AvoidStarImport
import static java.util.Collections.*; // expect: AvoidStarImport

class WildcardImports {
  Collection<?> values = emptyList();
}
