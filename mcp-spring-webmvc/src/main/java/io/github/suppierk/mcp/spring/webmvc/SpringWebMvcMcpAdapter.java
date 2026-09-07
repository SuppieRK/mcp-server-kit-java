package io.github.suppierk.mcp.spring.webmvc;

import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.HttpAcceptedResponse;
import io.github.suppierk.mcp.transport.http.HttpEventStreamResponse;
import io.github.suppierk.mcp.transport.http.HttpJsonResponse;
import io.github.suppierk.mcp.transport.http.HttpMcpRequest;
import io.github.suppierk.mcp.transport.http.HttpMcpResponse;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Translates Spring WebMVC requests to the framework-neutral HTTP binding.
 *
 * @param <C> the application-context type
 */
public final class SpringWebMvcMcpAdapter<C> {
  private final StreamableHttpMcpTransport<C> transport;

  /**
   * Creates an adapter with the default HTTP transport policy.
   *
   * @param serverKit the MCP server kit
   */
  public SpringWebMvcMcpAdapter(McpServerKit<C> serverKit) {
    this(new StreamableHttpMcpTransport<>(serverKit));
  }

  /**
   * Creates an adapter.
   *
   * @param transport the shared HTTP binding
   */
  public SpringWebMvcMcpAdapter(StreamableHttpMcpTransport<C> transport) {
    this.transport = Objects.requireNonNull(transport, "transport");
  }

  /**
   * Handles one Spring WebMVC request.
   *
   * <p>The controller must declare {@code ResponseEntity<StreamingResponseBody>} as its return type
   * so that Spring selects the streaming return-value handler.
   *
   * @param applicationContext application-owned invocation data
   * @param request the servlet request
   * @param servletResponse the native response used to flush SSE events
   * @param body the request body
   * @return the Spring response entity
   */
  public ResponseEntity<StreamingResponseBody> handle(
      C applicationContext,
      HttpServletRequest request,
      HttpServletResponse servletResponse,
      byte[] body) {
    Objects.requireNonNull(applicationContext, "applicationContext");
    Objects.requireNonNull(servletResponse, "servletResponse");
    HttpMcpResponse response =
        transport.handle(applicationContext, toRequest(request, body)).toCompletableFuture().join();
    ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.status());
    response.headers().forEach(builder::header);
    if (response instanceof HttpAcceptedResponse) {
      return builder.build();
    }
    if (response instanceof HttpJsonResponse json) {
      return builder.body(output -> output.write(json.body()));
    }
    HttpEventStreamResponse stream = (HttpEventStreamResponse) response;
    StreamingResponseBody streamingBody =
        ignored -> stream.writeTo(servletResponse.getOutputStream());
    return builder.body(streamingBody);
  }

  /** Converts the servlet request to a library-owned request. */
  private static HttpMcpRequest toRequest(HttpServletRequest request, byte[] body) {
    LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
    Collections.list(request.getHeaderNames())
        .forEach(name -> headers.put(name, Collections.list(request.getHeaders(name))));
    return new HttpMcpRequest(request.getMethod(), headers, body);
  }
}
