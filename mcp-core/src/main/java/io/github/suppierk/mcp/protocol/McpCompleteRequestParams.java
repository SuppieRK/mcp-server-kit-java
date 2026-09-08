package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Parameters for a {@code completion/complete} request.
 *
 * @param meta the optional protocol metadata
 * @param argument The argument's information
 * @param context Additional, optional context for completions
 * @param ref the completion target
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpCompleteRequestParams(
    McpRequestMetaObject meta,
    Map<String, ?> argument,
    Optional<Map<String, ?>> context,
    Object ref)
    implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpCompleteRequestParams {
    Objects.requireNonNull(meta, "meta");
    argument = McpProtocol.copy(Objects.requireNonNull(argument, "argument"));
    context = McpProtocol.copy(context);
    ref = McpProtocol.copy(Objects.requireNonNull(ref, "ref"));
  }

  /**
   * Returns a copy of the argument.
   *
   * @return the copied argument
   */
  public Map<String, ?> argument() {
    return McpProtocol.copy(argument);
  }

  /**
   * Returns a copy of the optional completion context.
   *
   * @return the copied context
   */
  public Optional<Map<String, ?>> context() {
    return McpProtocol.copy(context);
  }

  /**
   * Returns a copy of the completion target.
   *
   * @return the copied target
   */
  public Object ref() {
    return McpProtocol.copy(ref);
  }

  /**
   * Creates a builder for {@link McpCompleteRequestParams}.
   *
   * @return a new builder
   */
  public static Builder mcpCompleteRequestParams() {
    return new Builder();
  }

  /** Builds {@link McpCompleteRequestParams} values. */
  public static final class Builder {
    private McpRequestMetaObject meta;
    private Map<String, ?> argument;
    private Optional<Map<String, ?>> context = Optional.empty();
    private Object ref;

    private Builder() {}

    /**
     * Sets {@code meta} using a {@link McpRequestMetaObject} builder.
     *
     * @param configure the child configuration, invoked once before this builder changes
     * @return this builder
     */
    public Builder meta(Consumer<McpRequestMetaObject.Builder> configure) {
      var child = McpRequestMetaObject.mcpRequestMetaObject();
      configure.accept(child);
      return meta(child.build());
    }

    /**
     * Sets {@code meta}.
     *
     * @param meta the value
     * @return this builder
     */
    public Builder meta(McpRequestMetaObject meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Sets {@code argument}.
     *
     * @param argument the value
     * @return this builder
     */
    public Builder argument(Map<String, ?> argument) {
      this.argument = argument;
      return this;
    }

    /**
     * Sets {@code context}.
     *
     * @param context the optional value
     * @return this builder
     */
    public Builder context(Optional<Map<String, ?>> context) {
      this.context = context;
      return this;
    }

    /**
     * Sets {@code context}.
     *
     * @param context the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder context(Map<String, ?> context) {
      return context(Optional.ofNullable(context));
    }

    /**
     * Sets {@code ref}.
     *
     * @param ref the value
     * @return this builder
     */
    public Builder ref(Object ref) {
      this.ref = ref;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpCompleteRequestParams build() {
      return new McpCompleteRequestParams(meta, argument, context, ref);
    }
  }
}
