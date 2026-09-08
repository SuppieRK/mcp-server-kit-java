package io.github.suppierk.mcp.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A map of server-initiated requests that the client must fulfill. Keys are server-assigned
 * identifiers; values are the request objects.
 *
 * @param values the protocol entries
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpInputRequests(Map<String, McpInputRequest> values) {
  /** Validates and copies the entries. */
  public McpInputRequests {
    Objects.requireNonNull(values, "values");
    LinkedHashMap<String, McpInputRequest> copy = new LinkedHashMap<>();
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
  public Map<String, McpInputRequest> values() {
    return values;
  }

  /**
   * Creates a builder for {@link McpInputRequests}.
   *
   * @return a new builder
   */
  public static Builder mcpInputRequests() {
    return new Builder();
  }

  /** Builds {@link McpInputRequests} values. */
  public static final class Builder {
    private Map<String, McpInputRequest> values;

    private Builder() {}

    /**
     * Adds {@code values} using a {@link McpCreateMessageRequest} builder.
     *
     * @param key the entry key
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder createMessageRequest(
        String key, Consumer<McpCreateMessageRequest.Builder> configure) {
      var child = McpCreateMessageRequest.mcpCreateMessageRequest();
      configure.accept(child);
      var entries = new LinkedHashMap<>(this.values == null ? Map.of() : this.values);
      entries.put(Objects.requireNonNull(key, "key"), child.build());
      return values(entries);
    }

    /**
     * Adds {@code values} using a {@link McpElicitRequest} builder.
     *
     * @param key the entry key
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder elicitRequest(String key, Consumer<McpElicitRequest.Builder> configure) {
      var child = McpElicitRequest.mcpElicitRequest();
      configure.accept(child);
      var entries = new LinkedHashMap<>(this.values == null ? Map.of() : this.values);
      entries.put(Objects.requireNonNull(key, "key"), child.build());
      return values(entries);
    }

    /**
     * Adds {@code values} using a {@link McpListRootsRequest} builder.
     *
     * @param key the entry key
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder listRootsRequest(String key, Consumer<McpListRootsRequest.Builder> configure) {
      var child = McpListRootsRequest.mcpListRootsRequest();
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
    public Builder values(Map<String, McpInputRequest> values) {
      this.values = values;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpInputRequests build() {
      return new McpInputRequests(values);
    }
  }
}
