package io.github.suppierk.mcp.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.util.Objects;
import java.util.Optional;

/** Reports HTTP routing metadata that does not match the JSON-RPC body. */
public final class McpHeaderMismatchException extends McpProtocolException {
  @Serial private static final long serialVersionUID = 1L;

  /** The fixed wire error code. */
  public static final int CODE = -32020;

  /** Optional structured protocol error data. */
  private final Optional<JsonNode> data;

  /**
   * Creates a header-mismatch failure.
   *
   * @param message the concise error message
   * @param data optional protocol error data
   */
  public McpHeaderMismatchException(String message, Optional<JsonNode> data) {
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
