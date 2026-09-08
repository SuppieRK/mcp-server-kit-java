package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Parameters for a {@code prompts/get} request.
 *
 * @param meta the optional protocol metadata
 * @param arguments Arguments to use for templating the prompt.
 * @param inputResponses the supplied user input responses
 * @param name The name of the prompt or prompt template.
 * @param requestState the opaque request state
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpGetPromptRequestParams(
    McpRequestMetaObject meta,
    Optional<Map<String, ?>> arguments,
    Optional<McpInputResponses> inputResponses,
    String name,
    Optional<String> requestState)
    implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpGetPromptRequestParams {
    Objects.requireNonNull(meta, "meta");
    arguments = McpProtocol.copy(arguments);
    Objects.requireNonNull(inputResponses, "inputResponses");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(requestState, "requestState");
  }

  /**
   * Returns a copy of the optional prompt arguments.
   *
   * @return the copied arguments
   */
  public Optional<Map<String, ?>> arguments() {
    return McpProtocol.copy(arguments);
  }

  /**
   * Creates a builder for {@link McpGetPromptRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpGetPromptRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpGetPromptRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;
    private Optional<Map<String, ?>> arguments = Optional.empty();
    private Optional<McpInputResponses> inputResponses = Optional.empty();
    private String name;
    private Optional<String> requestState = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpRequestMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpRequestMetaObject.Builder> configure) {
      var child = McpRequestMetaObject.mcpRequestMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Sets {@code inputResponses} using a {@link McpInputResponses} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder inputResponses(Consumer<McpInputResponses.Builder> configure) {
      var child = McpInputResponses.mcpInputResponses();
      configure.accept(child);
      return inputResponses(child.build());
    }

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
     * Sets {@code arguments}.
     *
     * @param arguments the optional value
     * @return this builder
     */
    public Builder arguments(Optional<Map<String, ?>> arguments) {
      this.arguments = arguments;
      return this;
    }

    /**
     * Sets {@code arguments}.
     *
     * @param arguments the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder arguments(Map<String, ?> arguments) {
      return arguments(Optional.ofNullable(arguments));
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
     * Sets {@code name}.
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
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
    public McpGetPromptRequestParams build() {
      return new McpGetPromptRequestParams(meta, arguments, inputResponses, name, requestState);
    }
  }
}
