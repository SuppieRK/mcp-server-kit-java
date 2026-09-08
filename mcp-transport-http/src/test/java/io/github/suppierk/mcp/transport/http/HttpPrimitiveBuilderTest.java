package io.github.suppierk.mcp.transport.http;

import static io.github.suppierk.mcp.transport.http.HttpAcceptedResponse.httpAcceptedResponse;
import static io.github.suppierk.mcp.transport.http.HttpJsonResponse.httpJsonResponse;
import static io.github.suppierk.mcp.transport.http.HttpMcpRequest.httpMcpRequest;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HttpPrimitiveBuilderTest {
  // equals itself is under test, including null and unrelated types, not JUnit's equality helper.
  @SuppressWarnings("java:S5785")
  @Test
  void jsonResponseCopiesHaveValueSemantics() {
    var response = httpJsonResponse().body(new byte[] {3, 4}).build();
    var copy = new HttpJsonResponse(response.status(), response.headers(), response.body());

    assertEquals(response, copy);
    assertEquals(response.hashCode(), copy.hashCode());
    assertEquals(response.toString(), copy.toString());
    assertTrue(new HashSet<>(Set.of(response)).contains(copy));
    assertNotEquals(response, httpJsonResponse().status(201).body(new byte[] {3, 4}).build());
    assertNotEquals(
        response,
        httpJsonResponse()
            .headers(Map.of("Content-Type", "application/json"))
            .body(new byte[] {3, 4})
            .build());
    assertNotEquals(response, httpJsonResponse().body(new byte[] {3, 5}).build());
    assertFalse(response.equals(null));
    assertFalse(response.equals("response"));
  }

  // Keep direct equals calls so null and unrelated-type checks exercise the request contract.
  @SuppressWarnings("java:S5785")
  @Test
  void requestCopiesHaveValueSemantics() {
    var request =
        httpMcpRequest()
            .method("POST")
            .headers(Map.of("Content-Type", List.of("application/json")))
            .body(new byte[] {1, 2})
            .build();
    var copy = new HttpMcpRequest(request.method(), request.headers(), request.body());

    assertEquals(request, copy);
    assertEquals(request.hashCode(), copy.hashCode());
    assertEquals(request.toString(), copy.toString());
    assertTrue(new HashSet<>(Set.of(request)).contains(copy));
    assertNotEquals(request, new HttpMcpRequest("GET", request.headers(), request.body()));
    assertNotEquals(request, new HttpMcpRequest("POST", Map.of(), request.body()));
    assertNotEquals(request, new HttpMcpRequest("POST", request.headers(), new byte[] {1, 3}));
    assertFalse(request.equals(null));
    assertFalse(request.equals("request"));
  }

  @Test
  void requestBuilderPreservesNormalizationAndDefensiveCopies() {
    var values = new ArrayList<>(List.of("application/json"));
    var headers = new HashMap<String, List<String>>();
    headers.put("Content-Type", values);
    byte[] body = {1, 2};
    var builder = httpMcpRequest().method("POST").headers(headers).body(body);
    var request = builder.build();
    values.add("changed");
    headers.clear();
    body[0] = 9;
    request.body()[0] = 8;

    assertEquals("POST", request.method());
    assertEquals("application/json", request.header("content-type"));
    assertArrayEquals(new byte[] {1, 2}, request.body());
    assertEquals("GET", builder.method("GET").build().method());
    assertEquals("POST", request.method());
  }

  @Test
  void responseBuilderPreservesHeadersStatusAndBodySnapshots() {
    var headers = new HashMap<>(Map.of("Content-Type", "application/json"));
    byte[] body = {3, 4};
    var builder = httpJsonResponse().status(201).headers(headers).body(body);
    var response = builder.build();
    headers.clear();
    body[0] = 9;
    response.body()[0] = 8;

    assertEquals(201, response.status());
    assertEquals(Map.of("Content-Type", "application/json"), response.headers());
    assertArrayEquals(new byte[] {3, 4}, response.body());
    assertEquals(202, builder.status(202).build().status());
    assertEquals(201, response.status());
  }

  @Test
  void acceptedResponseBuilderRetainsTheFixedNotificationResponse() {
    var response = httpAcceptedResponse().build();
    assertEquals(202, response.status());
    assertEquals(Map.of(), response.headers());
  }

  @Test
  void buildersKeepTheExistingRequiredFieldValidation() {
    assertThrows(NullPointerException.class, () -> httpMcpRequest().build());
    assertThrows(
        NullPointerException.class, () -> httpMcpRequest().method("POST").body(null).build());
    assertThrows(
        NullPointerException.class, () -> httpMcpRequest().method("POST").headers(null).build());
    assertThrows(NullPointerException.class, () -> httpJsonResponse().body(null).build());
    assertThrows(NullPointerException.class, () -> httpJsonResponse().headers(null).build());
  }
}
