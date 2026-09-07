package io.github.suppierk.mcp.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Extends {@code MetaObject} with additional request-specific fields. All key naming rules from
 * {@code MetaObject} apply.
 *
 * @param clientCapabilities the client capabilities
 * @param clientInfo the optional client information
 * @param logLevel the optional request log level
 * @param protocolVersion the MCP revision
 * @param progressToken If specified, the caller is requesting out-of-band progress notifications
 *     for this request (as represented by {@link McpProgressNotification} ({@code
 *     notifications/progress})). The value of this parameter is an opaque token that will be
 *     attached to any subsequent notifications. The receiver is not obligated to provide these
 *     notifications.
 * @param extensions additional application-defined metadata
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public record McpRequestMetaObject(
    McpClientCapabilities clientCapabilities,
    Optional<McpImplementation> clientInfo,
    Optional<McpLoggingLevel> logLevel,
    String protocolVersion,
    Optional<Object> progressToken,
    Map<String, ?> extensions) {
  /** Validates and copies the protocol fields. */
  public McpRequestMetaObject {
    Objects.requireNonNull(clientCapabilities, "clientCapabilities");
    Objects.requireNonNull(clientInfo, "clientInfo");
    Objects.requireNonNull(logLevel, "logLevel");
    Objects.requireNonNull(protocolVersion, "protocolVersion");
    progressToken = McpProtocol.copy(progressToken);
    extensions = McpProtocol.copyObject(extensions);
  }

  /**
   * Returns a deep copy of the application-defined metadata.
   *
   * @return the additional metadata
   */
  public Map<String, ?> extensions() {
    return extensions;
  }

  /**
   * Returns a copy of the optional progress token.
   *
   * @return the copied token
   */
  public Optional<Object> progressToken() {
    return McpProtocol.copy(progressToken);
  }

  /**
   * Creates a builder for {@link McpRequestMetaObject}.
   *
   * @return a new builder
   */
  public static Builder mcpRequestMetaObject() {
    return new Builder();
  }

  /** Builds {@link McpRequestMetaObject} values. */
  public static final class Builder {
    private McpClientCapabilities clientCapabilities;
    private Optional<McpImplementation> clientInfo = Optional.empty();
    private Optional<McpLoggingLevel> logLevel = Optional.empty();
    private String protocolVersion;
    private Optional<Object> progressToken = Optional.empty();
    private Map<String, ?> extensions = Map.of();

    private Builder() {}

    /**
     * Sets {@code clientCapabilities}.
     *
     * @param clientCapabilities the value
     * @return this builder
     */
    public Builder clientCapabilities(McpClientCapabilities clientCapabilities) {
      this.clientCapabilities = clientCapabilities;
      return this;
    }

    /**
     * Sets {@code clientInfo}.
     *
     * @param clientInfo the optional value
     * @return this builder
     */
    public Builder clientInfo(Optional<McpImplementation> clientInfo) {
      this.clientInfo = clientInfo;
      return this;
    }

    /**
     * Sets {@code clientInfo}.
     *
     * @param clientInfo the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder clientInfo(McpImplementation clientInfo) {
      return clientInfo(Optional.ofNullable(clientInfo));
    }

    /**
     * Sets {@code logLevel}.
     *
     * @param logLevel the optional value
     * @return this builder
     */
    public Builder logLevel(Optional<McpLoggingLevel> logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    /**
     * Sets {@code logLevel}.
     *
     * @param logLevel the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder logLevel(McpLoggingLevel logLevel) {
      return logLevel(Optional.ofNullable(logLevel));
    }

    /**
     * Sets {@code protocolVersion}.
     *
     * @param protocolVersion the value
     * @return this builder
     */
    public Builder protocolVersion(String protocolVersion) {
      this.protocolVersion = protocolVersion;
      return this;
    }

    /**
     * Sets {@code progressToken}.
     *
     * @param progressToken the optional value
     * @return this builder
     */
    public Builder progressToken(Optional<Object> progressToken) {
      this.progressToken = progressToken;
      return this;
    }

    /**
     * Sets {@code progressToken}.
     *
     * @param progressToken the value, or {@code null} to clear it
     * @return this builder
     */
    public Builder progressToken(Object progressToken) {
      return progressToken(Optional.ofNullable(progressToken));
    }

    /**
     * Builds the value.
     *
     * @return the built value
     */
    public McpRequestMetaObject build() {
      return new McpRequestMetaObject(
          clientCapabilities, clientInfo, logLevel, protocolVersion, progressToken, extensions);
    }

    /**
     * Sets additional application-defined metadata.
     *
     * @param extensions the metadata fields
     * @return this builder
     */
    public Builder extensions(Map<String, ?> extensions) {
      this.extensions = extensions;
      return this;
    }
  }
}
