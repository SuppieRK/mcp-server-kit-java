package io.github.suppierk.mcp.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The result of a tool use, provided by the user back to the assistant.
 *
 * @param meta Optional metadata about the tool result. Clients SHOULD preserve this field when
 *     including tool results in subsequent sampling requests to enable caching optimizations.
 * @param content The unstructured result content of the tool use. This has the same format as
 *     {@code CallToolResult.content} and can include text, images, audio, resource links, and
 *     embedded resources.
 * @param isError Whether the tool use resulted in an error. If true, the content typically
 *     describes the error that occurred. Default: false
 * @param structuredContent An optional structured result value. This can be any JSON value (object,
 *     array, string, number, boolean, or null). If the tool defined an {@code Tool.outputSchema},
 *     this SHOULD conform to that schema.
 * @param toolUseId The ID of the tool use this result corresponds to. This MUST match the ID from a
 *     previous {@code ToolUseContent}.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpToolResultContent(
    Optional<McpMetaObject> meta,
    List<McpContentBlock> content,
    Optional<Boolean> isError,
    Optional<Object> structuredContent,
    String toolUseId)
    implements McpSamplingMessageContentBlock {

  private static final String TYPE = "tool_result";

  /** Validates and copies the protocol fields. */
  public McpToolResultContent {
    Objects.requireNonNull(meta, "meta");
    content = List.copyOf(content);
    Objects.requireNonNull(isError, "isError");
    structuredContent = McpProtocol.copy(structuredContent);
    Objects.requireNonNull(toolUseId, "toolUseId");
  }

  /**
   * Returns a copy of the optional structured content.
   *
   * @return the copied structured content
   */
  public Optional<Object> structuredContent() {
    return McpProtocol.copy(structuredContent);
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
   * Creates a builder for {@link McpToolResultContent}.
   *
   * @return a new builder
   */
  public static Builder mcpToolResultContent() {
    return new Builder();
  }

  /** Builds {@link McpToolResultContent} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private List<McpContentBlock> content;
    private Optional<Boolean> isError = Optional.empty();
    private Optional<Object> structuredContent = Optional.empty();
    private String toolUseId;

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpMetaObject.Builder> configure) {
      var child = McpMetaObject.mcpMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Appends {@code content} using a {@link McpAudioContent} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder audioContent(Consumer<McpAudioContent.Builder> configure) {
      var child = McpAudioContent.mcpAudioContent();
      configure.accept(child);
      var values = new ArrayList<>(this.content == null ? List.of() : this.content);
      values.add(child.build());
      return content(values);
    }

    /**
     * Appends {@code content} using a {@link McpEmbeddedResource} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder embeddedResource(Consumer<McpEmbeddedResource.Builder> configure) {
      var child = McpEmbeddedResource.mcpEmbeddedResource();
      configure.accept(child);
      var values = new ArrayList<>(this.content == null ? List.of() : this.content);
      values.add(child.build());
      return content(values);
    }

    /**
     * Appends {@code content} using a {@link McpImageContent} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder imageContent(Consumer<McpImageContent.Builder> configure) {
      var child = McpImageContent.mcpImageContent();
      configure.accept(child);
      var values = new ArrayList<>(this.content == null ? List.of() : this.content);
      values.add(child.build());
      return content(values);
    }

    /**
     * Appends {@code content} using a {@link McpResourceLink} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder resourceLink(Consumer<McpResourceLink.Builder> configure) {
      var child = McpResourceLink.mcpResourceLink();
      configure.accept(child);
      var values = new ArrayList<>(this.content == null ? List.of() : this.content);
      values.add(child.build());
      return content(values);
    }

    /**
     * Appends {@code content} using a {@link McpTextContent} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder textContent(Consumer<McpTextContent.Builder> configure) {
      var child = McpTextContent.mcpTextContent();
      configure.accept(child);
      var values = new ArrayList<>(this.content == null ? List.of() : this.content);
      values.add(child.build());
      return content(values);
    }

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
     * Sets {@code content}.
     *
     * @param content the value
     * @return this builder
     */
    public Builder content(List<McpContentBlock> content) {
      this.content = content;
      return this;
    }

    /**
     * Sets {@code isError}.
     *
     * @param isError the optional value
     * @return this builder
     */
    public Builder isError(Optional<Boolean> isError) {
      this.isError = isError;
      return this;
    }

    /**
     * Sets {@code isError}.
     *
     * @param isError the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder isError(Boolean isError) {
      return isError(Optional.ofNullable(isError));
    }

    /**
     * Sets {@code structuredContent}.
     *
     * @param structuredContent the optional value
     * @return this builder
     */
    public Builder structuredContent(Optional<Object> structuredContent) {
      this.structuredContent = structuredContent;
      return this;
    }

    /**
     * Sets {@code structuredContent}.
     *
     * @param structuredContent the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder structuredContent(Object structuredContent) {
      return structuredContent(Optional.ofNullable(structuredContent));
    }

    /**
     * Sets {@code toolUseId}.
     *
     * @param toolUseId the value
     * @return this builder
     */
    public Builder toolUseId(String toolUseId) {
      this.toolUseId = toolUseId;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpToolResultContent build() {
      return new McpToolResultContent(meta, content, isError, structuredContent, toolUseId);
    }
  }
}
