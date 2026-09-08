package io.github.suppierk.mcp;

import io.github.suppierk.mcp.protocol.McpAudioContent;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpEmbeddedResource;
import io.github.suppierk.mcp.protocol.McpImageContent;
import io.github.suppierk.mcp.protocol.McpImplementation;
import io.github.suppierk.mcp.protocol.McpResourceLink;
import io.github.suppierk.mcp.protocol.McpResultMetaObject;
import io.github.suppierk.mcp.protocol.McpTextContent;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Independent public-API examples; expected values never use a delegation under test. */
final class ToolResultBuilderTest {
  @TestFactory
  Stream<DynamicTest> toolResultsBuildTheirConfiguredChildren() {
    return cases().stream()
        .map(
            example ->
                DynamicTest.dynamicTest(
                    example.parent().getTypeName()
                        + "."
                        + example.property()
                        + " -> "
                        + example.child().getTypeName(),
                    example::verify));
  }

  static List<PrimitiveBuilderCase<?, ?>> cases() {
    return List.of(
        new PrimitiveBuilderCase<>(
            McpCallToolResult.class,
            "content",
            McpTextContent.class,
            "textContent",
            ToolResultBuilderTest::toolResult,
            (McpTextContent.Builder child) -> child.text("configured"),
            List.of(new McpTextContent("existing"), new McpTextContent("configured")),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolResult.class,
            "content",
            McpAudioContent.class,
            "audioContent",
            ToolResultBuilderTest::toolResult,
            (McpAudioContent.Builder child) -> child.data("YQ==").mimeType("audio/wav"),
            List.of(
                new McpTextContent("existing"),
                new McpAudioContent(Optional.empty(), Optional.empty(), "YQ==", "audio/wav")),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolResult.class,
            "content",
            McpImageContent.class,
            "imageContent",
            ToolResultBuilderTest::toolResult,
            (McpImageContent.Builder child) -> child.data("YQ==").mimeType("image/png"),
            List.of(
                new McpTextContent("existing"),
                new McpImageContent(Optional.empty(), Optional.empty(), "YQ==", "image/png")),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolResult.class,
            "content",
            McpEmbeddedResource.class,
            "embeddedResource",
            ToolResultBuilderTest::toolResult,
            (McpEmbeddedResource.Builder child) ->
                child.resource(Map.of("uri", "file:///configured", "text", "configured")),
            List.of(
                new McpTextContent("existing"),
                new McpEmbeddedResource(
                    Optional.empty(),
                    Optional.empty(),
                    Map.of("uri", "file:///configured", "text", "configured"))),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolResult.class,
            "content",
            McpResourceLink.class,
            "resourceLink",
            ToolResultBuilderTest::toolResult,
            (McpResourceLink.Builder child) ->
                child.name("configured").uri(URI.create("file:///configured")),
            List.of(
                new McpTextContent("existing"),
                new McpResourceLink(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    "configured",
                    Optional.empty(),
                    Optional.empty(),
                    URI.create("file:///configured"))),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            ToolResultBuilderTest::toolResult,
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(
                    new McpImplementation(
                        Optional.empty(),
                        Optional.empty(),
                        "configured",
                        Optional.empty(),
                        "1",
                        Optional.empty())),
            Optional.of(
                new McpResultMetaObject(
                    Optional.of(
                        new McpImplementation(
                            Optional.empty(),
                            Optional.empty(),
                            "configured",
                            Optional.empty(),
                            "1",
                            Optional.empty())))),
            List.of()));
  }

  private static McpCallToolResult.Builder toolResult() {
    return McpCallToolResult.mcpCallToolResult()
        .content(List.of(new McpTextContent("existing")))
        .resultType("complete")
        .isError(false)
        .structuredContent(Map.of("preserved", true));
  }
}
