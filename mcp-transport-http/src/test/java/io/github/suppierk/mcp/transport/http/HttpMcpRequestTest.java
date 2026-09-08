package io.github.suppierk.mcp.transport.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

class HttpMcpRequestTest {

  @Test
  void preservesImmutableHeaderCardinalityAndMergesNamesWithoutCaseSensitivity() {
    var supplied = new LinkedHashMap<String, List<String>>();
    supplied.put("X-Trace", List.of("first", "second"));
    supplied.put("x-trace", List.of("third"));
    supplied.put("Unrelated", List.of("one", "two"));
    byte[] body = "body".getBytes(StandardCharsets.UTF_8);

    var request = new HttpMcpRequest("POST", supplied, body);
    body[0] = 'B';

    assertEquals(List.of("first", "second", "third"), request.headers().get("X-TRACE"));
    assertEquals(List.of("one", "two"), request.headers().get("unrelated"));
    assertArrayEquals("body".getBytes(StandardCharsets.UTF_8), request.body());
    var traceValues = request.headers().get("x-trace");
    var headers = request.headers();
    var additionalValues = List.of("value");
    assertThrows(UnsupportedOperationException.class, () -> traceValues.add("fourth"));
    assertThrows(
        UnsupportedOperationException.class, () -> headers.put("Another", additionalValues));
  }

  @Test
  void exposesOnlyMethodHeadersAndBody() {
    assertEquals(
        List.of("method", "headers", "body"),
        Arrays.stream(HttpMcpRequest.class.getRecordComponents())
            .map(RecordComponent::getName)
            .toList());
    assertEquals(
        "java.util.Map<java.lang.String, java.util.List<java.lang.String>>",
        HttpMcpRequest.class.getRecordComponents()[1].getGenericType().getTypeName());
  }
}
