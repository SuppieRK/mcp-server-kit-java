package io.github.suppierk.mcp.example.springwebflux;

import static io.github.suppierk.mcp.server.McpServerKit.mcpServerKit;

import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.spring.webflux.SpringWebFluxMcpAdapter;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/** Shows how a Spring WebFlux host connects MCP building blocks to host-owned security. */
@SpringBootApplication
public class SpringWebFluxExampleApplication {
  /** Creates the application. */
  public SpringWebFluxExampleApplication() {}

  /**
   * Starts the example.
   *
   * @param args the Spring Boot arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(SpringWebFluxExampleApplication.class, args);
  }

  /**
   * Creates the public MCP server kit.
   *
   * @return the server kit
   */
  @Bean
  public McpServerKit<McpEmptyContext> publicMcpServerKit() {
    return mcpServerKit("spring-webflux-public", "1.0.0", McpEmptyContext.class)
        .syncTool(
            registration ->
                registration
                    .name("hello")
                    .handler(
                        (applicationContext, request, handlerContext) ->
                            new McpCallToolResult(List.of(new McpTextContent("Hello, World!")))))
        .build();
  }

  /**
   * Creates the protected MCP server kit with an asynchronous Reactor-to-JDK handler bridge.
   *
   * @return the server kit
   */
  @Bean
  public McpServerKit<Authentication> protectedMcpServerKit() {
    return mcpServerKit("spring-webflux-protected", "1.0.0", Authentication.class)
        .asyncTool(
            registration ->
                registration
                    .name("current-user")
                    .handler(
                        (authentication, request, handlerContext) ->
                            Mono.just(
                                    new McpCallToolResult(
                                        List.of(new McpTextContent(authentication.getName()))))
                                .toFuture()))
        .build();
  }

  /**
   * Creates the WebFlux route.
   *
   * @param publicServerKit the public MCP server kit
   * @param protectedServerKit the protected MCP server kit
   * @return the MCP route
   */
  @Bean
  public RouterFunction<ServerResponse> mcpRoute(
      McpServerKit<McpEmptyContext> publicServerKit,
      McpServerKit<Authentication> protectedServerKit) {
    SpringWebFluxMcpAdapter<McpEmptyContext> publicAdapter =
        new SpringWebFluxMcpAdapter<>(publicServerKit);
    SpringWebFluxMcpAdapter<Authentication> protectedAdapter =
        new SpringWebFluxMcpAdapter<>(protectedServerKit);
    return RouterFunctions.route()
        .POST("/mcp/public", request -> publicAdapter.handle(McpEmptyContext.INSTANCE, request))
        .POST(
            "/mcp/protected",
            request ->
                request
                    .principal()
                    .cast(Authentication.class)
                    .flatMap(authentication -> protectedAdapter.handle(authentication, request)))
        .build();
  }

  /**
   * Applies host-owned security before protected MCP requests reach their server kit.
   *
   * @param http Spring Security configuration
   * @return the configured filter chain
   */
  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers("/mcp/public")
                    .permitAll()
                    .pathMatchers("/mcp/protected")
                    .authenticated()
                    .anyExchange()
                    .denyAll())
        .httpBasic(Customizer.withDefaults())
        .build();
  }

  /**
   * Provides one demo-only user. This is not MCP OAuth or production security guidance.
   *
   * @return the in-memory user store
   */
  @Bean
  public MapReactiveUserDetailsService demoOnlyUsers() {
    UserDetails user =
        User.withUsername("demo-user").password("{noop}demo-password").roles("USER").build();
    return new MapReactiveUserDetailsService(user);
  }
}
