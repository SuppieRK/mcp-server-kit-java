package io.github.suppierk.mcp.transport.http;

import java.util.ArrayList;
import java.util.Arrays;
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

  /** Compares the method, case-preserved header entries, and body bytes by value. */
  @Override
  public boolean equals(Object other) {
    return other instanceof HttpMcpRequest request
        && method.equals(request.method)
        // Use ordinary map equality, not the lookup map's case-insensitive comparator.
        && Map.copyOf(headers).equals(Map.copyOf(request.headers))
        && Arrays.equals(body, request.body);
  }

  /** Hashes the method, case-preserved header entries, and body bytes by value. */
  @Override
  public int hashCode() {
    return Objects.hash(method, headers, Arrays.hashCode(body));
  }

  /** Describes the method, header entries, and body bytes. */
  @Override
  public String toString() {
    return "HttpMcpRequest[method="
        + method
        + ", headers="
        + headers
        + ", body="
        + Arrays.toString(body)
        + "]";
  }

  /**
   * Creates a request builder with no headers and an empty body. The method is required.
   *
   * @return a new builder
   */
  public static Builder httpMcpRequest() {
    return new Builder();
  }

  /** Builds {@link HttpMcpRequest} values. */
  public static final class Builder {
    private String method;
    private Map<String, List<String>> headers = Map.of();
    private byte[] body = new byte[0];

    private Builder() {}

    /**
     * Sets the HTTP method.
     *
     * @param method the method name
     * @return this builder
     */
    public Builder method(String method) {
      this.method = method;
      return this;
    }

    /**
     * Sets the headers, normalized and copied when built.
     *
     * @param headers the header field values
     * @return this builder
     */
    public Builder headers(Map<String, List<String>> headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Sets the request body, copied when built.
     *
     * @param body the request bytes
     * @return this builder
     */
    public Builder body(byte[] body) {
      this.body = body;
      return this;
    }

    /**
     * Builds an immutable request snapshot.
     *
     * @return the request
     */
    public HttpMcpRequest build() {
      return new HttpMcpRequest(method, headers, body);
    }
  }
}
