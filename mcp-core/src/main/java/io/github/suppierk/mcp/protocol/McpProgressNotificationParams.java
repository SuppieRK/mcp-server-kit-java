package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Parameters for a {@link McpProgressNotification} ({@code notifications/progress}) notification.
 *
 * @param meta the optional protocol metadata
 * @param message An optional message describing the current progress.
 * @param progress The progress thus far. This should increase every time progress is made, even if
 *     the total is unknown.
 * @param progressToken The progress token which was given in the initial request, used to associate
 *     this notification with the request that is proceeding.
 * @param total Total number of items to process (or total progress required), if known.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpProgressNotificationParams(
    Optional<McpNotificationMetaObject> meta,
    Optional<String> message,
    Double progress,
    Object progressToken,
    Optional<Double> total) {
  /** Validates and copies the protocol fields. */
  public McpProgressNotificationParams {
    Objects.requireNonNull(meta, "meta");
    Objects.requireNonNull(message, "message");
    Objects.requireNonNull(progress, "progress");
    progressToken = McpProtocol.copy(Objects.requireNonNull(progressToken, "progressToken"));
    Objects.requireNonNull(total, "total");
  }

  /**
   * Returns a copy of the progress token.
   *
   * @return the copied token
   */
  public Object progressToken() {
    return McpProtocol.copy(progressToken);
  }

  /**
   * Creates a builder for {@link McpProgressNotificationParams}.
   *
   * @return a new builder
   */
  public static Builder mcpProgressNotificationParams() {
    return new Builder();
  }

  /** Builds {@link McpProgressNotificationParams} values. */
  public static final class Builder {
    private Optional<McpNotificationMetaObject> meta = Optional.empty();
    private Optional<String> message = Optional.empty();
    private Double progress;
    private Object progressToken;
    private Optional<Double> total = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpNotificationMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpNotificationMetaObject.Builder> configure) {
      var child = McpNotificationMetaObject.mcpNotificationMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

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
     * Sets {@code message}.
     *
     * @param message the optional value
     * @return this builder
     */
    public Builder message(Optional<String> message) {
      this.message = message;
      return this;
    }

    /**
     * Sets {@code message}.
     *
     * @param message the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder message(String message) {
      return message(Optional.ofNullable(message));
    }

    /**
     * Sets {@code progress}.
     *
     * @param progress the value
     * @return this builder
     */
    public Builder progress(Double progress) {
      this.progress = progress;
      return this;
    }

    /**
     * Sets {@code progressToken}.
     *
     * @param progressToken the value
     * @return this builder
     */
    public Builder progressToken(Object progressToken) {
      this.progressToken = progressToken;
      return this;
    }

    /**
     * Sets {@code total}.
     *
     * @param total the optional value
     * @return this builder
     */
    public Builder total(Optional<Double> total) {
      this.total = total;
      return this;
    }

    /**
     * Sets {@code total}.
     *
     * @param total the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder total(Double total) {
      return total(Optional.ofNullable(total));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpProgressNotificationParams build() {
      return new McpProgressNotificationParams(meta, message, progress, progressToken, total);
    }
  }
}
