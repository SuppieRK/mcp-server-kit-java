package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;

/**
 * The parameters for a request to elicit non-sensitive information from the user via a form in the
 * client.
 *
 * @param message The message to present to the user describing what information is being requested.
 * @param requestedSchema A restricted subset of JSON Schema. Only top-level properties are allowed,
 *     without nesting.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpElicitRequestFormParams(String message, ObjectNode requestedSchema)
    implements McpElicitRequestParams {

  private static final String MODE = "form";

  /** Validates and copies the protocol fields. */
  public McpElicitRequestFormParams {
    Objects.requireNonNull(message, "message");
    requestedSchema = Objects.requireNonNull(requestedSchema, "requestedSchema").deepCopy();
  }

  /**
   * Returns a copy of the requested schema.
   *
   * @return the copied schema
   */
  public ObjectNode requestedSchema() {
    return McpProtocol.copy(requestedSchema);
  }

  /**
   * Gets the constant {@code mode} value.
   *
   * @return the constant value
   */
  @JsonProperty("mode")
  public String mode() {
    return MODE;
  }

  /**
   * Creates a builder for {@link McpElicitRequestFormParams}.
   *
   * @return a new builder
   */
  public static Builder mcpElicitRequestFormParams() {
    return new Builder();
  }

  /** Builds {@link McpElicitRequestFormParams} values. */
  public static final class Builder {
    private String message;
    private ObjectNode requestedSchema;

    private Builder() {}

    /**
     * Sets {@code message}.
     *
     * @param message the value
     * @return this builder
     */
    public Builder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * Sets {@code requestedSchema}.
     *
     * @param requestedSchema the value
     * @return this builder
     */
    public Builder requestedSchema(ObjectNode requestedSchema) {
      this.requestedSchema = requestedSchema;
      return this;
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpElicitRequestFormParams build() {
      return new McpElicitRequestFormParams(message, requestedSchema);
    }
  }
}
