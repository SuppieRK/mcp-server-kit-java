package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Parameters for a {@code sampling/createMessage} request.
 *
 * @param includeContext A request to include context from one or more MCP servers (including the
 *     caller), to be attached to the prompt. The client MAY ignore this request. Default is {@code
 *     "none"}. The values {@code "thisServer"} and {@code "allServers"} are deprecated (SEP-2596):
 *     servers SHOULD omit this field or use {@code "none"}, and SHOULD only use the deprecated
 *     values if the client declares {@code ClientCapabilities.sampling.context}.
 * @param maxTokens The requested maximum number of tokens to sample (to prevent runaway
 *     completions). The client MAY choose to sample fewer tokens than the requested maximum.
 * @param messages the message list
 * @param metadata Optional metadata to pass through to the LLM provider. The format of this
 *     metadata is provider-specific.
 * @param modelPreferences The server's preferences for which model to select. The client MAY ignore
 *     these preferences.
 * @param stopSequences the optional stop sequences
 * @param systemPrompt An optional system prompt the server wants to use for sampling. The client
 *     MAY modify or omit this prompt.
 * @param temperature the optional sampling temperature
 * @param toolChoice Controls how the model uses tools. The client MUST return an error if this
 *     field is provided but {@code ClientCapabilities.sampling.tools} is not declared. Default is
 *     {@code { mode: "auto" }}.
 * @param tools Tools that the model may use during generation. The client MUST return an error if
 *     this field is provided but {@code ClientCapabilities.sampling.tools} is not declared.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCreateMessageRequestParams(
    Optional<String> includeContext,
    Long maxTokens,
    List<McpSamplingMessage> messages,
    Optional<Map<String, ?>> metadata,
    Optional<McpModelPreferences> modelPreferences,
    Optional<List<String>> stopSequences,
    Optional<String> systemPrompt,
    Optional<Double> temperature,
    Optional<McpToolChoice> toolChoice,
    Optional<List<McpTool>> tools) {
  /** Validates and copies the protocol fields. */
  public McpCreateMessageRequestParams {
    Objects.requireNonNull(includeContext, "includeContext");
    Objects.requireNonNull(maxTokens, "maxTokens");
    messages = List.copyOf(messages);
    metadata = McpProtocol.copy(metadata);
    Objects.requireNonNull(modelPreferences, "modelPreferences");
    stopSequences = Objects.requireNonNull(stopSequences, "stopSequences").map(List::copyOf);
    Objects.requireNonNull(systemPrompt, "systemPrompt");
    Objects.requireNonNull(temperature, "temperature");
    Objects.requireNonNull(toolChoice, "toolChoice");
    tools = Objects.requireNonNull(tools, "tools").map(List::copyOf);
  }

  /**
   * Returns a copy of the optional provider metadata.
   *
   * @return the copied metadata
   */
  public Optional<Map<String, ?>> metadata() {
    return McpProtocol.copy(metadata);
  }

  /**
   * Creates a builder for {@link McpCreateMessageRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpCreateMessageRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpCreateMessageRequestParams} values. */
  public static final class Builder {
    private Optional<String> includeContext = Optional.empty();
    private Long maxTokens;
    private List<McpSamplingMessage> messages;
    private Optional<Map<String, ?>> metadata = Optional.empty();
    private Optional<McpModelPreferences> modelPreferences = Optional.empty();
    private Optional<List<String>> stopSequences = Optional.empty();
    private Optional<String> systemPrompt = Optional.empty();
    private Optional<Double> temperature = Optional.empty();
    private Optional<McpToolChoice> toolChoice = Optional.empty();
    private Optional<List<McpTool>> tools = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code includeContext}.
     *
     * @param includeContext the optional value
     * @return this builder
     */
    public Builder includeContext(Optional<String> includeContext) {
      this.includeContext = includeContext;
      return this;
    }

    /**
     * Sets {@code includeContext}.
     *
     * @param includeContext the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder includeContext(String includeContext) {
      return includeContext(Optional.ofNullable(includeContext));
    }

    /**
     * Sets {@code maxTokens}.
     *
     * @param maxTokens the value
     * @return this builder
     */
    public Builder maxTokens(Long maxTokens) {
      this.maxTokens = maxTokens;
      return this;
    }

    /**
     * Sets {@code messages}.
     *
     * @param messages the value
     * @return this builder
     */
    public Builder messages(List<McpSamplingMessage> messages) {
      this.messages = messages;
      return this;
    }

    /**
     * Sets {@code metadata}.
     *
     * @param metadata the optional value
     * @return this builder
     */
    public Builder metadata(Optional<Map<String, ?>> metadata) {
      this.metadata = metadata;
      return this;
    }

    /**
     * Sets {@code metadata}.
     *
     * @param metadata the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder metadata(Map<String, ?> metadata) {
      return metadata(Optional.ofNullable(metadata));
    }

    /**
     * Sets {@code modelPreferences}.
     *
     * @param modelPreferences the optional value
     * @return this builder
     */
    public Builder modelPreferences(Optional<McpModelPreferences> modelPreferences) {
      this.modelPreferences = modelPreferences;
      return this;
    }

    /**
     * Sets {@code modelPreferences}.
     *
     * @param modelPreferences the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder modelPreferences(McpModelPreferences modelPreferences) {
      return modelPreferences(Optional.ofNullable(modelPreferences));
    }

    /**
     * Sets {@code stopSequences}.
     *
     * @param stopSequences the optional value
     * @return this builder
     */
    public Builder stopSequences(Optional<List<String>> stopSequences) {
      this.stopSequences = stopSequences;
      return this;
    }

    /**
     * Sets {@code stopSequences}.
     *
     * @param stopSequences the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder stopSequences(List<String> stopSequences) {
      return stopSequences(Optional.ofNullable(stopSequences));
    }

    /**
     * Sets {@code systemPrompt}.
     *
     * @param systemPrompt the optional value
     * @return this builder
     */
    public Builder systemPrompt(Optional<String> systemPrompt) {
      this.systemPrompt = systemPrompt;
      return this;
    }

    /**
     * Sets {@code systemPrompt}.
     *
     * @param systemPrompt the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder systemPrompt(String systemPrompt) {
      return systemPrompt(Optional.ofNullable(systemPrompt));
    }

    /**
     * Sets {@code temperature}.
     *
     * @param temperature the optional value
     * @return this builder
     */
    public Builder temperature(Optional<Double> temperature) {
      this.temperature = temperature;
      return this;
    }

    /**
     * Sets {@code temperature}.
     *
     * @param temperature the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder temperature(Double temperature) {
      return temperature(Optional.ofNullable(temperature));
    }

    /**
     * Sets {@code toolChoice}.
     *
     * @param toolChoice the optional value
     * @return this builder
     */
    public Builder toolChoice(Optional<McpToolChoice> toolChoice) {
      this.toolChoice = toolChoice;
      return this;
    }

    /**
     * Sets {@code toolChoice}.
     *
     * @param toolChoice the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder toolChoice(McpToolChoice toolChoice) {
      return toolChoice(Optional.ofNullable(toolChoice));
    }

    /**
     * Sets {@code tools}.
     *
     * @param tools the optional value
     * @return this builder
     */
    public Builder tools(Optional<List<McpTool>> tools) {
      this.tools = tools;
      return this;
    }

    /**
     * Sets {@code tools}.
     *
     * @param tools the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder tools(List<McpTool> tools) {
      return tools(Optional.ofNullable(tools));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCreateMessageRequestParams build() {
      return new McpCreateMessageRequestParams(
          includeContext,
          maxTokens,
          messages,
          metadata,
          modelPreferences,
          stopSequences,
          systemPrompt,
          temperature,
          toolChoice,
          tools);
    }
  }
}
