package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The result returned by the server for a {@link McpDiscoverRequest} ({@code server/discover})
 * request.
 *
 * @param meta the optional protocol metadata
 * @param cacheScope the cache scope. {@code public} permits reuse across authorization contexts.
 *     {@code private} limits reuse to the same authorization context.
 * @param capabilities The capabilities of the server.
 * @param instructions Natural-language guidance describing the server and its features. This can be
 *     used by clients to improve an LLM's understanding of available tools (e.g., by including it
 *     in a system prompt). It should focus on information that helps the model use the server
 *     effectively and should not duplicate information already in tool descriptions.
 * @param resultType Indicates the type of the result, which allows the client to determine how to
 *     parse the result object. Servers implementing this protocol version MUST include this field.
 *     For backward compatibility, when a client receives a result from a server implementing an
 *     earlier protocol version (which does not include {@code resultType}), the client MUST treat
 *     the absent field as {@code "complete"}.
 * @param supportedVersions MCP Protocol Versions this server supports. The client should choose a
 *     version from this list for use in subsequent requests.
 * @param ttlMs the cache lifetime in milliseconds. Zero means immediately stale. A positive value
 *     specifies how long the result stays fresh. The value must not be negative.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpDiscoverResult(
    @JsonProperty("_meta") Optional<McpResultMetaObject> meta,
    String cacheScope,
    McpServerCapabilities capabilities,
    Optional<String> instructions,
    String resultType,
    List<String> supportedVersions,
    Long ttlMs)
    implements McpServerResult {
  /** Validates and copies the protocol fields. */
  public McpDiscoverResult {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(cacheScope, "cacheScope");
    Objects.requireNonNull(capabilities, "capabilities");
    Objects.requireNonNull(instructions, "instructions");
    Objects.requireNonNull(resultType, "resultType");
    supportedVersions = List.copyOf(supportedVersions);
    Objects.requireNonNull(ttlMs, "ttlMs");
  }

  /**
   * Creates a builder for {@link McpDiscoverResult}.
   *
   * @return a new builder
   */
  public static Builder mcpDiscoverResult() {
    return new Builder();
  }

  /** Builds {@link McpDiscoverResult} values. */
  public static final class Builder {
    private Optional<McpResultMetaObject> meta = Optional.empty();
    private String cacheScope;
    private McpServerCapabilities capabilities;
    private Optional<String> instructions = Optional.empty();
    private String resultType;
    private List<String> supportedVersions;
    private Long ttlMs;

    private Builder() {}

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
     * Sets {@code capabilities}.
     *
     * @param capabilities the value
     * @return this builder
     */
    public Builder capabilities(McpServerCapabilities capabilities) {
      this.capabilities = capabilities;
      return this;
    }

    /**
     * Sets {@code instructions}.
     *
     * @param instructions the optional value
     * @return this builder
     */
    public Builder instructions(Optional<String> instructions) {
      this.instructions = instructions;
      return this;
    }

    /**
     * Sets {@code instructions}.
     *
     * @param instructions the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder instructions(String instructions) {
      return instructions(Optional.ofNullable(instructions));
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
     * Sets {@code supportedVersions}.
     *
     * @param supportedVersions the value
     * @return this builder
     */
    public Builder supportedVersions(List<String> supportedVersions) {
      this.supportedVersions = supportedVersions;
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
    public McpDiscoverResult build() {
      return new McpDiscoverResult(
          meta, cacheScope, capabilities, instructions, resultType, supportedVersions, ttlMs);
    }
  }
}
