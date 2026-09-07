package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

class McpMetaObjectTest {
  @Test
  void copiesMutableJsonValues() {
    ObjectNode values = JsonNodeFactory.instance.objectNode().put("owner", "first");
    McpMetaObject metadata = new McpMetaObject(values);
    values.put("owner", "changed");
    ObjectNode returned = metadata.values();
    returned.put("owner", "changed again");

    assertEquals("first", metadata.values().path("owner").textValue());
    assertNotSame(returned, metadata.values());
  }
}
