package io.github.suppierk.mcp.example.micronaut;

import static io.github.suppierk.mcp.server.McpServerKit.mcpServerKit;

import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.transport.http.HttpAcceptedResponse;
import io.github.suppierk.mcp.transport.http.HttpEventStreamResponse;
import io.github.suppierk.mcp.transport.http.HttpJsonResponse;
import io.github.suppierk.mcp.transport.http.HttpMcpRequest;
import io.github.suppierk.mcp.transport.http.HttpMcpResponse;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import java.util.LinkedHashMap;
import java.util.List;
import org.reactivestreams.FlowAdapters;

/** Exposes MCP building blocks through Micronaut HTTP with host-owned authorization. */
@Controller("/mcp")
public final class McpController {
  private final StreamableHttpMcpTransport<McpEmptyContext> publicTransport =
      new StreamableHttpMcpTransport<>(
          mcpServerKit("micronaut-public", "1.0.0", McpEmptyContext.class)
              .syncTool(
                  registration ->
                      registration
                          .name("hello")
                          .handler(
                              (applicationContext, request, handlerContext) ->
                                  new McpCallToolResult(
                                      List.of(new McpTextContent("Hello, World!")))))
              .build());
  private final StreamableHttpMcpTransport<Authentication> protectedTransport =
      new StreamableHttpMcpTransport<>(
          mcpServerKit("micronaut-protected", "1.0.0", Authentication.class)
              .syncTool(
                  registration ->
                      registration
                          .name("current-user")
                          .handler(
                              (authentication, request, handlerContext) ->
                                  new McpCallToolResult(
                                      List.of(new McpTextContent(authentication.getName())))))
              .build());

  /** Creates the controller. */
  public McpController() {}

  /**
   * Handles one anonymous MCP request.
   *
   * @param request the Micronaut request
   * @param body the request body
   * @return the Micronaut response
   */
  @Post("/public")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<?> publicMcp(HttpRequest<byte[]> request, @Body byte[] body) {
    return handle(publicTransport, McpEmptyContext.INSTANCE, request, body);
  }

  /**
   * Handles one MCP request after Micronaut Security authenticates it.
   *
   * @param authentication the Micronaut identity
   * @param request the Micronaut request
   * @param body the request body
   * @return the Micronaut response
   */
  @Post("/protected")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<?> protectedMcp(
      Authentication authentication, HttpRequest<byte[]> request, @Body byte[] body) {
    return handle(protectedTransport, authentication, request, body);
  }

  /** Translates one Micronaut request through the selected application context and server kit. */
  private static <C> HttpResponse<?> handle(
      StreamableHttpMcpTransport<C> transport,
      C applicationContext,
      HttpRequest<byte[]> request,
      byte[] body) {
    LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
    request.getHeaders().forEach(headers::put);
    HttpMcpRequest exchange = new HttpMcpRequest(request.getMethodName(), headers, body);
    HttpMcpResponse result =
        transport.handle(applicationContext, exchange).toCompletableFuture().join();
    MutableHttpResponse<Object> response = HttpResponse.status(result.status(), "MCP");
    result.headers().forEach(response::header);
    if (result instanceof HttpAcceptedResponse) {
      return response;
    }
    if (result instanceof HttpJsonResponse json) {
      return response.body(json.body());
    }
    return response.body(
        Publishers.map(
            FlowAdapters.toPublisher(((HttpEventStreamResponse) result).events()),
            buffer -> {
              byte[] bytes = new byte[buffer.remaining()];
              buffer.get(bytes);
              return bytes;
            }));
  }
}
