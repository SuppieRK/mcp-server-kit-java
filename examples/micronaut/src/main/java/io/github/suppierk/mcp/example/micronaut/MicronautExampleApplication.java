package io.github.suppierk.mcp.example.micronaut;

import io.micronaut.runtime.Micronaut;

/** Starts the Micronaut host example for the MCP building blocks. */
public final class MicronautExampleApplication {
  /** Prevents construction. */
  private MicronautExampleApplication() {}

  /**
   * Starts the example.
   *
   * @param args the Micronaut arguments
   */
  public static void main(String[] args) {
    Micronaut.run(MicronautExampleApplication.class, args);
  }
}
