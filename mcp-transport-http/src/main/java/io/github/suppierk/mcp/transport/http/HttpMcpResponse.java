package io.github.suppierk.mcp.transport.http;

import java.util.Map;

/** A framework-neutral Streamable HTTP response. */
public sealed interface HttpMcpResponse
    permits HttpAcceptedResponse, HttpEventStreamResponse, HttpJsonResponse {
  /**
   * Gets the HTTP status.
   *
   * @return the status code
   */
  int status();

  /**
   * Gets immutable response headers.
   *
   * @return the response headers
   */
  Map<String, String> headers();
}
