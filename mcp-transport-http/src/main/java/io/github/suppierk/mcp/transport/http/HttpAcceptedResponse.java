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

  /**
   * Creates a builder for the fixed accepted response.
   *
   * @return a new builder
   */
  public static Builder httpAcceptedResponse() {
    return new Builder();
  }

  /** Builds {@link HttpAcceptedResponse} values. */
  public static final class Builder {
    private Builder() {}

    /**
     * Builds the accepted response.
     *
     * @return the response
     */
    public HttpAcceptedResponse build() {
      return new HttpAcceptedResponse();
    }
  }
}
