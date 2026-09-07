package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class McpRequestMethodTest {
  @Test
  void clientToServerRequestsPublishTheirWireMethods() {
    assertEquals(
        List.of(
            "server/discover",
            "tools/list",
            "tools/call",
            "resources/list",
            "resources/templates/list",
            "resources/read",
            "subscriptions/listen",
            "prompts/list",
            "prompts/get",
            "completion/complete"),
        List.of(
            McpDiscoverRequest.METHOD,
            McpListToolsRequest.METHOD,
            McpCallToolRequest.METHOD,
            McpListResourcesRequest.METHOD,
            McpListResourceTemplatesRequest.METHOD,
            McpReadResourceRequest.METHOD,
            McpSubscriptionsListenRequest.METHOD,
            McpListPromptsRequest.METHOD,
            McpGetPromptRequest.METHOD,
            McpCompleteRequest.METHOD));
  }
}
