package io.github.suppierk.mcp.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The result returned by the client for a {@link McpListRootsRequest} ({@code roots/list}) request.
 * This result contains an array of {@code Root} objects, each representing a root directory or file
 * that the server can operate on.
 *
 * @param roots the listed roots
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpListRootsResult(List<McpRoot> roots) implements McpInputResponse {
  /** Validates and copies the protocol fields. */
  public McpListRootsResult {
    roots = List.copyOf(roots);
  }

  /**
   * Creates a builder for {@link McpListRootsResult}.
   *
   * @return a new builder
   */
  public static Builder mcpListRootsResult() {
    return new Builder();
  }

  /** Builds {@link McpListRootsResult} values. */
  public static final class Builder {
    private List<McpRoot> roots;

    private Builder() {}

    /**
     * Appends {@code roots} using a {@link McpRoot} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder root(Consumer<McpRoot.Builder> configure) {
      var child = McpRoot.mcpRoot();
      configure.accept(child);
      var values = new ArrayList<>(this.roots == null ? List.of() : this.roots);
      values.add(child.build());
      return roots(values);
    }

    /**
     * Sets {@code roots}.
     *
     * @param roots the value
     * @return this builder
     */
    public Builder roots(List<McpRoot> roots) {
      this.roots = roots;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpListRootsResult build() {
      return new McpListRootsResult(roots);
    }
  }
}
