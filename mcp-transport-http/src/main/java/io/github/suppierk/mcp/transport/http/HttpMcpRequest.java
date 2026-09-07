package io.github.suppierk.mcp.transport.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Supplies one framework-neutral HTTP request.
 *
 * @param method the HTTP method
 * @param headers the header field values
 * @param body the request body
 */
public record HttpMcpRequest(String method, Map<String, List<String>> headers, byte[] body) {

  /** Validates and copies the request. */
  public HttpMcpRequest {
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(headers, "headers");
    body = Objects.requireNonNull(body, "body").clone();
    TreeMap<String, List<String>> normalized = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    headers.forEach(
        (name, values) -> {
          Objects.requireNonNull(name, "header name");
          List<String> copied = List.copyOf(values);
          normalized.compute(
              name,
              (ignored, current) -> {
                if (current == null) {
                  return copied;
                }
                ArrayList<String> merged = new ArrayList<>(current);
                merged.addAll(copied);
                return List.copyOf(merged);
              });
        });
    headers = Collections.unmodifiableMap(normalized);
  }

  /**
   * Gets one header without case sensitivity.
   *
   * @param name the header name
   * @return the value, or {@code null}
   */
  public String header(String name) {
    List<String> values = headerValues(name);
    return values.size() == 1 ? values.get(0) : null;
  }

  /**
   * Gets all values for one header without case sensitivity.
   *
   * @param name the header name
   * @return the immutable values, possibly empty
   */
  public List<String> headerValues(String name) {
    return headers.getOrDefault(Objects.requireNonNull(name, "name"), List.of());
  }

  /**
   * Gets a copy of the request body.
   *
   * @return a new byte array that contains the request body
   */
  @Override
  public byte[] body() {
    return body.clone();
  }
}
