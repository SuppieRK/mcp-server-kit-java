package io.github.suppierk.mcp.example.micronaut;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import jakarta.inject.Singleton;

/**
 * Demo-only Basic identity store. This is not MCP OAuth or production security guidance.
 *
 * @param <B> the request body type
 */
@Singleton
public final class DemoAuthenticationProvider<B> implements HttpRequestAuthenticationProvider<B> {
  /** Creates the provider. */
  public DemoAuthenticationProvider() {}

  @Override
  public AuthenticationResponse authenticate(
      HttpRequest<B> requestContext, AuthenticationRequest<String, String> authRequest) {
    if ("demo-user".equals(authRequest.getIdentity())
        && "demo-password".equals(authRequest.getSecret())) {
      return AuthenticationResponse.success("demo-user");
    }
    return AuthenticationResponse.failure();
  }
}
