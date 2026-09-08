package io.github.suppierk.mcp.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A prompt or prompt template that the server offers.
 *
 * @param meta the optional protocol metadata
 * @param arguments the optional prompt arguments
 * @param description the optional prompt description
 * @param icons optional icons for a user interface
 * @param name the programmatic prompt name and fallback display name
 * @param title the optional human-readable title for a user interface
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpPrompt(
    Optional<McpMetaObject> meta,
    Optional<List<McpPromptArgument>> arguments,
    Optional<String> description,
    Optional<List<McpIcon>> icons,
    String name,
    Optional<String> title)
    implements McpBaseMetadata {
  /** Validates and copies the protocol fields. */
  public McpPrompt {
    Objects.requireNonNull(meta, "meta");
    arguments = Objects.requireNonNull(arguments, "arguments").map(List::copyOf);
    Objects.requireNonNull(description, "description");
    icons = Objects.requireNonNull(icons, "icons").map(List::copyOf);
    if (Objects.requireNonNull(name, "name").isBlank()) {
      throw new IllegalArgumentException("A prompt name must not be blank");
    }
    Objects.requireNonNull(title, "title");
  }

  /**
   * Creates a minimal prompt declaration.
   *
   * @param name the prompt name
   */
  public McpPrompt(String name) {
    this(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        name,
        Optional.empty());
  }

  /**
   * Creates a builder for {@link McpPrompt}.
   *
   * @return a new builder
   */
  public static Builder mcpPrompt() {
    return new Builder();
  }

  /** Builds {@link McpPrompt} values. */
  public static final class Builder {
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<List<McpPromptArgument>> arguments = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<List<McpIcon>> icons = Optional.empty();
    private String name;
    private Optional<String> title = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpMetaObject.Builder> configure) {
      var child = McpMetaObject.mcpMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Appends {@code arguments} using a {@link McpPromptArgument} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder promptArgument(Consumer<McpPromptArgument.Builder> configure) {
      var child = McpPromptArgument.mcpPromptArgument();
      configure.accept(child);
      var values = new ArrayList<>(this.arguments.orElseGet(List::of));
      values.add(child.build());
      return arguments(values);
    }

    /**
     * Appends {@code icons} using a {@link McpIcon} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder icon(Consumer<McpIcon.Builder> configure) {
      var child = McpIcon.mcpIcon();
      configure.accept(child);
      var values = new ArrayList<>(this.icons.orElseGet(List::of));
      values.add(child.build());
      return icons(values);
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the optional value
     * @return this builder
     */
    public Builder meta(Optional<McpMetaObject> meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder meta(McpMetaObject meta) {
      return meta(Optional.ofNullable(meta));
    }

    /**
     * Sets {@code arguments}.
     *
     * @param arguments the optional value
     * @return this builder
     */
    public Builder arguments(Optional<List<McpPromptArgument>> arguments) {
      this.arguments = arguments;
      return this;
    }

    /**
     * Sets {@code arguments}.
     *
     * @param arguments the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder arguments(List<McpPromptArgument> arguments) {
      return arguments(Optional.ofNullable(arguments));
    }

    /**
     * Sets {@code description}.
     *
     * @param description the optional value
     * @return this builder
     */
    public Builder description(Optional<String> description) {
      this.description = description;
      return this;
    }

    /**
     * Sets {@code description}.
     *
     * @param description the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder description(String description) {
      return description(Optional.ofNullable(description));
    }

    /**
     * Sets {@code icons}.
     *
     * @param icons the optional value
     * @return this builder
     */
    public Builder icons(Optional<List<McpIcon>> icons) {
      this.icons = icons;
      return this;
    }

    /**
     * Sets {@code icons}.
     *
     * @param icons the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder icons(List<McpIcon> icons) {
      return icons(Optional.ofNullable(icons));
    }

    /**
     * Sets {@code name}.
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code title}.
     *
     * @param title the optional value
     * @return this builder
     */
    public Builder title(Optional<String> title) {
      this.title = title;
      return this;
    }

    /**
     * Sets {@code title}.
     *
     * @param title the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder title(String title) {
      return title(Optional.ofNullable(title));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpPrompt build() {
      return new McpPrompt(meta, arguments, description, icons, name, title);
    }
  }
}
