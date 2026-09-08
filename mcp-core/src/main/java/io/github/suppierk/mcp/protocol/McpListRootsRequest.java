package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Optional;

/**
 * Sent from the server to request a list of root URIs from the client. Roots allow servers to ask
 * for specific directories or files to operate on. A common example for roots is providing a set of
 * repositories or directories a server should operate on. This request is typically used when the
 * server needs to understand the file system structure or access specific locations that the client
 * has permission to read from.
 *
 * @param params the method parameters
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListRootsRequest(Optional<Map<String, ?>> params) implements McpInputRequest {

  private static final String METHOD_NAME = "roots/list";

  /** Validates and copies the protocol fields. */
  public McpListRootsRequest {
    params = McpProtocol.copy(params);
  }

  /**
   * Returns a copy of the optional parameters.
   *
   * @return the copied parameters
   */
  public Optional<Map<String, ?>> params() {
    return McpProtocol.copy(params);
  }

  /**
   * Gets the constant {@code method} value.
   *
   * @return the constant value
   */
  public String method() {
    return METHOD_NAME;
  }

  /**
   * Creates a builder for {@link McpListRootsRequest}.
   *
   * @return a new builder
   */
  public static Builder mcpListRootsRequest() {
    return new Builder();
  }

  /** Builds {@link McpListRootsRequest} values. */
  public static final class Builder {
    private Optional<Map<String, ?>> params = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code params}.
     *
     * @param params the optional value
     * @return this builder
     */
    public Builder params(Optional<Map<String, ?>> params) {
      this.params = params;
      return this;
    }

    /**
     * Sets {@code params}.
     *
     * @param params the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder params(Map<String, ?> params) {
      return params(Optional.ofNullable(params));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpListRootsRequest build() {
      return new McpListRootsRequest(params);
    }
  }
}
