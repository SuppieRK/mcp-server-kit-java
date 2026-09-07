package io.github.suppierk.mcp.example.quarkus;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTool;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.HttpAcceptedResponse;
import io.github.suppierk.mcp.transport.http.HttpEventStreamResponse;
import io.github.suppierk.mcp.transport.http.HttpJsonResponse;
import io.github.suppierk.mcp.transport.http.HttpMcpRequest;
import io.github.suppierk.mcp.transport.http.HttpMcpResponse;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.util.LinkedHashMap;
import java.util.List;

/** Exposes MCP building blocks through Quarkus REST with host-owned authorization. */
@Path("/mcp")
public final class McpEndpoint {
  private final StreamableHttpMcpTransport<McpEmptyContext> publicTransport =
      new StreamableHttpMcpTransport<>(
          McpServerKit.builder("quarkus-public", "1.0.0", McpEmptyContext.class)
              .syncTool(
                  new McpTool("hello", emptyInputSchema()),
                  (applicationContext, request, handlerContext) -> textResult("Hello, World!"))
              .build());
  private final StreamableHttpMcpTransport<SecurityIdentity> protectedTransport =
      new StreamableHttpMcpTransport<>(
          McpServerKit.builder("quarkus-protected", "1.0.0", SecurityIdentity.class)
              .syncTool(
                  new McpTool("current-user", emptyInputSchema()),
                  (identity, request, handlerContext) ->
                      textResult(identity.getPrincipal().getName()))
              .build());

  @Inject SecurityIdentity securityIdentity;

  /** Creates the endpoint. */
  public McpEndpoint() {}

  /**
   * Handles one anonymous MCP request.
   *
   * @param headers the request headers
   * @param body the request body
   * @return the JAX-RS response
   */
  @POST
  @Path("/public")
  @PermitAll
  public Response publicMcp(@Context HttpHeaders headers, byte[] body) {
    return handle(publicTransport, McpEmptyContext.INSTANCE, headers, body);
  }

  /**
   * Handles one MCP request after Quarkus authenticates it.
   *
   * @param headers the request headers
   * @param body the request body
   * @return the JAX-RS response
   */
  @POST
  @Path("/protected")
  @Authenticated
  public Response protectedMcp(@Context HttpHeaders headers, byte[] body) {
    return handle(protectedTransport, securityIdentity, headers, body);
  }

  /** Translates one Quarkus request through the selected application context and server kit. */
  private static <C> Response handle(
      StreamableHttpMcpTransport<C> transport,
      C applicationContext,
      HttpHeaders headers,
      byte[] body) {
    LinkedHashMap<String, List<String>> requestHeaders = new LinkedHashMap<>();
    headers
        .getRequestHeaders()
        .forEach((name, values) -> requestHeaders.put(name, List.copyOf(values)));
    HttpMcpRequest request = new HttpMcpRequest("POST", requestHeaders, body);
    HttpMcpResponse response =
        transport.handle(applicationContext, request).toCompletableFuture().join();
    Response.ResponseBuilder builder = Response.status(response.status());
    response.headers().forEach(builder::header);
    if (response instanceof HttpAcceptedResponse) {
      return builder.build();
    }
    if (response instanceof HttpJsonResponse json) {
      return builder.entity(json.body()).build();
    }
    HttpEventStreamResponse stream = (HttpEventStreamResponse) response;
    StreamingOutput output = stream::writeTo;
    return builder.entity(output).build();
  }

  /** Creates a closed schema for a tool that accepts no arguments. */
  private static ObjectNode emptyInputSchema() {
    return JsonNodeFactory.instance
        .objectNode()
        .put("type", "object")
        .put("additionalProperties", false);
  }

  /** Creates one successful text tool result. */
  private static McpCallToolResult textResult(String text) {
    return new McpCallToolResult(List.of(new McpTextContent(text)));
  }
}
