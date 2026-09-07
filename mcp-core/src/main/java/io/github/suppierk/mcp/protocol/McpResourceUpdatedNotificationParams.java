package io.github.suppierk.mcp.protocol;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Parameters for a {@code notifications/resources/updated} notification.
 *
 * @param meta the optional protocol metadata
 * @param uri The URI of the resource that has been updated. This might be a sub-resource of the one
 *     that the client actually subscribed to.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpResourceUpdatedNotificationParams(
    Optional<McpNotificationMetaObject> meta, URI uri) {
  /** Validates and copies the protocol fields. */
  public McpResourceUpdatedNotificationParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(uri, "uri");
  }

  /**
   * Creates a builder for {@link McpResourceUpdatedNotificationParams}.
   *
   * @return a new builder
   */
  public static Builder mcpResourceUpdatedNotificationParams() {
    return new Builder();
  }

  /** Builds {@link McpResourceUpdatedNotificationParams} values. */
  public static final class Builder {
    private Optional<McpNotificationMetaObject> meta = Optional.empty();
    private URI uri;

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
     * Sets {@code uri}.
     *
     * @param uri the value
     * @return this builder
     */
    public Builder uri(URI uri) {
      this.uri = uri;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpResourceUpdatedNotificationParams build() {
      return new McpResourceUpdatedNotificationParams(meta, uri);
    }
  }
}
