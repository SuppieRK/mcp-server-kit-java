package io.github.suppierk.mcp.example.springwebmvc;

import static io.github.suppierk.mcp.server.McpServerKit.mcpServerKit;

import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.spring.webmvc.SpringWebMvcMcpAdapter;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.HeaderWriterFilter;

/** Shows how a Spring WebMVC host connects MCP building blocks to host-owned security. */
@SpringBootApplication
public class SpringWebMvcExampleApplication {
  /** Creates the application. */
  public SpringWebMvcExampleApplication() {}

  /**
   * Starts the example.
   *
   * @param args the Spring Boot arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(SpringWebMvcExampleApplication.class, args);
  }

  /**
   * Creates the public MCP server kit.
   *
   * @return the server kit
   */
  @Bean
  public McpServerKit<McpEmptyContext> publicMcpServerKit() {
    return mcpServerKit("spring-webmvc-public", "1.0.0", McpEmptyContext.class)
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
   * Creates the protected MCP server kit.
   *
   * @return the server kit
   */
  @Bean
  public McpServerKit<Authentication> protectedMcpServerKit() {
    return mcpServerKit("spring-webmvc-protected", "1.0.0", Authentication.class)
        .syncTool(
            registration ->
                registration
                    .name("current-user")
                    .handler(
                        (authentication, request, handlerContext) ->
                            new McpCallToolResult(
                                List.of(new McpTextContent(authentication.getName())))))
        .build();
  }

  /**
   * Creates the public WebMVC adapter.
   *
   * @param serverKit the MCP server kit
   * @return the adapter
   */
  @Bean
  public SpringWebMvcMcpAdapter<McpEmptyContext> publicMcpAdapter(
      McpServerKit<McpEmptyContext> serverKit) {
    return new SpringWebMvcMcpAdapter<>(serverKit);
  }

  /**
   * Creates the protected WebMVC adapter.
   *
   * @param serverKit the MCP server kit
   * @return the adapter
   */
  @Bean
  public SpringWebMvcMcpAdapter<Authentication> protectedMcpAdapter(
      McpServerKit<Authentication> serverKit) {
    return new SpringWebMvcMcpAdapter<>(serverKit);
  }

  /**
   * Applies host-owned security before protected MCP requests reach their server kit.
   *
   * @param http Spring Security configuration
   * @return the configured filter chain
   * @throws Exception if Spring Security cannot build the chain
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.headers(
            headers ->
                headers.withObjectPostProcessor(
                    new ObjectPostProcessor<HeaderWriterFilter>() {
                      @Override
                      public <F extends HeaderWriterFilter> F postProcess(F filter) {
                        // Finish security headers before StreamingResponseBody starts writing
                        // from another thread; servlet response headers are not thread-safe.
                        filter.setShouldWriteHeadersEagerly(true);
                        return filter;
                      }
                    }))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/mcp/**"))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers("/mcp/public")
                    .permitAll()
                    .requestMatchers("/mcp/protected")
                    .authenticated()
                    .anyRequest()
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
  public UserDetailsService demoOnlyUsers() {
    return new InMemoryUserDetailsManager(
        User.withUsername("demo-user").password("{noop}demo-password").roles("USER").build());
  }
}
