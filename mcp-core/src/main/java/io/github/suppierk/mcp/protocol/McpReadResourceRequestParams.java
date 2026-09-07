package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Parameters for a {@code resources/read} request.
 *
 * @param meta the optional protocol metadata
 * @param inputResponses the supplied user input responses
 * @param requestState the opaque request state
 * @param uri The URI of the resource. The URI can use any protocol; it is up to the server how to
 *     interpret it.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpReadResourceRequestParams(
    @JsonProperty("_meta") McpRequestMetaObject meta,
    Optional<McpInputResponses> inputResponses,
    Optional<String> requestState,
    URI uri)
    implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpReadResourceRequestParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(inputResponses, "inputResponses");
    Objects.requireNonNull(requestState, "requestState");
    Objects.requireNonNull(uri, "uri");
  }

  /**
   * Creates a builder for {@link McpReadResourceRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpReadResourceRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpReadResourceRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;
    private Optional<McpInputResponses> inputResponses = Optional.empty();
    private Optional<String> requestState = Optional.empty();
    private URI uri;

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the value
     * @return this builder
     */
    public Builder meta(McpRequestMetaObject meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code inputResponses}.
     *
     * @param inputResponses the optional value
     * @return this builder
     */
    public Builder inputResponses(Optional<McpInputResponses> inputResponses) {
      this.inputResponses = inputResponses;
      return this;
    }

    /**
     * Sets {@code inputResponses}.
     *
     * @param inputResponses the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder inputResponses(McpInputResponses inputResponses) {
      return inputResponses(Optional.ofNullable(inputResponses));
    }

    /**
     * Sets {@code requestState}.
     *
     * @param requestState the optional value
     * @return this builder
     */
    public Builder requestState(Optional<String> requestState) {
      this.requestState = requestState;
      return this;
    }

    /**
     * Sets {@code requestState}.
     *
     * @param requestState the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder requestState(String requestState) {
      return requestState(Optional.ofNullable(requestState));
    }

    /**
     * Sets {@code uri}.
     *
     * @param uri the value
     * @return this builder
     */
    public Builder uri(URI uri) {
      this.uri = uri;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpReadResourceRequestParams build() {
      return new McpReadResourceRequestParams(meta, inputResponses, requestState, uri);
    }
  }
}
