package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonRpcNotificationTest {
  @Test
  void ownsImmutableJdkParametersAndRoundTripsExplicitNull() {
    var entries = new ArrayList<Object>();
    entries.add("original");
    entries.add(null);
    var supplied = new LinkedHashMap<String, Object>();
    supplied.put("entries", entries);
    var notification =
        JsonRpcNotification.jsonRpcNotification().method("example/notice").params(supplied).build();

    entries.set(0, "changed");
    supplied.clear();

    assertEquals("original", ((List<?>) notification.params().get("entries")).get(0));
    assertThrows(UnsupportedOperationException.class, () -> notification.params().clear());
    assertThrows(
        UnsupportedOperationException.class,
        () -> ((List<?>) notification.params().get("entries")).clear());
    try (var kit = McpServerKit.builder("notification", "1", McpEmptyContext.class).build()) {
      assertEquals(notification, kit.decode(kit.encode(notification)));
    }
  }
}
