package io.github.suppierk.mcp.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The result returned by the server for a {@link McpCallToolRequest} ({@code tools/call}) request.
 *
 * @param meta the optional protocol metadata
 * @param content A list of content objects that represent the unstructured result of the tool call.
 * @param isError Whether the tool call ended in an error. If not set, this is assumed to be false
 *     (the call was successful). Any errors that originate from the tool SHOULD be reported inside
 *     the result object with {@code isError} set to true. It must not use an MCP protocol error
 *     response. This lets the model inspect and correct the tool error. An error that occurs while
 *     the server finds the tool, an unsupported tool call, or another exceptional condition must
 *     use an MCP error response.
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @param structuredContent An optional JSON value that represents the structured result of the tool
 *     call. This can be any JSON value (object, array, string, number, boolean, or null) that
 *     conforms to the tool's outputSchema if one is defined.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCallToolResult(
    Optional<McpResultMetaObject> meta,
    List<McpContentBlock> content,
    Optional<Boolean> isError,
    String resultType,
    Optional<Object> structuredContent)
    implements McpServerResult, McpCallToolResultResponse.Result {
  /** Validates and copies the protocol fields. */
  public McpCallToolResult {
    Objects.requireNonNull(meta, "meta");
    content = List.copyOf(content);
    Objects.requireNonNull(isError, "isError");
    Objects.requireNonNull(resultType, "resultType");
    structuredContent = McpProtocol.copy(structuredContent);
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
   * Creates a complete tool result.
   *
   * @param content the result content
   */
  public McpCallToolResult(List<McpContentBlock> content) {
    this(Optional.empty(), content, Optional.empty(), "complete", Optional.empty());
  }

  /**
   * Creates a complete tool result with structured content.
   *
   * @param content the result content
   * @param structuredContent the structured result
   */
  public McpCallToolResult(List<McpContentBlock> content, Object structuredContent) {
    this(
        Optional.empty(),
        content,
        Optional.empty(),
        "complete",
        Optional.of(
            McpProtocol.copy(Objects.requireNonNull(structuredContent, "structuredContent"))));
  }

  /**
   * Creates a builder for {@link McpCallToolResult}.
   *
   * @return a new builder
   */
  public static Builder mcpCallToolResult() {
    return new Builder();
  }

  /** Builds {@link McpCallToolResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private List<McpContentBlock> content;
    private Optional<Boolean> isError = Optional.empty();
    private String resultType;
    private Optional<Object> structuredContent = Optional.empty();

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
     * Builds the value.
     *
     * @return the built value
     */
    public McpCallToolResult build() {
      return new McpCallToolResult(meta, content, isError, resultType, structuredContent);
    }
  }
}
