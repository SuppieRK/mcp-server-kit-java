package io.github.suppierk.mcp.transport.http;

import java.util.Map;
import java.util.Objects;

/**
 * A finite JSON HTTP response.
 *
 * @param status the HTTP status
 * @param headers the response headers
 * @param body the UTF-8 JSON body
 */
public record HttpJsonResponse(int status, Map<String, String> headers, byte[] body)
    implements HttpMcpResponse {
  /**
   * Copies the response data.
   *
   * @param status the HTTP status code
   * @param headers the response headers
   * @param body the JSON response body
   * @throws NullPointerException if {@code headers} or {@code body} is {@code null}
   */
  public HttpJsonResponse {
    headers = Map.copyOf(headers);
    body = Objects.requireNonNull(body, "body").clone();
  }

  /**
   * Gets a copy of the JSON response body.
   *
   * @return a new byte array that contains the response body
   */
  public byte[] body() {
    return body.clone();
  }

  /**
   * Creates a response builder with status 200, no headers, and an empty body.
   *
   * @return a new builder
   */
  public static Builder httpJsonResponse() {
    return new Builder();
  }

  /** Builds {@link HttpJsonResponse} values. */
  public static final class Builder {
    private int status = 200;
    private Map<String, String> headers = Map.of();
    private byte[] body = new byte[0];

    private Builder() {}

    /**
     * Sets the HTTP status.
     *
     * @param status the status code
     * @return this builder
     */
    public Builder status(int status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the response headers, copied when built.
     *
     * @param headers the response headers
     * @return this builder
     */
    public Builder headers(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Sets the response body, copied when built.
     *
     * @param body the UTF-8 JSON bytes
     * @return this builder
     */
    public Builder body(byte[] body) {
      this.body = body;
      return this;
    }

    /**
     * Builds an immutable response snapshot.
     *
     * @return the response
     */
    public HttpJsonResponse build() {
      return new HttpJsonResponse(status, headers, body);
    }
  }
}
