package io.github.suppierk.mcp.example.springwebmvc;

import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.spring.webmvc.SpringWebMvcMcpAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** Exposes separate public and protected MCP endpoints with host-owned authorization. */
@RestController
public final class McpController {
  private final SpringWebMvcMcpAdapter<McpEmptyContext> publicAdapter;
  private final SpringWebMvcMcpAdapter<Authentication> protectedAdapter;

  /**
   * Creates the controller.
   *
   * @param publicAdapter the public MCP adapter
   * @param protectedAdapter the protected MCP adapter
   */
  public McpController(
      SpringWebMvcMcpAdapter<McpEmptyContext> publicAdapter,
      SpringWebMvcMcpAdapter<Authentication> protectedAdapter) {
    this.publicAdapter = publicAdapter;
    this.protectedAdapter = protectedAdapter;
  }

  /**
   * Handles one MCP request.
   *
   * @param request the servlet request
   * @param response the servlet response
   * @param body the request body
   * @return the HTTP response
   */
  @PostMapping(path = "/mcp/public")
  public ResponseEntity<StreamingResponseBody> publicMcp(
      HttpServletRequest request, HttpServletResponse response, @RequestBody byte[] body) {
    return publicAdapter.handle(McpEmptyContext.INSTANCE, request, response, body);
  }

  /**
   * Handles one protected MCP request after Spring Security authenticates it.
   *
   * @param authentication the Spring Security identity
   * @param request the servlet request
   * @param response the servlet response
   * @param body the request body
   * @return the HTTP response
   */
  @PostMapping(path = "/mcp/protected")
  public ResponseEntity<StreamingResponseBody> protectedMcp(
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody byte[] body) {
    return protectedAdapter.handle(authentication, request, response, body);
  }
}
