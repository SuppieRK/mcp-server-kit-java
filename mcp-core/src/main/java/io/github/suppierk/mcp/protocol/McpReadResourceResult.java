package io.github.suppierk.mcp.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The result returned by the server for a {@link McpReadResourceRequest} ({@code resources/read})
 * request.
 *
 * @param meta the optional protocol metadata
 * @param cacheScope the cache scope. {@code public} permits reuse across authorization contexts.
 *     {@code private} limits reuse to the same authorization context.
 * @param contents the resource contents
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @param ttlMs the cache lifetime in milliseconds. Zero means immediately stale. A positive value
 *     specifies how long the result stays fresh. The value must not be negative.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpReadResourceResult(
    Optional<McpResultMetaObject> meta,
    String cacheScope,
    List<McpResourceContents> contents,
    String resultType,
    Long ttlMs)
    implements McpServerResult, McpReadResourceResultResponse.Result {
  /** Validates and copies the protocol fields. */
  public McpReadResourceResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(cacheScope, "cacheScope");
    contents = List.copyOf(contents);
    Objects.requireNonNull(resultType, "resultType");
    Objects.requireNonNull(ttlMs, "ttlMs");
  }

  /**
   * Creates an immediately stale private resource result.
   *
   * @param contents the resource contents
   */
  public McpReadResourceResult(List<McpResourceContents> contents) {
    this(Optional.empty(), "private", contents, "complete", 0L);
  }

  /**
   * Creates a builder for {@link McpReadResourceResult}.
   *
   * @return a new builder
   */
  public static Builder mcpReadResourceResult() {
    return new Builder();
  }

  /** Builds {@link McpReadResourceResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private String cacheScope;
    private List<McpResourceContents> contents;
    private String resultType;
    private Long ttlMs;

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
     * Appends {@code contents} using a {@link McpBlobResourceContents} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder blobResourceContents(Consumer<McpBlobResourceContents.Builder> configure) {
      var child = McpBlobResourceContents.mcpBlobResourceContents();
      configure.accept(child);
      var values = new ArrayList<>(this.contents == null ? List.of() : this.contents);
      values.add(child.build());
      return contents(values);
    }

    /**
     * Appends {@code contents} using a {@link McpTextResourceContents} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder textResourceContents(Consumer<McpTextResourceContents.Builder> configure) {
      var child = McpTextResourceContents.mcpTextResourceContents();
      configure.accept(child);
      var values = new ArrayList<>(this.contents == null ? List.of() : this.contents);
      values.add(child.build());
      return contents(values);
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
     * Sets {@code cacheScope}.
     *
     * @param cacheScope the value
     * @return this builder
     */
    public Builder cacheScope(String cacheScope) {
      this.cacheScope = cacheScope;
      return this;
    }

    /**
     * Sets {@code contents}.
     *
     * @param contents the value
     * @return this builder
     */
    public Builder contents(List<McpResourceContents> contents) {
      this.contents = contents;
      return this;
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
     * Sets {@code ttlMs}.
     *
     * @param ttlMs the value
     * @return this builder
     */
    public Builder ttlMs(Long ttlMs) {
      this.ttlMs = ttlMs;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpReadResourceResult build() {
      return new McpReadResourceResult(meta, cacheScope, contents, resultType, ttlMs);
    }
  }
}
