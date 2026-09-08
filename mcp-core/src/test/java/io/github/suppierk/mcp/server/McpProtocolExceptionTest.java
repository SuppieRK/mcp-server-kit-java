package io.github.suppierk.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.mcp.JsonTestValues;
import io.github.suppierk.mcp.protocol.McpClientCapabilities;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class McpProtocolExceptionTest {
  @Test
  void invalidParametersOwnsItsImmutableProtocolFailure() throws Exception {
    var suppliedData = new LinkedHashMap<String, Object>();
    suppliedData.put("field", "supplied");
    var exception =
        new McpInvalidParamsException("Invalid application parameters", Optional.of(suppliedData));
    suppliedData.put("field", "changed");

    assertTrue(exception instanceof RuntimeException);
    assertEquals(-32602, exception.code());
    assertEquals("Invalid application parameters", exception.getMessage());
    assertEquals(
        "supplied", JsonTestValues.json(exception.data().orElseThrow()).path("field").textValue());
    var errorData = (Map<?, ?>) exception.data().orElseThrow();
    assertThrows(UnsupportedOperationException.class, errorData::clear);
    assertEquals(
        "supplied", JsonTestValues.json(exception.data().orElseThrow()).path("field").textValue());
    assertTrue(Modifier.isFinal(McpProtocolException.class.getMethod("code").getModifiers()));
  }

  @Test
  void everyConcreteProtocolExceptionOwnsItsStandardCode() {
    var noData = Optional.<Object>empty();
    var capabilities =
        new McpClientCapabilities(
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()));

    assertEquals(
        List.of(-32700, -32600, -32601, -32602, -32603, -32020, -32021, -32022),
        List.of(
                new McpParseException("parse", noData),
                new McpInvalidRequestException("request", noData),
                new McpMethodNotFoundException("method", noData),
                new McpInvalidParamsException("parameters", noData),
                new McpInternalException("internal", noData),
                new McpHeaderMismatchException("header", noData),
                new McpMissingRequiredClientCapabilityException("capability", capabilities),
                new McpUnsupportedProtocolVersionException(
                    "version", "requested", List.of("supported")))
            .stream()
            .map(McpProtocolException::code)
            .toList());
    assertEquals(
        Set.of(
            McpParseException.class,
            McpInvalidRequestException.class,
            McpMethodNotFoundException.class,
            McpInvalidParamsException.class,
            McpInternalException.class,
            McpHeaderMismatchException.class,
            McpMissingRequiredClientCapabilityException.class,
            McpUnsupportedProtocolVersionException.class),
        Arrays.stream(McpProtocolException.class.getPermittedSubclasses())
            .collect(Collectors.toSet()));
    assertTrue(
        Arrays.stream(McpProtocolException.class.getPermittedSubclasses())
            .flatMap(type -> Arrays.stream(type.getConstructors()))
            .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
            .noneMatch(type -> type.equals(Integer.TYPE) || type.equals(Integer.class)));
  }

  @Test
  void specializedProtocolExceptionsPreserveTypedData() {
    var capabilities =
        new McpClientCapabilities(
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()),
            JsonTestValues.optionalObject(Optional.empty()));
    var missing = new McpMissingRequiredClientCapabilityException("Missing roots", capabilities);
    var supported = new ArrayList<>(List.of("2026-07-28"));
    var unsupported =
        new McpUnsupportedProtocolVersionException("Unsupported", "1900-01-01", supported);
    supported.add("changed");

    assertEquals(capabilities, missing.requiredCapabilities());
    assertEquals("1900-01-01", unsupported.requestedRevision());
    assertEquals(List.of("2026-07-28"), unsupported.supportedRevisions());
  }
}
