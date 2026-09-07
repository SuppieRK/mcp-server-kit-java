package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Extends {@code MetaObject} with additional result-specific fields. All key naming rules from
 * {@code MetaObject} apply.
 *
 * @param serverInfo the optional server information
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResultMetaObject(Optional<McpImplementation> serverInfo) {
  /** Validates and copies the protocol fields. */
  public McpResultMetaObject {
    Objects.requireNonNull(serverInfo, "serverInfo");
  }

  /**
   * Creates a builder for {@link McpResultMetaObject}.
   *
   * @return a new builder
   */
  public static Builder mcpResultMetaObject() {
    return new Builder();
  }

  /** Builds {@link McpResultMetaObject} values. */
  public static final class Builder {
    private Optional<McpImplementation> serverInfo = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code serverInfo}.
     *
     * @param serverInfo the optional value
     * @return this builder
     */
    public Builder serverInfo(Optional<McpImplementation> serverInfo) {
      this.serverInfo = serverInfo;
      return this;
    }

    /**
     * Sets {@code serverInfo}.
     *
     * @param serverInfo the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder serverInfo(McpImplementation serverInfo) {
      return serverInfo(Optional.ofNullable(serverInfo));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpResultMetaObject build() {
      return new McpResultMetaObject(serverInfo);
    }
  }
}
