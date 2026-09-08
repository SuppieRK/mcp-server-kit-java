package io.github.suppierk.mcp.server;

import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import java.io.Serial;
import java.util.Objects;

/**
 * A deliberate MCP protocol failure whose wire code is fixed by its concrete type.
 *
 * <p>These exceptions are application-side signals. The server kit converts them to {@link
 * JsonRpcErrorResponse} for MCP JSON output. Java object-stream serialization of these exceptions
 * is not supported.
 */
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
