package io.github.suppierk.mcp.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.util.Objects;
import java.util.Optional;

/** Reports an unknown JSON-RPC method. */
public final class McpMethodNotFoundException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int CODE = -32601;

  /** Optional structured protocol error data. */
  private final Optional<JsonNode> data;

  /**
   * Creates a method-not-found failure.
   *
   * @param message the concise error message
   * @param data optional protocol error data
   */
  public McpMethodNotFoundException(String message, Optional<JsonNode> data) {
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
