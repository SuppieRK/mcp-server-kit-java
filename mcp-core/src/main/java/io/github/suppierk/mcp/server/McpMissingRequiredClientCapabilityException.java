package io.github.suppierk.mcp.server;

import io.github.suppierk.mcp.protocol.McpClientCapabilities;
import java.io.Serial;
import java.util.Objects;

/** Reports a client that did not declare a capability required by an operation. */
public final class McpMissingRequiredClientCapabilityException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int ERROR_CODE = -32021;

  /** The client capabilities required by the failed operation. */
  // MCP serializes JsonRpcErrorResponse; Java object-stream serialization is not supported.
  @SuppressWarnings("java:S1948")
  private final McpClientCapabilities requiredCapabilities;

  /**
   * Creates a missing-capability failure.
   *
   * @param message the concise error message
   * @param requiredCapabilities the capabilities required by the operation
   */
  public McpMissingRequiredClientCapabilityException(
      String message, McpClientCapabilities requiredCapabilities) {
    super(ERROR_CODE, message);
    this.requiredCapabilities =
        Objects.requireNonNull(requiredCapabilities, "requiredCapabilities");
  }

  /**
   * Returns the required client capabilities.
   *
   * @return the required capabilities
   */
  public McpClientCapabilities requiredCapabilities() {
    return requiredCapabilities;
  }
}
