package io.github.suppierk.mcp.server;

import io.github.suppierk.mcp.protocol.McpError;
import java.io.Serial;
import java.util.Optional;

/** Reports an unknown JSON-RPC method. */
public final class McpMethodNotFoundException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int CODE = -32601;

  /** Optional structured protocol error data. */
  private final Optional<Object> data;

  /**
   * Creates a method-not-found failure.
   *
   * @param message the concise error message
   * @param data optional protocol error data
   */
  public McpMethodNotFoundException(String message, Optional<Object> data) {
    super(CODE, message);
    this.data = new McpError((long) CODE, data, message).data();
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
