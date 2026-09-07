package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;

/**
 * Extends {@code MetaObject} with additional notification-specific fields. All key naming rules
 * from {@code MetaObject} apply.
 *
 * @param subscriptionId the optional subscription identifier
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpNotificationMetaObject(
    @JsonProperty("io.modelcontextprotocol/subscriptionId") Optional<JsonNode> subscriptionId) {
  /** Validates and copies the protocol fields. */
  public McpNotificationMetaObject {
    subscriptionId =
        Objects.requireNonNull(subscriptionId, "subscriptionId").map(JsonNode::deepCopy);
  }

  /**
   * Returns a copy of the optional subscription identifier.
   *
   * @return the subscription identifier
   */
  @Override
  public Optional<JsonNode> subscriptionId() {
    return subscriptionId.map(JsonNode::deepCopy);
  }

  /**
   * Creates a builder for {@link McpNotificationMetaObject}.
   *
   * @return a new builder
   */
  public static Builder mcpNotificationMetaObject() {
    return new Builder();
  }

  /** Builds {@link McpNotificationMetaObject} values. */
  public static final class Builder {
    private Optional<JsonNode> subscriptionId = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code subscriptionId}.
     *
     * @param subscriptionId the optional value
     * @return this builder
     */
    public Builder subscriptionId(Optional<JsonNode> subscriptionId) {
      this.subscriptionId = subscriptionId;
      return this;
    }

    /**
     * Sets {@code subscriptionId}.
     *
     * @param subscriptionId the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder subscriptionId(JsonNode subscriptionId) {
      return subscriptionId(Optional.ofNullable(subscriptionId));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpNotificationMetaObject build() {
      return new McpNotificationMetaObject(subscriptionId);
    }
  }
}
