package io.github.suppierk.mcp.server;

import io.github.suppierk.mcp.protocol.McpError;
import java.io.Serial;
import java.util.Optional;

/** Reports invalid method parameters. */
public final class McpInvalidParamsException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int ERROR_CODE = -32602;

  /** Optional structured protocol error data. */
  // MCP serializes JsonRpcErrorResponse; Java object-stream serialization is not supported.
  @SuppressWarnings("java:S1948")
  private final Optional<Object> data;

  /**
   * Creates an invalid-parameters failure.
   *
   * @param message the concise error message
   * @param data optional protocol error data
   */
  public McpInvalidParamsException(String message, Optional<Object> data) {
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
