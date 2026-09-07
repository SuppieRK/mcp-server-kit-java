package io.github.suppierk.mcp.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Notification types selected by a {@link McpSubscriptionsListenRequest} ({@code
 * subscriptions/listen}) request. The server must not send a type that the client did not select.
 *
 * @param promptsListChanged whether to receive {@link McpPromptListChangedNotification}
 * @param resourceSubscriptions resource URIs for {@link McpResourceUpdatedNotification}
 *     notifications
 * @param resourcesListChanged whether to receive {@link McpResourceListChangedNotification}
 * @param toolsListChanged whether to receive {@link McpToolListChangedNotification}
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpSubscriptionFilter(
    Optional<Boolean> promptsListChanged,
    Optional<List<String>> resourceSubscriptions,
    Optional<Boolean> resourcesListChanged,
    Optional<Boolean> toolsListChanged) {
  /** Validates and copies the protocol fields. */
  public McpSubscriptionFilter {
    Objects.requireNonNull(promptsListChanged, "promptsListChanged");
    resourceSubscriptions =
        Objects.requireNonNull(resourceSubscriptions, "resourceSubscriptions").map(List::copyOf);
    Objects.requireNonNull(resourcesListChanged, "resourcesListChanged");
    Objects.requireNonNull(toolsListChanged, "toolsListChanged");
  }

  /**
   * Creates a builder for {@link McpSubscriptionFilter}.
   *
   * @return a new builder
   */
  public static Builder mcpSubscriptionFilter() {
    return new Builder();
  }

  /** Builds {@link McpSubscriptionFilter} values. */
  public static final class Builder {
    private Optional<Boolean> promptsListChanged = Optional.empty();
    private Optional<List<String>> resourceSubscriptions = Optional.empty();
    private Optional<Boolean> resourcesListChanged = Optional.empty();
    private Optional<Boolean> toolsListChanged = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code promptsListChanged}.
     *
     * @param promptsListChanged the optional value
     * @return this builder
     */
    public Builder promptsListChanged(Optional<Boolean> promptsListChanged) {
      this.promptsListChanged = promptsListChanged;
      return this;
    }

    /**
     * Sets {@code promptsListChanged}.
     *
     * @param promptsListChanged the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder promptsListChanged(Boolean promptsListChanged) {
      return promptsListChanged(Optional.ofNullable(promptsListChanged));
    }

    /**
     * Sets {@code resourceSubscriptions}.
     *
     * @param resourceSubscriptions the optional value
     * @return this builder
     */
    public Builder resourceSubscriptions(Optional<List<String>> resourceSubscriptions) {
      this.resourceSubscriptions = resourceSubscriptions;
      return this;
    }

    /**
     * Sets {@code resourceSubscriptions}.
     *
     * @param resourceSubscriptions the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder resourceSubscriptions(List<String> resourceSubscriptions) {
      return resourceSubscriptions(Optional.ofNullable(resourceSubscriptions));
    }

    /**
     * Sets {@code resourcesListChanged}.
     *
     * @param resourcesListChanged the optional value
     * @return this builder
     */
    public Builder resourcesListChanged(Optional<Boolean> resourcesListChanged) {
      this.resourcesListChanged = resourcesListChanged;
      return this;
    }

    /**
     * Sets {@code resourcesListChanged}.
     *
     * @param resourcesListChanged the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder resourcesListChanged(Boolean resourcesListChanged) {
      return resourcesListChanged(Optional.ofNullable(resourcesListChanged));
    }

    /**
     * Sets {@code toolsListChanged}.
     *
     * @param toolsListChanged the optional value
     * @return this builder
     */
    public Builder toolsListChanged(Optional<Boolean> toolsListChanged) {
      this.toolsListChanged = toolsListChanged;
      return this;
    }

    /**
     * Sets {@code toolsListChanged}.
     *
     * @param toolsListChanged the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder toolsListChanged(Boolean toolsListChanged) {
      return toolsListChanged(Optional.ofNullable(toolsListChanged));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpSubscriptionFilter build() {
      return new McpSubscriptionFilter(
          promptsListChanged, resourceSubscriptions, resourcesListChanged, toolsListChanged);
    }
  }
}
