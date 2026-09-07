package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The result returned by the client for an {@link McpElicitRequest} ({@code elicitation/create})
 * request.
 *
 * @param action the user action. {@code accept} submits the form. {@code decline} rejects the
 *     action. {@code cancel} dismisses it without a choice.
 * @param content The submitted form data, only present when action is {@code "accept"} and mode was
 *     {@code "form"}. Contains values matching the requested schema. Omitted for out-of-band mode
 *     responses.
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpElicitResult(String action, Optional<Map<String, ?>> content)
    implements McpInputResponse {
  /** Validates and copies the protocol fields. */
  public McpElicitResult {
    Objects.requireNonNull(action, "action");
    content = McpProtocol.copy(content);
  }

  /**
   * Returns a copy of the optional submitted form content.
   *
   * @return the copied content
   */
  public Optional<Map<String, ?>> content() {
    return McpProtocol.copy(content);
  }

  /**
   * Creates a builder for {@link McpElicitResult}.
   *
   * @return a new builder
   */
  public static Builder mcpElicitResult() {
    return new Builder();
  }

  /** Builds {@link McpElicitResult} values. */
  public static final class Builder {
    private String action;
    private Optional<Map<String, ?>> content = Optional.empty();

    private Builder() {}

    /**
     * Sets {@code action}.
     *
     * @param action the value
     * @return this builder
     */
    public Builder action(String action) {
      this.action = action;
      return this;
    }

    /**
     * Sets {@code content}.
     *
     * @param content the optional value
     * @return this builder
     */
    public Builder content(Optional<Map<String, ?>> content) {
      this.content = content;
      return this;
    }

    /**
     * Sets {@code content}.
     *
     * @param content the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder content(Map<String, ?> content) {
      return content(Optional.ofNullable(content));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpElicitResult build() {
      return new McpElicitResult(action, content);
    }
  }
}
