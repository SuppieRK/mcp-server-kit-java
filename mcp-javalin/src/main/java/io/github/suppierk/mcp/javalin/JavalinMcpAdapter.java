package io.github.suppierk.mcp.javalin;

import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.HttpAcceptedResponse;
import io.github.suppierk.mcp.transport.http.HttpEventStreamResponse;
import io.github.suppierk.mcp.transport.http.HttpJsonResponse;
import io.github.suppierk.mcp.transport.http.HttpMcpRequest;
import io.github.suppierk.mcp.transport.http.HttpMcpResponse;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import io.javalin.http.Context;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Translates Javalin requests to the framework-neutral HTTP binding.
 *
 * @param <C> the application-context type
 */
public final class JavalinMcpAdapter<C> {
  private final StreamableHttpMcpTransport<C> transport;

  /**
   * Creates an adapter with the default HTTP transport policy.
   *
   * @param serverKit the MCP server kit
   */
  public JavalinMcpAdapter(McpServerKit<C> serverKit) {
    this(new StreamableHttpMcpTransport<>(serverKit));
  }

  /**
   * Creates an adapter.
   *
   * @param transport the shared HTTP binding
   */
  public JavalinMcpAdapter(StreamableHttpMcpTransport<C> transport) {
    this.transport = Objects.requireNonNull(transport, "transport");
  }

  /**
   * Handles one Javalin context.
   *
   * @param applicationContext application-owned invocation data
   * @param context the Javalin context
   */
  public void handle(C applicationContext, Context context) {
    Objects.requireNonNull(applicationContext, "applicationContext");
    LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
    Collections.list(context.req().getHeaderNames())
        .forEach(name -> headers.put(name, Collections.list(context.req().getHeaders(name))));
    HttpMcpRequest request =
        new HttpMcpRequest(context.method().name(), headers, context.bodyAsBytes());
    HttpMcpResponse response =
        transport.handle(applicationContext, request).toCompletableFuture().join();
    context.status(response.status());
    response.headers().forEach(context::header);
    if (response instanceof HttpAcceptedResponse) {
      return;
    }
    if (response instanceof HttpJsonResponse json) {
      context.result(json.body());
      return;
    }
    context.result(((HttpEventStreamResponse) response).inputStream());
  }
}
