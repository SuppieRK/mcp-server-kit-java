package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Metadata for a {@link McpSubscriptionsListenResult}. It contains the subscription stream
 * identifier and follows the {@link McpMetaObject} key rules.
 *
 * @param serverInfo the optional server information
 * @param subscriptionId the subscription identifier
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionsListenResultMetaObject(
    Optional<McpImplementation> serverInfo, Object subscriptionId) {
  /** Validates and copies the protocol fields. */
  public McpSubscriptionsListenResultMetaObject {
    Objects.requireNonNull(serverInfo, "serverInfo");
    subscriptionId = McpProtocol.copy(Objects.requireNonNull(subscriptionId, "subscriptionId"));
  }

  /**
   * Returns a copy of the subscription identifier.
   *
   * @return the copied identifier
   */
  public Object subscriptionId() {
    return McpProtocol.copy(subscriptionId);
  }

  /**
   * Creates a builder for {@link McpSubscriptionsListenResultMetaObject}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionsListenResultMetaObject() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionsListenResultMetaObject} values. */
  public static final class Builder {
    private Optional<McpImplementation> serverInfo = Optional.empty();
    private Object subscriptionId;

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
     * Sets {@code subscriptionId}.
     *
     * @param subscriptionId the value
     * @return this builder
     */
    public Builder subscriptionId(Object subscriptionId) {
      this.subscriptionId = subscriptionId;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpSubscriptionsListenResultMetaObject build() {
      return new McpSubscriptionsListenResultMetaObject(serverInfo, subscriptionId);
    }
  }
}
