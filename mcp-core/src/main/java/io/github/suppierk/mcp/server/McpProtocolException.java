package io.github.suppierk.mcp.server;

import java.io.Serial;
import java.util.Objects;

/** A deliberate MCP protocol failure whose wire code is fixed by its concrete type. */
public abstract sealed class McpProtocolException extends RuntimeException
    permits McpHeaderMismatchException,
        McpInternalException,
        McpInvalidParamsException,
        McpInvalidRequestException,
        McpMethodNotFoundException,
        McpMissingRequiredClientCapabilityException,
        McpParseException,
        McpUnsupportedProtocolVersionException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed JSON-RPC or MCP error code. */
  private final int code;

  /** Creates a fixed-code protocol exception. */
  McpProtocolException(int code, String message) {
    super(Objects.requireNonNull(message, "message"));
    this.code = code;
  }

  /**
   * Returns the fixed JSON-RPC or MCP error code.
   *
   * @return the wire error code
   */
  public final int code() {
    return code;
  }
}
