package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.Objects;

/**
 * The parameters for a request to elicit information from the user via a URL in the client.
 *
 * @param message The message to present to the user explaining why the interaction is needed.
 * @param url The URL that the user should navigate to.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpElicitRequestURLParams(String message, URI url) implements McpElicitRequestParams {

  private static final String MODE = "url";

  /** Validates and copies the protocol fields. */
  public McpElicitRequestURLParams {
    Objects.requireNonNull(message, "message");
    Objects.requireNonNull(url, "url");
  }

  /**
   * Gets the constant {@code mode} value.
   *
   * @return the constant value
   */
  public String mode() {
    return MODE;
  }

  /**
   * Creates a builder for {@link McpElicitRequestURLParams}.
   *
   * @return a new builder
   */
  public static Builder mcpElicitRequestURLParams() {
    return new Builder();
  }

  /** Builds {@link McpElicitRequestURLParams} values. */
  public static final class Builder {
    private String message;
    private URI url;

    private Builder() {}

    /**
     * Sets {@code message}.
     *
     * @param message the value
     * @return this builder
     */
    public Builder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * Sets {@code url}.
     *
     * @param url the value
     * @return this builder
     */
    public Builder url(URI url) {
      this.url = url;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpElicitRequestURLParams build() {
      return new McpElicitRequestURLParams(message, url);
    }
  }
}
