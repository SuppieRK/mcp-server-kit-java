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
            McpDiscoverRequest.METHOD_NAME,
            McpListToolsRequest.METHOD_NAME,
            McpCallToolRequest.METHOD_NAME,
            McpListResourcesRequest.METHOD_NAME,
            McpListResourceTemplatesRequest.METHOD_NAME,
            McpReadResourceRequest.METHOD_NAME,
            McpSubscriptionsListenRequest.METHOD_NAME,
            McpListPromptsRequest.METHOD_NAME,
            McpGetPromptRequest.METHOD_NAME,
            McpCompleteRequest.METHOD_NAME));
  }
}
