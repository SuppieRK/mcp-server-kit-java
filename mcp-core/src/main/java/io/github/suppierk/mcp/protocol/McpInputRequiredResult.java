package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An InputRequiredResult sent by the server to indicate that additional input is needed before the
 * request can be completed. At least one of {@code inputRequests} or {@code requestState} MUST be
 * present.
 *
 * @param meta the optional protocol metadata
 * @param inputRequests the requested user inputs
 * @param requestState the opaque request state
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpInputRequiredResult(
    Optional<McpResultMetaObject> meta,
    Optional<McpInputRequests> inputRequests,
    Optional<String> requestState,
    String resultType)
    implements McpServerResult,
        McpCallToolResultResponse.Result,
        McpGetPromptResultResponse.Result,
        McpReadResourceResultResponse.Result {
  /** Validates and copies the protocol fields. */
  public McpInputRequiredResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(inputRequests, "inputRequests");
    Objects.requireNonNull(requestState, "requestState");
    Objects.requireNonNull(resultType, "resultType");
  }

  /**
   * Creates a builder for {@link McpInputRequiredResult}.
   *
   * @return a new builder
   */
  public static Builder mcpInputRequiredResult() {
    return new Builder();
  }

  /** Builds {@link McpInputRequiredResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private Optional<McpInputRequests> inputRequests = Optional.empty();
    private Optional<String> requestState = Optional.empty();
    private String resultType;

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpResultMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpResultMetaObject.Builder> configure) {
      var child = McpResultMetaObject.mcpResultMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Sets {@code inputRequests} using a {@link McpInputRequests} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder inputRequests(Consumer<McpInputRequests.Builder> configure) {
      var child = McpInputRequests.mcpInputRequests();
      configure.accept(child);
      return inputRequests(child.build());
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpResultMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpResultMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Sets {@code inputRequests}.
     *
     * @param inputRequests the optional value
     * @return this builder
     */
    public Builder inputRequests(Optional<McpInputRequests> inputRequests) {
      this.inputRequests = inputRequests;
      return this;
    }

    /**
     * Sets {@code inputRequests}.
     *
     * @param inputRequests the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder inputRequests(McpInputRequests inputRequests) {
      return inputRequests(Optional.ofNullable(inputRequests));
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
     * Sets {@code resultType}.
     *
     * @param resultType the value
     * @return this builder
     */
    public Builder resultType(String resultType) {
      this.resultType = resultType;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpInputRequiredResult build() {
      return new McpInputRequiredResult(meta, inputRequests, requestState, resultType);
    }
  }
}
