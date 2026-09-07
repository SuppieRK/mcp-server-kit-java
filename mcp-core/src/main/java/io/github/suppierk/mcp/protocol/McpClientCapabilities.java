package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Optional;

/**
 * Capabilities declared by a client, including extension-defined capabilities.
 *
 * @param elicitation Present if the client supports elicitation from the server.
 * @param experimental Experimental, non-standard capabilities that the client supports.
 * @param extensions optional extension settings, keyed by a prefixed extension identifier; an empty
 *     settings object declares support without settings
 * @param roots Present if the client supports listing roots.
 * @param sampling Present if the client supports sampling from an LLM.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpClientCapabilities(
    Optional<Map<String, ?>> elicitation,
    Optional<Map<String, ?>> experimental,
    Optional<Map<String, ?>> extensions,
    Optional<Map<String, ?>> roots,
    Optional<Map<String, ?>> sampling) {
  /** Validates and copies the protocol fields. */
  public McpClientCapabilities {
    elicitation = McpProtocol.copy(elicitation);
    experimental = McpProtocol.copy(experimental);
    extensions = McpProtocol.copy(extensions);
    roots = McpProtocol.copy(roots);
    sampling = McpProtocol.copy(sampling);
  }

  /**
   * Returns a copy of the optional elicitation capability.
   *
   * @return the copied elicitation capability
   */
  public Optional<Map<String, ?>> elicitation() {
    return McpProtocol.copy(elicitation);
  }

  /**
   * Returns a copy of the optional experimental capabilities.
   *
   * @return the copied experimental capabilities
   */
  public Optional<Map<String, ?>> experimental() {
    return McpProtocol.copy(experimental);
  }

  /**
   * Returns a copy of the optional extensions.
   *
   * @return the copied extensions
   */
  public Optional<Map<String, ?>> extensions() {
    return McpProtocol.copy(extensions);
  }

  /**
   * Returns a copy of the optional roots capability.
   *
   * @return the copied roots capability
   */
  public Optional<Map<String, ?>> roots() {
    return McpProtocol.copy(roots);
  }

  /**
   * Returns a copy of the optional sampling capability.
   *
   * @return the copied sampling capability
   */
  public Optional<Map<String, ?>> sampling() {
    return McpProtocol.copy(sampling);
  }

  /**
   * Creates a builder for {@link McpClientCapabilities}.
   *
   * @return a new builder
   */
  public static Builder mcpClientCapabilities() {
    return new Builder();
  }

  /** Builds {@link McpClientCapabilities} values. */
  public static final class Builder {
    private Optional<Map<String, ?>> elicitation = Optional.empty();
    private Optional<Map<String, ?>> experimental = Optional.empty();
    private Optional<Map<String, ?>> extensions = Optional.empty();
    private Optional<Map<String, ?>> roots = Optional.empty();
    private Optional<Map<String, ?>> sampling = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code elicitation}.
     *
     * @param elicitation the optional value
     * @return this builder
     */
    public Builder elicitation(Optional<Map<String, ?>> elicitation) {
      this.elicitation = elicitation;
      return this;
    }

    /**
     * Sets {@code elicitation}.
     *
     * @param elicitation the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder elicitation(Map<String, ?> elicitation) {
      return elicitation(Optional.ofNullable(elicitation));
    }

    /**
     * Sets {@code experimental}.
     *
     * @param experimental the optional value
     * @return this builder
     */
    public Builder experimental(Optional<Map<String, ?>> experimental) {
      this.experimental = experimental;
      return this;
    }

    /**
     * Sets {@code experimental}.
     *
     * @param experimental the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder experimental(Map<String, ?> experimental) {
      return experimental(Optional.ofNullable(experimental));
    }

    /**
     * Sets {@code extensions}.
     *
     * @param extensions the optional value
     * @return this builder
     */
    public Builder extensions(Optional<Map<String, ?>> extensions) {
      this.extensions = extensions;
      return this;
    }

    /**
     * Sets {@code extensions}.
     *
     * @param extensions the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder extensions(Map<String, ?> extensions) {
      return extensions(Optional.ofNullable(extensions));
    }

    /**
     * Sets {@code roots}.
     *
     * @param roots the optional value
     * @return this builder
     */
    public Builder roots(Optional<Map<String, ?>> roots) {
      this.roots = roots;
      return this;
    }

    /**
     * Sets {@code roots}.
     *
     * @param roots the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder roots(Map<String, ?> roots) {
      return roots(Optional.ofNullable(roots));
    }

    /**
     * Sets {@code sampling}.
     *
     * @param sampling the optional value
     * @return this builder
     */
    public Builder sampling(Optional<Map<String, ?>> sampling) {
      this.sampling = sampling;
      return this;
    }

    /**
     * Sets {@code sampling}.
     *
     * @param sampling the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder sampling(Map<String, ?> sampling) {
      return sampling(Optional.ofNullable(sampling));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpClientCapabilities build() {
      return new McpClientCapabilities(elicitation, experimental, extensions, roots, sampling);
    }
  }
}
