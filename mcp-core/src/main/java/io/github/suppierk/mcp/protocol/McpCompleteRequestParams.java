package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import java.util.Optional;

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
    @JsonProperty("_meta") McpRequestMetaObject meta,
    ObjectNode argument,
    Optional<ObjectNode> context,
    JsonNode ref)
    implements McpRequestParameters {
  /** Validates and copies the protocol fields. */
  public McpCompleteRequestParams {
    Objects.requireNonNull(meta, "meta");
    argument = Objects.requireNonNull(argument, "argument").deepCopy();
    context = McpProtocol.copy(context);
    ref = Objects.requireNonNull(ref, "ref").deepCopy();
  }

  /**
   * Returns a copy of the argument.
   *
   * @return the copied argument
   */
  public ObjectNode argument() {
    return McpProtocol.copy(argument);
  }

  /**
   * Returns a copy of the optional completion context.
   *
   * @return the copied context
   */
  public Optional<ObjectNode> context() {
    return McpProtocol.copy(context);
  }

  /**
   * Returns a copy of the completion target.
   *
   * @return the copied target
   */
  public JsonNode ref() {
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
    private ObjectNode argument;
    private Optional<ObjectNode> context = Optional.empty();
    private JsonNode ref;

    private Builder() {}

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
    public Builder argument(ObjectNode argument) {
      this.argument = argument;
      return this;
    }

    /**
     * Sets {@code context}.
     *
     * @param context the optional value
     * @return this builder
     */
    public Builder context(Optional<ObjectNode> context) {
      this.context = context;
      return this;
    }

    /**
     * Sets {@code context}.
     *
     * @param context the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder context(ObjectNode context) {
      return context(Optional.ofNullable(context));
    }

    /**
     * Sets {@code ref}.
     *
     * @param ref the value
     * @return this builder
     */
    public Builder ref(JsonNode ref) {
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
