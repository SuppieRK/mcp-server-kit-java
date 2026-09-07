package io.github.suppierk.mcp.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.util.Objects;
import java.util.Optional;

/** Reports a deliberate internal JSON-RPC failure. */
public final class McpInternalException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int CODE = -32603;

  /** Optional structured protocol error data. */
  private final Optional<JsonNode> data;

  /**
   * Creates an internal failure.
   *
   * @param message the concise error message
   * @param data optional protocol error data
   */
  public McpInternalException(String message, Optional<JsonNode> data) {
    super(CODE, message);
    this.data = Objects.requireNonNull(data, "data").map(JsonNode::deepCopy);
  }

  /**
   * Returns a defensive copy of the optional error data.
   *
   * @return the optional error data
   */
  public Optional<JsonNode> data() {
    return data.map(JsonNode::deepCopy);
  }
}
