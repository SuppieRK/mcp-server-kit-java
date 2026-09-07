package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;

/**
 * Capabilities declared by a server, including extension-defined capabilities.
 *
 * @param completions Present if the server supports argument autocompletion suggestions.
 * @param experimental Experimental, non-standard capabilities that the server supports.
 * @param extensions optional extension settings, keyed by a prefixed extension identifier; an empty
 *     settings object declares support without settings
 * @param logging Present if the server supports sending log messages to the client.
 * @param prompts Present if the server offers any prompt templates.
 * @param resources Present if the server offers any resources to read.
 * @param tools Present if the server offers any tools to call.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpServerCapabilities(
    Optional<ObjectNode> completions,
    Optional<ObjectNode> experimental,
    Optional<ObjectNode> extensions,
    Optional<ObjectNode> logging,
    Optional<ObjectNode> prompts,
    Optional<ObjectNode> resources,
    Optional<ObjectNode> tools) {
  /** Validates and copies the protocol fields. */
  public McpServerCapabilities {
    completions = McpProtocol.copy(completions);
    experimental = McpProtocol.copy(experimental);
    extensions = McpProtocol.copy(extensions);
    logging = McpProtocol.copy(logging);
    prompts = McpProtocol.copy(prompts);
    resources = McpProtocol.copy(resources);
    tools = McpProtocol.copy(tools);
  }

  /**
   * Returns a copy of the optional completions capability.
   *
   * @return the copied completions capability
   */
  public Optional<ObjectNode> completions() {
    return McpProtocol.copy(completions);
  }

  /**
   * Returns a copy of the optional experimental capabilities.
   *
   * @return the copied experimental capabilities
   */
  public Optional<ObjectNode> experimental() {
    return McpProtocol.copy(experimental);
  }

  /**
   * Returns a copy of the optional extensions.
   *
   * @return the copied extensions
   */
  public Optional<ObjectNode> extensions() {
    return McpProtocol.copy(extensions);
  }

  /**
   * Returns a copy of the optional logging capability.
   *
   * @return the copied logging capability
   */
  public Optional<ObjectNode> logging() {
    return McpProtocol.copy(logging);
  }

  /**
   * Returns a copy of the optional prompts capability.
   *
   * @return the copied prompts capability
   */
  public Optional<ObjectNode> prompts() {
    return McpProtocol.copy(prompts);
  }

  /**
   * Returns a copy of the optional resources capability.
   *
   * @return the copied resources capability
   */
  public Optional<ObjectNode> resources() {
    return McpProtocol.copy(resources);
  }

  /**
   * Returns a copy of the optional tools capability.
   *
   * @return the copied tools capability
   */
  public Optional<ObjectNode> tools() {
    return McpProtocol.copy(tools);
  }

  /**
   * Creates a builder for {@link McpServerCapabilities}.
   *
   * @return a new builder
   */
  public static Builder mcpServerCapabilities() {
    return new Builder();
  }

  /** Builds {@link McpServerCapabilities} values. */
  public static final class Builder {
    private Optional<ObjectNode> completions = Optional.empty();
    private Optional<ObjectNode> experimental = Optional.empty();
    private Optional<ObjectNode> extensions = Optional.empty();
    private Optional<ObjectNode> logging = Optional.empty();
    private Optional<ObjectNode> prompts = Optional.empty();
    private Optional<ObjectNode> resources = Optional.empty();
    private Optional<ObjectNode> tools = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code completions}.
     *
     * @param completions the optional value
     * @return this builder
     */
    public Builder completions(Optional<ObjectNode> completions) {
      this.completions = completions;
      return this;
    }

    /**
     * Sets {@code completions}.
     *
     * @param completions the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder completions(ObjectNode completions) {
      return completions(Optional.ofNullable(completions));
    }

    /**
     * Sets {@code experimental}.
     *
     * @param experimental the optional value
     * @return this builder
     */
    public Builder experimental(Optional<ObjectNode> experimental) {
      this.experimental = experimental;
      return this;
    }

    /**
     * Sets {@code experimental}.
     *
     * @param experimental the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder experimental(ObjectNode experimental) {
      return experimental(Optional.ofNullable(experimental));
    }

    /**
     * Sets {@code extensions}.
     *
     * @param extensions the optional value
     * @return this builder
     */
    public Builder extensions(Optional<ObjectNode> extensions) {
      this.extensions = extensions;
      return this;
    }

    /**
     * Sets {@code extensions}.
     *
     * @param extensions the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder extensions(ObjectNode extensions) {
      return extensions(Optional.ofNullable(extensions));
    }

    /**
     * Sets {@code logging}.
     *
     * @param logging the optional value
     * @return this builder
     */
    public Builder logging(Optional<ObjectNode> logging) {
      this.logging = logging;
      return this;
    }

    /**
     * Sets {@code logging}.
     *
     * @param logging the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder logging(ObjectNode logging) {
      return logging(Optional.ofNullable(logging));
    }

    /**
     * Sets {@code prompts}.
     *
     * @param prompts the optional value
     * @return this builder
     */
    public Builder prompts(Optional<ObjectNode> prompts) {
      this.prompts = prompts;
      return this;
    }

    /**
     * Sets {@code prompts}.
     *
     * @param prompts the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder prompts(ObjectNode prompts) {
      return prompts(Optional.ofNullable(prompts));
    }

    /**
     * Sets {@code resources}.
     *
     * @param resources the optional value
     * @return this builder
     */
    public Builder resources(Optional<ObjectNode> resources) {
      this.resources = resources;
      return this;
    }

    /**
     * Sets {@code resources}.
     *
     * @param resources the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder resources(ObjectNode resources) {
      return resources(Optional.ofNullable(resources));
    }

    /**
     * Sets {@code tools}.
     *
     * @param tools the optional value
     * @return this builder
     */
    public Builder tools(Optional<ObjectNode> tools) {
      this.tools = tools;
      return this;
    }

    /**
     * Sets {@code tools}.
     *
     * @param tools the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder tools(ObjectNode tools) {
      return tools(Optional.ofNullable(tools));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpServerCapabilities build() {
      return new McpServerCapabilities(
          completions, experimental, extensions, logging, prompts, resources, tools);
    }
  }
}
