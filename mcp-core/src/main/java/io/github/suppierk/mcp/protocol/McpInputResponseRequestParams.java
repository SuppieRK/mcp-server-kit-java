package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

/**
 * Parameters that resume a request with supplied input.
 *
 * @param meta the optional protocol metadata
 * @param inputResponses the supplied user input responses
 * @param requestState the opaque request state
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpInputResponseRequestParams(
    @JsonProperty("_meta") McpRequestMetaObject meta,
    Optional<McpInputResponses> inputResponses,
    Optional<String> requestState) {
  /** Validates and copies the protocol fields. */
  public McpInputResponseRequestParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(inputResponses, "inputResponses");
    Objects.requireNonNull(requestState, "requestState");
  }

  /**
   * Creates a builder for {@link McpInputResponseRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpInputResponseRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpInputResponseRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;
    private Optional<McpInputResponses> inputResponses = Optional.empty();
    private Optional<String> requestState = Optional.empty();

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
     * Builds the value.
     *
     * @return the built value
     */
    public McpInputResponseRequestParams build() {
      return new McpInputResponseRequestParams(meta, inputResponses, requestState);
    }
  }
}
