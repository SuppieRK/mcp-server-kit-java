package io.github.suppierk.mcp.conformance;

import static io.github.suppierk.mcp.protocol.McpCallToolResult.mcpCallToolResult;
import static io.github.suppierk.mcp.protocol.McpCompleteResult.mcpCompleteResult;
import static io.github.suppierk.mcp.protocol.McpGetPromptResult.mcpGetPromptResult;
import static io.github.suppierk.mcp.protocol.McpPrompt.mcpPrompt;
import static io.github.suppierk.mcp.protocol.McpReadResourceResult.mcpReadResourceResult;
import static io.github.suppierk.mcp.protocol.McpResource.mcpResource;
import static io.github.suppierk.mcp.protocol.McpResourceTemplate.mcpResourceTemplate;

import io.github.suppierk.mcp.protocol.McpRole;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** Application fixtures named by the pinned upstream server scenarios. */
final class ConformanceFixtures {
  private static final String PNG =
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aL1sAAAAASUVORK5CYII=";
  private static final String WAV =
      "UklGRiYAAABXQVZFZm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQIAAAAAAA==";

  private ConformanceFixtures() {}

  static McpServerKit.Builder<McpEmptyContext> register(
      McpServerKit.Builder<McpEmptyContext> kit, Executor executor) {
    return kit.syncTool(
            tool ->
                tool.name("test_simple_text")
                    .description("Returns text")
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .textContent(text -> text.text("Hello, World!"))
                                .build()))
        .syncTool(
            tool ->
                tool.name("test_image_content")
                    .description("Returns a PNG image")
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .imageContent(image -> image.data(PNG).mimeType("image/png"))
                                .build()))
        .syncTool(
            tool ->
                tool.name("test_audio_content")
                    .description("Returns WAV audio")
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .audioContent(audio -> audio.data(WAV).mimeType("audio/wav"))
                                .build()))
        .syncTool(
            tool ->
                tool.name("test_embedded_resource")
                    .description("Returns an embedded resource")
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .embeddedResource(
                                    resource ->
                                        resource.resource(
                                            Map.of(
                                                "uri",
                                                "test://embedded",
                                                "mimeType",
                                                "text/plain",
                                                "text",
                                                "Embedded text")))
                                .build()))
        .syncTool(
            tool ->
                tool.name("test_multiple_content_types")
                    .description("Returns mixed content")
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .textContent(text -> text.text("Mixed content"))
                                .imageContent(image -> image.data(PNG).mimeType("image/png"))
                                .embeddedResource(
                                    resource ->
                                        resource.resource(
                                            Map.of(
                                                "uri",
                                                "test://embedded",
                                                "mimeType",
                                                "text/plain",
                                                "text",
                                                "Embedded text")))
                                .build()))
        .syncTool(
            tool ->
                tool.name("test_error_handling")
                    .description("Returns a tool execution error")
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .isError(true)
                                .textContent(text -> text.text("Deliberate tool execution failure"))
                                .build()))
        .asyncTool(
            tool ->
                tool.name("test_tool_with_progress")
                    .description("Reports three progress updates")
                    .handler(
                        (context, request, output) ->
                            CompletableFuture.supplyAsync(
                                () -> {
                                  try {
                                    for (int progress = 0; progress <= 100; progress += 50) {
                                      // Model work between updates so the HTTP consumer can request
                                      // more.
                                      TimeUnit.MILLISECONDS.sleep(100);
                                      output.progress(progress, 100);
                                    }
                                  } catch (InterruptedException exception) {
                                    Thread.currentThread().interrupt();
                                    throw new CompletionException(exception);
                                  }
                                  return mcpCallToolResult()
                                      .resultType("complete")
                                      .textContent(text -> text.text("Done"))
                                      .build();
                                },
                                executor)))
        .syncResource(
            mcpResource()
                .name("static-text")
                .description("Static text")
                .uri(URI.create("test://static-text"))
                .mimeType("text/plain")
                .build(),
            (context, request, output) ->
                mcpReadResourceResult()
                    .resultType("complete")
                    .cacheScope("private")
                    .ttlMs(0L)
                    .textResourceContents(
                        text -> text.uri(request.uri()).mimeType("text/plain").text("Static text"))
                    .build())
        .syncResource(
            mcpResource()
                .name("static-binary")
                .description("Static binary data")
                .uri(URI.create("test://static-binary"))
                .mimeType("image/png")
                .build(),
            (context, request, output) ->
                mcpReadResourceResult()
                    .resultType("complete")
                    .cacheScope("private")
                    .ttlMs(0L)
                    .blobResourceContents(
                        blob -> blob.uri(request.uri()).mimeType("image/png").blob(PNG))
                    .build())
        .syncResourceTemplate(
            mcpResourceTemplate()
                .name("template")
                .description("Reflects the requested ID")
                .uriTemplate("test://template/{id}/data")
                .mimeType("text/plain")
                .build(),
            Pattern.compile("test://template/[^/]+/data"),
            (context, request, output) ->
                mcpReadResourceResult()
                    .resultType("complete")
                    .cacheScope("private")
                    .ttlMs(0L)
                    .textResourceContents(
                        text ->
                            text.uri(request.uri())
                                .mimeType("text/plain")
                                .text("Template data for " + request.uri()))
                    .build())
        .syncPrompt(
            mcpPrompt().name("test_simple_prompt").description("A simple prompt").build(),
            (context, request, output) ->
                mcpGetPromptResult()
                    .resultType("complete")
                    .promptMessage(
                        message ->
                            message
                                .role(McpRole.USER)
                                .textContent(text -> text.text("Hello, World!")))
                    .build())
        .syncPrompt(
            mcpPrompt()
                .name("test_prompt_with_arguments")
                .description("Uses both arguments")
                .promptArgument(
                    arg -> arg.name("arg1").description("First argument").required(true))
                .promptArgument(
                    arg -> arg.name("arg2").description("Second argument").required(true))
                .build(),
            (context, request, output) ->
                mcpGetPromptResult()
                    .resultType("complete")
                    .promptMessage(
                        message ->
                            message
                                .role(McpRole.USER)
                                .textContent(
                                    text ->
                                        text.text(
                                            request.arguments().orElseThrow().get("arg1")
                                                + " "
                                                + request.arguments().orElseThrow().get("arg2"))))
                    .build())
        .syncPrompt(
            mcpPrompt()
                .name("test_prompt_with_embedded_resource")
                .description("Embeds the requested resource")
                .promptArgument(arg -> arg.name("resourceUri").required(true))
                .build(),
            (context, request, output) ->
                mcpGetPromptResult()
                    .resultType("complete")
                    .promptMessage(
                        message ->
                            message
                                .role(McpRole.USER)
                                .embeddedResource(
                                    resource ->
                                        resource.resource(
                                            Map.of(
                                                "uri",
                                                request
                                                    .arguments()
                                                    .orElseThrow()
                                                    .get("resourceUri"),
                                                "mimeType",
                                                "text/plain",
                                                "text",
                                                "Embedded prompt data"))))
                    .build())
        .syncPrompt(
            mcpPrompt().name("test_prompt_with_image").description("An image prompt").build(),
            (context, request, output) ->
                mcpGetPromptResult()
                    .resultType("complete")
                    .promptMessage(
                        message ->
                            message
                                .role(McpRole.USER)
                                .imageContent(image -> image.data(PNG).mimeType("image/png")))
                    .build())
        .syncTool(
            tool ->
                tool.name("json_schema_2020_12_tool")
                    .description("Preserves JSON Schema 2020-12 keywords")
                    // The schema shape is specified by the pinned upstream json-schema-2020-12
                    // scenario.
                    .inputSchema(
                        Map.of(
                            "$schema",
                            "https://json-schema.org/draft/2020-12/schema",
                            "type",
                            "object",
                            "$defs",
                            Map.of(
                                "address",
                                Map.of(
                                    "$anchor",
                                    "addressDef",
                                    "type",
                                    "object",
                                    "properties",
                                    Map.of(
                                        "street",
                                        Map.of("type", "string"),
                                        "city",
                                        Map.of("type", "string")))),
                            "properties",
                            Map.of(
                                "name",
                                Map.of("type", "string"),
                                "address",
                                Map.of("$ref", "#/$defs/address"),
                                "contactMethod",
                                Map.of("type", "string", "enum", List.of("phone", "email")),
                                "phone",
                                Map.of("type", "string"),
                                "email",
                                Map.of("type", "string")),
                            "allOf",
                            List.of(
                                Map.of(
                                    "anyOf",
                                    List.of(
                                        Map.of("required", List.of("phone")),
                                        Map.of("required", List.of("email"))))),
                            "if",
                            Map.of(
                                "properties",
                                Map.of("contactMethod", Map.of("const", "phone")),
                                "required",
                                List.of("contactMethod")),
                            "then",
                            Map.of("required", List.of("phone")),
                            "else",
                            Map.of("required", List.of("email")),
                            "additionalProperties",
                            false))
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .textContent(text -> text.text("Schema validated"))
                                .build()))
        .syncTool(
            tool ->
                tool.name("test_header_mirror")
                    .description("Mirrors a string argument into a header")
                    .inputSchema(
                        Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of("value", Map.of("type", "string", "x-mcp-header", "Value")),
                            "additionalProperties",
                            false))
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .textContent(
                                    text ->
                                        text.text(
                                            String.valueOf(
                                                request.arguments().orElse(Map.of()).get("value"))))
                                .build()))
        .syncCompletion(
            (context, request, output) ->
                mcpCompleteResult()
                    .resultType("complete")
                    .completion(Map.of("values", List.of("first", "second")))
                    .build());
  }
}
