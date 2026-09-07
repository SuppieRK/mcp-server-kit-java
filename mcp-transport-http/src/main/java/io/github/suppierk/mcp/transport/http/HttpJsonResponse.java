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
}
