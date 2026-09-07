package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * Parameters for a {@code notifications/cancelled} notification.
 *
 * @param meta the optional protocol metadata
 * @param reason An optional string describing the reason for the cancellation. This MAY be logged
 *     or presented to the user.
 * @param requestId The ID of the request to cancel. This MUST correspond to the ID of a request the
 *     client previously issued.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCancelledNotificationParams(
    Optional<McpNotificationMetaObject> meta, Optional<String> reason, Object requestId) {
  /** Validates and copies the protocol fields. */
  public McpCancelledNotificationParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(reason, "reason");
    requestId = McpProtocol.copy(Objects.requireNonNull(requestId, "requestId"));
  }

  /**
   * Returns a copy of the request identifier.
   *
   * @return the copied identifier
   */
  public Object requestId() {
    return McpProtocol.copy(requestId);
  }

  /**
   * Creates a builder for {@link McpCancelledNotificationParams}.
   *
   * @return a new builder
   */
  public static Builder mcpCancelledNotificationParams() {
    return new Builder();
  }

  /** Builds {@link McpCancelledNotificationParams} values. */
  public static final class Builder {
    private Optional<McpNotificationMetaObject> meta = Optional.empty();
    private Optional<String> reason = Optional.empty();
    private Object requestId;

    private Builder() {}

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpNotificationMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpNotificationMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Sets {@code reason}.
     *
     * @param reason the optional value
     * @return this builder
     */
    public Builder reason(Optional<String> reason) {
      this.reason = reason;
      return this;
    }

    /**
     * Sets {@code reason}.
     *
     * @param reason the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder reason(String reason) {
      return reason(Optional.ofNullable(reason));
    }

    /**
     * Sets {@code requestId}.
     *
     * @param requestId the value
     * @return this builder
     */
    public Builder requestId(Object requestId) {
      this.requestId = requestId;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCancelledNotificationParams build() {
      return new McpCancelledNotificationParams(meta, reason, requestId);
    }
  }
}
