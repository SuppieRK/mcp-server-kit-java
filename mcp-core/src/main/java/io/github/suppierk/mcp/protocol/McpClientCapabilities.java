package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
    Optional<ObjectNode> elicitation,
    Optional<ObjectNode> experimental,
    Optional<ObjectNode> extensions,
    Optional<ObjectNode> roots,
    Optional<ObjectNode> sampling) {
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
  public Optional<ObjectNode> elicitation() {
    return McpProtocol.copy(elicitation);
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
   * Returns a copy of the optional roots capability.
   *
   * @return the copied roots capability
   */
  public Optional<ObjectNode> roots() {
    return McpProtocol.copy(roots);
  }

  /**
   * Returns a copy of the optional sampling capability.
   *
   * @return the copied sampling capability
   */
  public Optional<ObjectNode> sampling() {
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
    private Optional<ObjectNode> elicitation = Optional.empty();
    private Optional<ObjectNode> experimental = Optional.empty();
    private Optional<ObjectNode> extensions = Optional.empty();
    private Optional<ObjectNode> roots = Optional.empty();
    private Optional<ObjectNode> sampling = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code elicitation}.
     *
     * @param elicitation the optional value
     * @return this builder
     */
    public Builder elicitation(Optional<ObjectNode> elicitation) {
      this.elicitation = elicitation;
      return this;
    }

    /**
     * Sets {@code elicitation}.
     *
     * @param elicitation the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder elicitation(ObjectNode elicitation) {
      return elicitation(Optional.ofNullable(elicitation));
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
     * Sets {@code roots}.
     *
     * @param roots the optional value
     * @return this builder
     */
    public Builder roots(Optional<ObjectNode> roots) {
      this.roots = roots;
      return this;
    }

    /**
     * Sets {@code roots}.
     *
     * @param roots the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder roots(ObjectNode roots) {
      return roots(Optional.ofNullable(roots));
    }

    /**
     * Sets {@code sampling}.
     *
     * @param sampling the optional value
     * @return this builder
     */
    public Builder sampling(Optional<ObjectNode> sampling) {
      this.sampling = sampling;
      return this;
    }

    /**
     * Sets {@code sampling}.
     *
     * @param sampling the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder sampling(ObjectNode sampling) {
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
