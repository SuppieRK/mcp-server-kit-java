package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Additional properties describing a {@code Tool} to clients. NOTE: all properties in {@code
 * McpToolAnnotations} values are hints. They do not guarantee an accurate description of tool
 * behavior (including descriptive properties like {@code title}). Clients should never make tool
 * use decisions based on {@code ToolAnnotations} received from untrusted servers.
 *
 * @param destructiveHint If true, the tool may perform destructive updates to its environment. If
 *     false, the tool performs only additive updates. (This property is meaningful only when {@code
 *     readOnlyHint == false}) Default: true
 * @param idempotentHint whether repeated calls with the same arguments have no additional effect.
 *     This hint applies only when {@code readOnlyHint} is false. The default is false.
 * @param openWorldHint If true, this tool may interact with an "open world" of external entities.
 *     If false, the tool's domain of interaction is closed. For example, the world of a web search
 *     tool is open, whereas that of a memory tool is not. Default: true
 * @param readOnlyHint If true, the tool does not modify its environment. Default: false
 * @param title A human-readable title for the tool.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpToolAnnotations(
    Optional<Boolean> destructiveHint,
    Optional<Boolean> idempotentHint,
    Optional<Boolean> openWorldHint,
    Optional<Boolean> readOnlyHint,
    Optional<String> title) {
  /** Validates and copies the protocol fields. */
  public McpToolAnnotations {
    Objects.requireNonNull(destructiveHint, "destructiveHint");
    Objects.requireNonNull(idempotentHint, "idempotentHint");
    Objects.requireNonNull(openWorldHint, "openWorldHint");
    Objects.requireNonNull(readOnlyHint, "readOnlyHint");
    Objects.requireNonNull(title, "title");
  }

  /**
   * Creates a builder for {@link McpToolAnnotations}.
   *
   * @return a new builder
   */
  public static Builder mcpToolAnnotations() {
    return new Builder();
  }

  /** Builds {@link McpToolAnnotations} values. */
  public static final class Builder {
    private Optional<Boolean> destructiveHint = Optional.empty();
    private Optional<Boolean> idempotentHint = Optional.empty();
    private Optional<Boolean> openWorldHint = Optional.empty();
    private Optional<Boolean> readOnlyHint = Optional.empty();
    private Optional<String> title = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code destructiveHint}.
     *
     * @param destructiveHint the optional value
     * @return this builder
     */
    public Builder destructiveHint(Optional<Boolean> destructiveHint) {
      this.destructiveHint = destructiveHint;
      return this;
    }

    /**
     * Sets {@code destructiveHint}.
     *
     * @param destructiveHint the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder destructiveHint(Boolean destructiveHint) {
      return destructiveHint(Optional.ofNullable(destructiveHint));
    }

    /**
     * Sets {@code idempotentHint}.
     *
     * @param idempotentHint the optional value
     * @return this builder
     */
    public Builder idempotentHint(Optional<Boolean> idempotentHint) {
      this.idempotentHint = idempotentHint;
      return this;
    }

    /**
     * Sets {@code idempotentHint}.
     *
     * @param idempotentHint the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder idempotentHint(Boolean idempotentHint) {
      return idempotentHint(Optional.ofNullable(idempotentHint));
    }

    /**
     * Sets {@code openWorldHint}.
     *
     * @param openWorldHint the optional value
     * @return this builder
     */
    public Builder openWorldHint(Optional<Boolean> openWorldHint) {
      this.openWorldHint = openWorldHint;
      return this;
    }

    /**
     * Sets {@code openWorldHint}.
     *
     * @param openWorldHint the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder openWorldHint(Boolean openWorldHint) {
      return openWorldHint(Optional.ofNullable(openWorldHint));
    }

    /**
     * Sets {@code readOnlyHint}.
     *
     * @param readOnlyHint the optional value
     * @return this builder
     */
    public Builder readOnlyHint(Optional<Boolean> readOnlyHint) {
      this.readOnlyHint = readOnlyHint;
      return this;
    }

    /**
     * Sets {@code readOnlyHint}.
     *
     * @param readOnlyHint the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder readOnlyHint(Boolean readOnlyHint) {
      return readOnlyHint(Optional.ofNullable(readOnlyHint));
    }

    /**
     * Sets {@code title}.
     *
     * @param title the optional value
     * @return this builder
     */
    public Builder title(Optional<String> title) {
      this.title = title;
      return this;
    }

    /**
     * Sets {@code title}.
     *
     * @param title the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder title(String title) {
      return title(Optional.ofNullable(title));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpToolAnnotations build() {
      return new McpToolAnnotations(
          destructiveHint, idempotentHint, openWorldHint, readOnlyHint, title);
    }
  }
}
