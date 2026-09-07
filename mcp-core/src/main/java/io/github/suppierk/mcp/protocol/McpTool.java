package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Definition for a tool the client can call.
 *
 * @param meta the optional protocol metadata
 * @param annotations optional tool behavior and display hints
 * @param description the optional tool description
 * @param icons optional icons for a user interface
 * @param inputSchema the JSON Schema 2020-12 object for tool arguments; the root type must be
 *     {@code object}
 * @param name the programmatic tool name and fallback display name
 * @param outputSchema the optional JSON Schema 2020-12 object for structured output
 * @param title the optional human-readable title for a user interface
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpTool(
    @JsonProperty("_meta") Optional<McpMetaObject> meta,
    Optional<McpToolAnnotations> annotations,
    Optional<String> description,
    Optional<List<McpIcon>> icons,
    ObjectNode inputSchema,
    String name,
    Optional<ObjectNode> outputSchema,
    Optional<String> title)
    implements McpBaseMetadata {
  /** Validates and copies the protocol fields. */
  public McpTool {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(annotations, "annotations");
    Objects.requireNonNull(description, "description");
    icons = Objects.requireNonNull(icons, "icons").map(List::copyOf);
    inputSchema = Objects.requireNonNull(inputSchema, "inputSchema").deepCopy();
    if (!"object".equals(inputSchema.path("type").textValue())) {
      throw new IllegalArgumentException("A tool input schema must have object type");
    }
    if (Objects.requireNonNull(name, "name").isBlank()) {
      throw new IllegalArgumentException("A tool name must not be blank");
    }
    outputSchema = Objects.requireNonNull(outputSchema, "outputSchema").map(ObjectNode::deepCopy);
    Objects.requireNonNull(title, "title");
  }

  /**
   * Creates a minimal tool declaration.
   *
   * @param name the tool name
   * @param inputSchema the input schema
   */
  public McpTool(String name, ObjectNode inputSchema) {
    this(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        inputSchema,
        name,
        Optional.empty(),
        Optional.empty());
  }

  /**
   * Returns a copy of the input schema.
   *
   * @return the input schema
   */
  @Override
  public ObjectNode inputSchema() {
    return inputSchema.deepCopy();
  }

  /**
   * Returns a copy of the optional output schema.
   *
   * @return the output schema
   */
  @Override
  public Optional<ObjectNode> outputSchema() {
    return outputSchema.map(ObjectNode::deepCopy);
  }

  /**
   * Creates a builder for {@link McpTool}.
   *
   * @return a new builder
   */
  public static Builder mcpTool() {
    return new Builder();
  }

  /** Builds {@link McpTool} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<McpToolAnnotations> annotations = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<List<McpIcon>> icons = Optional.empty();
    private ObjectNode inputSchema;
    private String name;
    private Optional<ObjectNode> outputSchema = Optional.empty();
    private Optional<String> title = Optional.empty();

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
     * Sets {@code annotations}.
     *
     * @param annotations the optional value
     * @return this builder
     */
    public Builder annotations(Optional<McpToolAnnotations> annotations) {
      this.annotations = annotations;
      return this;
    }

    /**
     * Sets {@code annotations}.
     *
     * @param annotations the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder annotations(McpToolAnnotations annotations) {
      return annotations(Optional.ofNullable(annotations));
    }

    /**
     * Sets {@code description}.
     *
     * @param description the optional value
     * @return this builder
     */
    public Builder description(Optional<String> description) {
      this.description = description;
      return this;
    }

    /**
     * Sets {@code description}.
     *
     * @param description the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder description(String description) {
      return description(Optional.ofNullable(description));
    }

    /**
     * Sets {@code icons}.
     *
     * @param icons the optional value
     * @return this builder
     */
    public Builder icons(Optional<List<McpIcon>> icons) {
      this.icons = icons;
      return this;
    }

    /**
     * Sets {@code icons}.
     *
     * @param icons the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder icons(List<McpIcon> icons) {
      return icons(Optional.ofNullable(icons));
    }

    /**
     * Sets {@code inputSchema}.
     *
     * @param inputSchema the value
     * @return this builder
     */
    public Builder inputSchema(ObjectNode inputSchema) {
      this.inputSchema = inputSchema;
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
     * Sets {@code outputSchema}.
     *
     * @param outputSchema the optional value
     * @return this builder
     */
    public Builder outputSchema(Optional<ObjectNode> outputSchema) {
      this.outputSchema = outputSchema;
      return this;
    }

    /**
     * Sets {@code outputSchema}.
     *
     * @param outputSchema the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder outputSchema(ObjectNode outputSchema) {
      return outputSchema(Optional.ofNullable(outputSchema));
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
    public McpTool build() {
      return new McpTool(
          meta, annotations, description, icons, inputSchema, name, outputSchema, title);
    }
  }
}
