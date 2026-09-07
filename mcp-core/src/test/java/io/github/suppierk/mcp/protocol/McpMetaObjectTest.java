package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class McpMetaObjectTest {
  @Test
  void copiesMutableJsonValues() {
    var values = new LinkedHashMap<String, Object>();
    values.put("owner", "first");
    McpMetaObject metadata = new McpMetaObject(values);
    values.put("owner", "changed");
    assertThrows(UnsupportedOperationException.class, () -> metadata.values().clear());
    assertEquals("first", metadata.values().get("owner"));
  }
}
