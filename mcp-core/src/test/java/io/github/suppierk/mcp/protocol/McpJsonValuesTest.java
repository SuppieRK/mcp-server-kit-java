package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class McpJsonValuesTest {
  @Test
  void distinguishesExplicitNullFromAbsentOptionalMembers() {
    try (var kit = McpServerKit.builder("nulls", "1", McpEmptyContext.class).build()) {
      var explicit =
          McpCallToolResult.mcpCallToolResult()
              .content(List.of())
              .resultType("complete")
              .structuredContent(McpJsonNull.INSTANCE)
              .build();
      var absent = new McpCallToolResult(List.of());
      var explicitResponse =
          assertInstanceOf(
              JsonRpcResultResponse.class,
              kit.decode(kit.encode(new McpCallToolResultResponse(1, explicit))));
      var absentResponse =
          assertInstanceOf(
              JsonRpcResultResponse.class,
              kit.decode(kit.encode(new McpCallToolResultResponse(1, absent))));
      assertTrue(((Map<?, ?>) explicitResponse.result()).containsKey("structuredContent"));
      assertEquals(null, ((Map<?, ?>) explicitResponse.result()).get("structuredContent"));
      assertEquals(false, ((Map<?, ?>) absentResponse.result()).containsKey("structuredContent"));
      var error = new JsonRpcErrorResponse(1, -32602, "invalid", Optional.of(McpJsonNull.INSTANCE));
      assertEquals(error, kit.decode(kit.encode(error)));
      var result = new JsonRpcResultResponse(1, McpJsonNull.INSTANCE);
      assertEquals(result, kit.decode(kit.encode(result)));

      var meta =
          new McpRequestMetaObject(
              McpClientCapabilities.mcpClientCapabilities().build(),
              Optional.empty(),
              Optional.empty(),
              McpProtocol.REVISION,
              Optional.of(McpJsonNull.INSTANCE),
              Map.of());
      var request =
          new McpListToolsRequest(1, new McpPaginatedRequestParams(meta, Optional.empty()));
      assertEquals(request, kit.decode(kit.encode(request)));
      var noToken =
          new McpListToolsRequest(
              1,
              new McpPaginatedRequestParams(
                  McpRequestMetaObject.mcpRequestMetaObject()
                      .clientCapabilities(McpClientCapabilities.mcpClientCapabilities().build())
                      .protocolVersion(McpProtocol.REVISION)
                      .build(),
                  Optional.empty()));
      assertEquals(noToken, kit.decode(kit.encode(noToken)));
    }
  }

  @Test
  void preservesLargeIntegersAndPreciseDecimalsInJdkValues() {
    byte[] wire =
        """
        {"jsonrpc":"2.0","method":"example/notice","params":{
          "integer":123456789012345678901234567890,
          "decimal":0.12345678901234567890123456789,
          "exponent":1e1000
        }}
        """
            .getBytes(StandardCharsets.UTF_8);
    try (var kit = McpServerKit.builder("numbers", "1", McpEmptyContext.class).build()) {
      var notification = assertInstanceOf(JsonRpcNotification.class, kit.decode(wire));
      assertEquals(
          new BigInteger("123456789012345678901234567890"), notification.params().get("integer"));
      assertEquals(
          new BigDecimal("0.12345678901234567890123456789"), notification.params().get("decimal"));
      assertEquals(new BigDecimal("1e1000"), notification.params().get("exponent"));
      assertEquals(notification, kit.decode(kit.encode(notification)));
    }
  }
}
