package io.github.suppierk.mcp.example.javalin;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.suppierk.mcp.javalin.JavalinMcpAdapter;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTool;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.BasicAuthCredentials;
import io.javalin.security.RouteRole;
import java.util.List;
import java.util.Map;

/** Shows how a Javalin host connects MCP building blocks to host-owned security. */
public final class JavalinExampleApplication {
  private static final String IDENTITY_ATTRIBUTE = "demo-identity";

  /** Prevents construction. */
  private JavalinExampleApplication() {}

  /**
   * Creates the configured Javalin application.
   *
   * @return the application
   */
  public static Javalin create() {
    McpServerKit<McpEmptyContext> publicServerKit = createPublicServerKit();
    McpServerKit<DemoIdentity> protectedServerKit = createProtectedServerKit();
    JavalinMcpAdapter<McpEmptyContext> publicAdapter = new JavalinMcpAdapter<>(publicServerKit);
    JavalinMcpAdapter<DemoIdentity> protectedAdapter = new JavalinMcpAdapter<>(protectedServerKit);
    return Javalin.create()
        .beforeMatched(JavalinExampleApplication::authenticateProtectedRoute)
        .post(
            "/mcp/public",
            context -> publicAdapter.handle(McpEmptyContext.INSTANCE, context),
            Access.PUBLIC)
        .post(
            "/mcp/protected",
            context -> protectedAdapter.handle(identity(context), context),
            Access.PROTECTED);
  }

  /** Creates the public server kit. */
  private static McpServerKit<McpEmptyContext> createPublicServerKit() {
    return McpServerKit.builder("javalin-public", "1.0.0", McpEmptyContext.class)
        .syncTool(
            new McpTool("hello", emptyInputSchema()),
            (applicationContext, request, handlerContext) -> result("Hello, World!"))
        .build();
  }

  /** Creates the protected server kit. */
  private static McpServerKit<DemoIdentity> createProtectedServerKit() {
    return McpServerKit.builder("javalin-protected", "1.0.0", DemoIdentity.class)
        .syncTool(
            new McpTool("current-user", emptyInputSchema()),
            (identity, request, handlerContext) -> result(identity.identifier()))
        .build();
  }

  /** Applies the demo-only Basic gate before a protected endpoint handler runs. */
  private static void authenticateProtectedRoute(Context context) {
    if (!context.routeRoles().contains(Access.PROTECTED)) {
      return;
    }
    BasicAuthCredentials credentials = context.basicAuthCredentials();
    if (credentials == null
        || !"demo-user".equals(credentials.getUsername())
        || !"demo-password".equals(credentials.getPassword())) {
      throw new UnauthorizedResponse(
          "Authentication required", Map.of("WWW-Authenticate", "Basic realm=\"mcp-example\""));
    }
    context.attribute(IDENTITY_ATTRIBUTE, new DemoIdentity(credentials.getUsername()));
  }

  /** Gets the identity established by access middleware. */
  private static DemoIdentity identity(Context context) {
    DemoIdentity identity = context.attribute(IDENTITY_ATTRIBUTE);
    if (identity == null) {
      throw new IllegalStateException("Protected route has no authenticated identity");
    }
    return identity;
  }

  /** Creates a closed schema for a tool that accepts no arguments. */
  private static ObjectNode emptyInputSchema() {
    return JsonNodeFactory.instance
        .objectNode()
        .put("type", "object")
        .put("additionalProperties", false);
  }

  /** Creates one successful text tool result. */
  private static McpCallToolResult result(String text) {
    return new McpCallToolResult(List.of(new McpTextContent(text)));
  }

  /** Route access classifications owned by this Javalin application. */
  private enum Access implements RouteRole {
    PUBLIC,
    PROTECTED
  }

  /**
   * Demo-only application identity. This is not MCP OAuth or production security guidance.
   *
   * @param identifier the authenticated user identifier
   */
  public record DemoIdentity(String identifier) {}

  /**
   * Starts the example on port 8080.
   *
   * @param args ignored command-line arguments
   */
  public static void main(String[] args) {
    create().start(8080);
  }
}
