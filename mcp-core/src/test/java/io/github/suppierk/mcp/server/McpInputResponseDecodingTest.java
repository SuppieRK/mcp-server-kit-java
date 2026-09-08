package io.github.suppierk.mcp.server;

import static io.github.suppierk.mcp.protocol.McpCreateMessageResult.mcpCreateMessageResult;
import static io.github.suppierk.mcp.protocol.McpElicitResult.mcpElicitResult;
import static io.github.suppierk.mcp.protocol.McpListRootsResult.mcpListRootsResult;
import static io.github.suppierk.mcp.server.McpServerKit.mcpServerKit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpInputResponse;
import io.github.suppierk.mcp.protocol.McpRole;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class McpInputResponseDecodingTest {
  @ParameterizedTest
  @MethodSource("inputResponses")
  void decodesClientInputResponsesWithoutATypeTag(String input, McpInputResponse expected) {
    try (var kit = mcpServerKit("decode", "1", McpEmptyContext.class).build()) {
      String wire =
          """
          {"jsonrpc":"2.0","id":1,"method":"tools/call","params":{
            "name":"retry","arguments":{},"inputResponses":{"answer":%s},
            "_meta":{"io.modelcontextprotocol/protocolVersion":"2026-07-28",
                     "io.modelcontextprotocol/clientCapabilities":{}}}}
          """
              .formatted(input);
      var request =
          assertInstanceOf(
              McpCallToolRequest.class, kit.decode(wire.getBytes(StandardCharsets.UTF_8)));
      assertEquals(
          expected, request.params().inputResponses().orElseThrow().values().get("answer"));
    }
  }

  private static Stream<Arguments> inputResponses() {
    return Stream.of(
        Arguments.of(
            """
            {"action":"accept","content":{"name":"Alice"}}
            """,
            mcpElicitResult().action("accept").content(Map.of("name", "Alice")).build()),
        Arguments.of(
            """
            {"role":"assistant","content":{"type":"text","text":"Paris"},
             "model":"test-model","stopReason":"endTurn"}
            """,
            mcpCreateMessageResult()
                .role(McpRole.ASSISTANT)
                .content(Map.of("type", "text", "text", "Paris"))
                .model("test-model")
                .stopReason("endTurn")
                .build()),
        Arguments.of(
            """
            {"roots":[{"uri":"file:///test/root","name":"Test Root"}]}
            """,
            mcpListRootsResult()
                .root(root -> root.uri(URI.create("file:///test/root")).name("Test Root"))
                .build()));
  }
}
