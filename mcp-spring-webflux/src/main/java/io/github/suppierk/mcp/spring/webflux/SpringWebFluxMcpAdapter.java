package io.github.suppierk.mcp.spring.webflux;

import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.HttpAcceptedResponse;
import io.github.suppierk.mcp.transport.http.HttpEventStreamResponse;
import io.github.suppierk.mcp.transport.http.HttpJsonResponse;
import io.github.suppierk.mcp.transport.http.HttpMcpRequest;
import io.github.suppierk.mcp.transport.http.HttpMcpResponse;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Translates Spring WebFlux requests to the framework-neutral HTTP binding.
 *
 * @param <C> the application-context type
 */
public final class SpringWebFluxMcpAdapter<C> {
  private final StreamableHttpMcpTransport<C> transport;

  /**
   * Creates an adapter with the default HTTP transport policy.
   *
   * @param serverKit the MCP server kit
   */
  public SpringWebFluxMcpAdapter(McpServerKit<C> serverKit) {
    this(new StreamableHttpMcpTransport<>(serverKit));
  }

  /**
   * Creates an adapter.
   *
   * @param transport the shared HTTP binding
   */
  public SpringWebFluxMcpAdapter(StreamableHttpMcpTransport<C> transport) {
    this.transport = Objects.requireNonNull(transport, "transport");
  }

  /**
   * Handles one functional WebFlux request.
   *
   * @param applicationContext application-owned invocation data
   * @param request the WebFlux request
   * @return a publisher for the WebFlux response
   */
  public Mono<ServerResponse> handle(C applicationContext, ServerRequest request) {
    Objects.requireNonNull(applicationContext, "applicationContext");
    return request
        .bodyToMono(byte[].class)
        .defaultIfEmpty(new byte[0])
        .flatMap(
            body -> {
              LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
              request.headers().asHttpHeaders().forEach(headers::put);
              HttpMcpRequest exchange = new HttpMcpRequest(request.method().name(), headers, body);
              return Mono.fromCompletionStage(transport.handle(applicationContext, exchange));
            })
        .flatMap(this::toResponse);
  }

  /** Converts a framework-neutral response to a WebFlux response. */
  private Mono<ServerResponse> toResponse(HttpMcpResponse response) {
    ServerResponse.BodyBuilder builder = ServerResponse.status(response.status());
    response.headers().forEach(builder::header);
    if (response instanceof HttpAcceptedResponse) {
      return builder.build();
    }
    if (response instanceof HttpJsonResponse json) {
      return builder.bodyValue(json.body());
    }
    HttpEventStreamResponse stream = (HttpEventStreamResponse) response;
    Flux<DataBuffer> buffers =
        JdkFlowAdapter.flowPublisherToFlux(stream.events())
            .map(SpringWebFluxMcpAdapter::dataBuffer);
    return builder.body(BodyInserters.fromDataBuffers(buffers));
  }

  /** Wraps one read-only byte buffer for WebFlux output. */
  private static DataBuffer dataBuffer(ByteBuffer source) {
    return DefaultDataBufferFactory.sharedInstance.wrap(source);
  }
}
