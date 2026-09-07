package io.github.suppierk.mcp.transport.http;

import java.util.Map;

/** A 202 response for an accepted MCP notification. */
public record HttpAcceptedResponse() implements HttpMcpResponse {
  /** {@inheritDoc} */
  @Override
  public int status() {
    return 202;
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> headers() {
    return Map.of();
  }
}
