package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class McpCacheableResultTest {
  @Test
  void validatesProtocolConstraints() {
    for (String cacheScope :
        List.of(McpCacheableResult.CACHE_SCOPE_PRIVATE, McpCacheableResult.CACHE_SCOPE_PUBLIC)) {
      McpCacheableResult result =
          new McpCacheableResult(Optional.empty(), cacheScope, "complete", 0L);

      assertEquals(cacheScope, result.cacheScope());
    }

    for (String cacheScope : List.of("", "PRIVATE", "shared")) {
      assertThrows(
          IllegalArgumentException.class,
          () -> new McpCacheableResult(Optional.empty(), cacheScope, "complete", 0L));
    }
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new McpCacheableResult(
                Optional.empty(), McpCacheableResult.CACHE_SCOPE_PRIVATE, "complete", -1L));
  }
}
