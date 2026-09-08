package io.github.suppierk.mcp.server;

import io.github.suppierk.mcp.protocol.McpError;
import java.io.Serial;
import java.util.Optional;

/** Reports an unknown JSON-RPC method. */
public final class McpMethodNotFoundException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int ERROR_CODE = -32601;

  /** Optional structured protocol error data. */
  // MCP serializes JsonRpcErrorResponse; Java object-stream serialization is not supported.
  @SuppressWarnings("java:S1948")
  private final Optional<Object> data;

  /**
   * Creates a method-not-found failure.
   *
   * @param message the concise error message
   * @param data optional protocol error data
   */
  public McpMethodNotFoundException(String message, Optional<Object> data) {
    super(ERROR_CODE, message);
    this.data = new McpError((long) ERROR_CODE, data, message).data();
  }

  /**
   * Returns a defensive copy of the optional error data.
   *
   * @return the optional error data
   */
  public Optional<Object> data() {
    return data;
  }
}
