package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A request from the assistant to call a tool.
 *
 * @param meta Optional metadata about the tool use. Clients SHOULD preserve this field when
 *     including tool uses in subsequent sampling requests to enable caching optimizations.
 * @param id A unique identifier for this tool use. This ID is used to match tool results to their
 *     corresponding tool uses.
 * @param input The arguments to pass to the tool, conforming to the tool's input schema.
 * @param name The name of the tool to call.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpToolUseContent(
    Optional<McpMetaObject> meta, String id, Map<String, ?> input, String name)
    implements McpSamplingMessageContentBlock {

  private static final String TYPE = "tool_use";

  /** Validates and copies the protocol fields. */
  public McpToolUseContent {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(id, "id");
    input = McpProtocol.copy(Objects.requireNonNull(input, "input"));
    Objects.requireNonNull(name, "name");
  }

  /**
   * Returns a copy of the tool input.
   *
   * @return the copied input
   */
  public Map<String, ?> input() {
    return McpProtocol.copy(input);
  }

  /**
   * Gets the constant {@code type} value.
   *
   * @return the constant value
   */
  public String type() {
    return TYPE;
  }

  /**
   * Creates a builder for {@link McpToolUseContent}.
   *
   * @return a new builder
   */
  public static Builder mcpToolUseContent() {
    return new Builder();
  }

  /** Builds {@link McpToolUseContent} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private String id;
    private Map<String, ?> input;
    private String name;

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Sets {@code id}.
     *
     * @param id the value
     * @return this builder
     */
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets {@code input}.
     *
     * @param input the value
     * @return this builder
     */
    public Builder input(Map<String, ?> input) {
      this.input = input;
      return this;
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
     * Builds the value.
     *
     * @return the built value
     */
    public McpToolUseContent build() {
      return new McpToolUseContent(meta, id, input, name);
    }
  }
}
