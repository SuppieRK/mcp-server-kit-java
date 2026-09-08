package io.github.suppierk.mcp.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The result returned by the server for a {@link McpListPromptsRequest} ({@code prompts/list})
 * request.
 *
 * @param meta the optional protocol metadata
 * @param cacheScope the cache scope. {@code public} permits reuse across authorization contexts.
 *     {@code private} limits reuse to the same authorization context.
 * @param nextCursor An opaque token representing the pagination position after the last returned
 *     result. If present, there may be more results available.
 * @param prompts the listed prompts
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @param ttlMs the cache lifetime in milliseconds. Zero means immediately stale. A positive value
 *     specifies how long the result stays fresh. The value must not be negative.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListPromptsResult(
    Optional<McpResultMetaObject> meta,
    String cacheScope,
    Optional<String> nextCursor,
    List<McpPrompt> prompts,
    String resultType,
    Long ttlMs)
    implements McpServerResult {
  /** Validates and copies the protocol fields. */
  public McpListPromptsResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(cacheScope, "cacheScope");
    Objects.requireNonNull(nextCursor, "nextCursor");
    prompts = List.copyOf(prompts);
    Objects.requireNonNull(resultType, "resultType");
    Objects.requireNonNull(ttlMs, "ttlMs");
  }

  /**
   * Creates a builder for {@link McpListPromptsResult}.
   *
   * @return a new builder
   */
  public static Builder mcpListPromptsResult() {
    return new Builder();
  }

  /** Builds {@link McpListPromptsResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private String cacheScope;
    private Optional<String> nextCursor = Optional.empty();
    private List<McpPrompt> prompts;
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
     * Appends {@code prompts} using a {@link McpPrompt} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder prompt(Consumer<McpPrompt.Builder> configure) {
      var child = McpPrompt.mcpPrompt();
      configure.accept(child);
      var values = new ArrayList<>(this.prompts == null ? List.of() : this.prompts);
      values.add(child.build());
      return prompts(values);
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
     * Sets {@code nextCursor}.
     *
     * @param nextCursor the optional value
     * @return this builder
     */
    public Builder nextCursor(Optional<String> nextCursor) {
      this.nextCursor = nextCursor;
      return this;
    }

    /**
     * Sets {@code nextCursor}.
     *
     * @param nextCursor the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder nextCursor(String nextCursor) {
      return nextCursor(Optional.ofNullable(nextCursor));
    }

    /**
     * Sets {@code prompts}.
     *
     * @param prompts the value
     * @return this builder
     */
    public Builder prompts(List<McpPrompt> prompts) {
      this.prompts = prompts;
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
    public McpListPromptsResult build() {
      return new McpListPromptsResult(meta, cacheScope, nextCursor, prompts, resultType, ttlMs);
    }
  }
}
