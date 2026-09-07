package io.github.suppierk.mcp.server;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

/** Reports an MCP revision that this server kit does not support. */
public final class McpUnsupportedProtocolVersionException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int CODE = -32022;

  /** The unsupported revision requested by the client. */
  private final String requestedRevision;

  /** The revisions supported by the server kit. */
  private final List<String> supportedRevisions;

  /**
   * Creates an unsupported-revision failure.
   *
   * @param message the concise error message
   * @param requestedRevision the requested MCP revision
   * @param supportedRevisions the supported MCP revisions
   */
  public McpUnsupportedProtocolVersionException(
      String message, String requestedRevision, List<String> supportedRevisions) {
    super(CODE, message);
    this.requestedRevision = Objects.requireNonNull(requestedRevision, "requestedRevision");
    this.supportedRevisions = List.copyOf(supportedRevisions);
  }

  /**
   * Returns the requested revision.
   *
   * @return the requested revision
   */
  public String requestedRevision() {
    return requestedRevision;
  }

  /**
   * Returns the supported revisions.
   *
   * @return the immutable supported revisions
   */
  public List<String> supportedRevisions() {
    return supportedRevisions;
  }
}
