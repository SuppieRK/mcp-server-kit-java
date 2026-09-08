package io.github.suppierk.mcp.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Client responses to server-initiated input requests.
 *
 * @param values results keyed by the matching {@link McpInputRequests} entry
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpInputResponses(Map<String, McpInputResponse> values) {
  /** Validates and copies the entries. */
  public McpInputResponses {
    Objects.requireNonNull(values, "values");
    LinkedHashMap<String, McpInputResponse> copy = new LinkedHashMap<>();
    values.forEach(
        (key, value) ->
            copy.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, key)));
    values = Collections.unmodifiableMap(copy);
  }

  /**
   * Gets the immutable protocol entries.
   *
   * @return the entries
   */
  @Override
  public Map<String, McpInputResponse> values() {
    return values;
  }

  /**
   * Creates a builder for {@link McpInputResponses}.
   *
   * @return a new builder
   */
  public static Builder mcpInputResponses() {
    return new Builder();
  }

  /** Builds {@link McpInputResponses} values. */
  public static final class Builder {
    private Map<String, McpInputResponse> values;

    private Builder() {}

    /**
     * Adds {@code values} using a {@link McpCreateMessageResult} builder.
     *
     * @param key the entry key
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder createMessageResult(
        String key, Consumer<McpCreateMessageResult.Builder> configure) {
      var child = McpCreateMessageResult.mcpCreateMessageResult();
      configure.accept(child);
      var entries = new LinkedHashMap<>(this.values == null ? Map.of() : this.values);
      entries.put(Objects.requireNonNull(key, "key"), child.build());
      return values(entries);
    }

    /**
     * Adds {@code values} using a {@link McpElicitResult} builder.
     *
     * @param key the entry key
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder elicitResult(String key, Consumer<McpElicitResult.Builder> configure) {
      var child = McpElicitResult.mcpElicitResult();
      configure.accept(child);
      var entries = new LinkedHashMap<>(this.values == null ? Map.of() : this.values);
      entries.put(Objects.requireNonNull(key, "key"), child.build());
      return values(entries);
    }

    /**
     * Adds {@code values} using a {@link McpListRootsResult} builder.
     *
     * @param key the entry key
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder listRootsResult(String key, Consumer<McpListRootsResult.Builder> configure) {
      var child = McpListRootsResult.mcpListRootsResult();
      configure.accept(child);
      var entries = new LinkedHashMap<>(this.values == null ? Map.of() : this.values);
      entries.put(Objects.requireNonNull(key, "key"), child.build());
      return values(entries);
    }

    /**
     * Sets {@code values}.
     *
     * @param values the value
     * @return this builder
     */
    public Builder values(Map<String, McpInputResponse> values) {
      this.values = values;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpInputResponses build() {
      return new McpInputResponses(values);
    }
  }
}
