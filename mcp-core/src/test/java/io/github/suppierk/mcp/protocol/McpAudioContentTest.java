package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.github.suppierk.mcp.JsonTestValues;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class McpAudioContentTest {
  @Test
  void builderUsesTheStandardOptionalBehavior() {
    McpMetaObject meta =
        new McpMetaObject(JsonTestValues.object(JsonNodeFactory.instance.objectNode()));
    McpAnnotations annotations =
        new McpAnnotations(Optional.empty(), Optional.empty(), Optional.empty());

    McpAudioContent content =
        McpAudioContent.mcpAudioContent()
            .meta(Optional.of(meta))
            .meta(meta)
            .annotations(Optional.of(annotations))
            .annotations(annotations)
            .data("YXVkaW8=")
            .mimeType("audio/mpeg")
            .build();

    assertEquals(Optional.of(meta), content.meta());
    assertEquals(Optional.of(annotations), content.annotations());
    assertTrue(
        McpAudioContent.mcpAudioContent()
            .meta((McpMetaObject) null)
            .annotations((McpAnnotations) null)
            .data("YXVkaW8=")
            .mimeType("audio/mpeg")
            .build()
            .meta()
            .isEmpty());
    assertThrows(
        NullPointerException.class,
        () ->
            McpAudioContent.mcpAudioContent()
                .meta((Optional<McpMetaObject>) null)
                .data("YXVkaW8=")
                .mimeType("audio/mpeg")
                .build());
  }
}
