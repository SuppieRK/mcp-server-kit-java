package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class McpAnnotationsTest {
  @Test
  void validatesPriority() {
    for (double priority : new double[] {0D, 0.25D, 0.5D, 1D}) {
      McpAnnotations annotations =
          new McpAnnotations(Optional.empty(), Optional.empty(), Optional.of(priority));

      assertEquals(priority, annotations.priority().orElseThrow());
    }

    for (double priority :
        new double[] {
          -Double.MIN_VALUE,
          Math.nextUp(1D),
          Double.NaN,
          Double.NEGATIVE_INFINITY,
          Double.POSITIVE_INFINITY
        }) {
      assertThrows(
          IllegalArgumentException.class,
          () -> new McpAnnotations(Optional.empty(), Optional.empty(), Optional.of(priority)));
    }
  }
}
