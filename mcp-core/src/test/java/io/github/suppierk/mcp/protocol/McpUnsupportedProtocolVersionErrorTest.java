package io.github.suppierk.mcp.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.github.suppierk.mcp.JsonTestValues;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.server.McpUnsupportedProtocolVersionException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class McpUnsupportedProtocolVersionErrorTest {
  @Test
  void encodesAsAJsonRpcErrorResponse() {
    McpUnsupportedProtocolVersionError response =
        new McpUnsupportedProtocolVersionError(
            new McpUnsupportedProtocolVersionError.Error(
                "Unsupported protocol version",
                new McpUnsupportedProtocolVersionError.Data(
                    "1900-01-01", List.of(McpProtocol.REVISION))),
            JsonTestValues.optionalValue(Optional.of(JsonNodeFactory.instance.numberNode(1))));
    McpServerKit<McpEmptyContext> server =
        McpServerKit.builder("model", "1", McpEmptyContext.class).build();
    JsonRpcErrorResponse decoded = (JsonRpcErrorResponse) server.decode(server.encode(response));

    assertEquals(McpUnsupportedProtocolVersionException.CODE, decoded.code());
    assertEquals(
        "1900-01-01",
        JsonTestValues.json(decoded.data().orElseThrow()).path("requested").textValue());
    assertFalse(JsonTestValues.json(decoded.id()).isNull());
  }
}
