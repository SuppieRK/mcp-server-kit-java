package io.github.suppierk.mcp.protocol;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Parameters for a {@code notifications/message} notification.
 *
 * @param meta the optional protocol metadata
 * @param data The data to be logged, such as a string message or an object. Any JSON serializable
 *     type is allowed here.
 * @param level The severity of this log message.
 * @param logger An optional name of the logger issuing this message.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpLoggingMessageNotificationParams(
    Optional<McpNotificationMetaObject> meta,
    Object data,
    McpLoggingLevel level,
    Optional<String> logger) {
  /** Validates and copies the protocol fields. */
  public McpLoggingMessageNotificationParams {
    Objects.requireNonNull(meta, "meta");
    data = McpProtocol.copy(Objects.requireNonNull(data, "data"));
    Objects.requireNonNull(level, "level");
    Objects.requireNonNull(logger, "logger");
  }

  /**
   * Returns a copy of the log data.
   *
   * @return the copied log data
   */
  public Object data() {
    return McpProtocol.copy(data);
  }

  /**
   * Creates a builder for {@link McpLoggingMessageNotificationParams}.
   *
   * @return a new builder
   */
  public static Builder mcpLoggingMessageNotificationParams() {
    return new Builder();
  }

  /** Builds {@link McpLoggingMessageNotificationParams} values. */
  public static final class Builder {
    private Optional<McpNotificationMetaObject> meta = Optional.empty();
    private Object data;
    private McpLoggingLevel level;
    private Optional<String> logger = Optional.empty();

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
     * Sets {@code data}.
     *
     * @param data the value
     * @return this builder
     */
    public Builder data(Object data) {
      this.data = data;
      return this;
    }

    /**
     * Sets {@code level}.
     *
     * @param level the value
     * @return this builder
     */
    public Builder level(McpLoggingLevel level) {
      this.level = level;
      return this;
    }

    /**
     * Sets {@code logger}.
     *
     * @param logger the optional value
     * @return this builder
     */
    public Builder logger(Optional<String> logger) {
      this.logger = logger;
      return this;
    }

    /**
     * Sets {@code logger}.
     *
     * @param logger the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder logger(String logger) {
      return logger(Optional.ofNullable(logger));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpLoggingMessageNotificationParams build() {
      return new McpLoggingMessageNotificationParams(meta, data, level, logger);
    }
  }
}
