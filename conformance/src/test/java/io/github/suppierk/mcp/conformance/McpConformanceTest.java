package io.github.suppierk.mcp.conformance;

import static io.github.suppierk.mcp.protocol.McpCallToolResult.mcpCallToolResult;
import static io.github.suppierk.mcp.protocol.McpClientCapabilities.mcpClientCapabilities;
import static io.github.suppierk.mcp.protocol.McpInputRequiredResult.mcpInputRequiredResult;
import static io.github.suppierk.mcp.server.McpServerKit.mcpServerKit;
import static io.github.suppierk.mcp.transport.http.HttpMcpRequest.httpMcpRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpMissingRequiredClientCapabilityException;
import io.github.suppierk.mcp.server.McpServerKit;
import io.github.suppierk.mcp.transport.http.HttpEventStreamResponse;
import io.github.suppierk.mcp.transport.http.HttpJsonResponse;
import io.github.suppierk.mcp.transport.http.StreamableHttpMcpTransport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Runs the official runner against a real HTTP endpoint, never mocked kit internals. */
class McpConformanceTest {
  @Test
  void passesTheFullServerSdkRequirements() throws Exception {
    Path reports = Path.of(System.getProperty("conformance.reports"));
    Files.createDirectories(reports);
    Path run = Files.createTempDirectory(reports, "run-");
    Path output = run.resolve("runner.log");
    var executor = Executors.newCachedThreadPool();
    try (var kit = createKit(executor)) {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      URI origin = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
      var transport = new StreamableHttpMcpTransport<>(kit, Set.of(origin));
      server.setExecutor(executor);
      server.createContext("/mcp", exchange -> handle(transport, exchange));
      server.start();
      try {
        Process process =
            new ProcessBuilder(
                    "node",
                    System.getProperty("conformance.runner"),
                    "server",
                    "--url",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/mcp",
                    "--requirements",
                    "2026-07-28",
                    "--output-dir",
                    run.toString())
                .directory(run.toFile())
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        try {
          assertTrue(
              process.waitFor(5, TimeUnit.MINUTES), "Conformance runner timed out: " + output);
          assertEquals(0, process.exitValue(), () -> "Upstream conformance failed; see " + output);
          verifyRequiredReports(run);
        } finally {
          process.destroyForcibly();
          process.waitFor(5, TimeUnit.SECONDS);
        }
      } finally {
        server.stop(0);
      }
    } finally {
      executor.shutdownNow();
      assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "HTTP fixture did not stop");
    }
  }

  private static void verifyRequiredReports(Path run) throws IOException {
    // Independent inventory of the pinned requirements file. A missing/skipped suite is not a pass.
    Set<String> required;
    try (var input =
        McpConformanceTest.class.getResourceAsStream("/required-server-scenarios.txt")) {
      required =
          Set.copyOf(
              Arrays.asList(
                  new String(input.readAllBytes(), StandardCharsets.UTF_8).strip().split("\\R")));
    }
    assertEquals(37, required.size(), "Review the pinned full server requirements when updating");
    Map<String, Path> reports = new HashMap<>();
    try (var files = Files.walk(run)) {
      files
          .filter(path -> path.getFileName().toString().equals("checks.json"))
          .forEach(
              path -> {
                String scenario =
                    path.getParent()
                        .getFileName()
                        .toString()
                        .replaceFirst("^server-", "")
                        .replaceFirst("-\\d{4}-\\d{2}-\\d{2}T.*$", "");
                assertTrue(reports.put(scenario, path) == null, "Duplicate report for " + scenario);
              });
    }
    assertEquals(50, reports.size(), "Expected 37 required and 13 upstream diagnostic scenarios");
    var json = new ObjectMapper();
    Map<String, Integer> requiredCounts = new TreeMap<>();
    Map<String, Integer> diagnosticCounts = new TreeMap<>();
    for (var report : reports.entrySet()) {
      var counts = required.contains(report.getKey()) ? requiredCounts : diagnosticCounts;
      for (var check : json.readTree(report.getValue().toFile())) {
        counts.merge(check.path("status").asText(), 1, Integer::sum);
      }
    }
    String summary =
        "MCP 2026-07-28 / conformance 0.2.0-alpha.11\n"
            + "37 required scenarios: "
            + requiredCounts
            + "\n"
            + "13 upstream non-scored diagnostics: "
            + diagnosticCounts
            + "\n"
            + "Full reports: "
            + run
            + "\n";
    Files.writeString(run.resolve("summary.txt"), summary, StandardCharsets.UTF_8);
    System.out.print(summary);
    for (String scenario : required) {
      assertTrue(reports.containsKey(scenario), "Missing required scenario: " + scenario);
      var checks = json.readTree(reports.get(scenario).toFile());
      assertTrue(checks.isArray() && !checks.isEmpty(), "Empty report: " + scenario);
      boolean exercised = false;
      for (var check : checks) {
        String status = check.path("status").asText();
        assertTrue(
            Set.of("SUCCESS", "WARNING", "SKIPPED", "INFO").contains(status),
            () -> scenario + ": " + check);
        if (status.equals("SUCCESS") && !check.path("id").asText().equals("wire-schema-valid")) {
          exercised = true;
        }
      }
      assertTrue(exercised, "Required scenario did not exercise its behavior: " + scenario);
    }
  }

  private static McpServerKit<McpEmptyContext> createKit(Executor executor) {
    var builder =
        ConformanceFixtures.register(
            mcpServerKit("conformance-fixture", "1.0.0", McpEmptyContext.class), executor);
    return InputRequiredFixtures.register(builder)
        .syncTool(
            tool ->
                tool.name("test_missing_capability")
                    .description("Requires the sampling capability")
                    .handler(
                        (context, request, output) -> {
                          // Capability requirements belong to the tool; the kit maps the standard
                          // exception.
                          if (request.meta().clientCapabilities().sampling().isEmpty()) {
                            throw new McpMissingRequiredClientCapabilityException(
                                "This tool requires sampling",
                                mcpClientCapabilities().sampling(Map.of()).build());
                          }
                          return mcpCallToolResult()
                              .resultType("complete")
                              .textContent(text -> text.text("Sampling is available"))
                              .build();
                        }))
        .syncTool(
            tool ->
                tool.name("test_streaming_elicitation")
                    .description("Requests input within a tool result")
                    .handler(
                        (context, request, output) ->
                            mcpInputRequiredResult()
                                .resultType("input_required")
                                .inputRequests(
                                    inputs ->
                                        inputs.elicitRequest(
                                            "confirmation",
                                            elicitation ->
                                                elicitation.elicitRequestFormParams(
                                                    form ->
                                                        form.message("Confirm this request")
                                                            .requestedSchema(
                                                                Map.of(
                                                                    "type",
                                                                    "object",
                                                                    "properties",
                                                                    Map.of())))))
                                .build()))
        // This kit intentionally has no request-scoped logging facility.
        .syncTool(
            tool ->
                tool.name("test_logging_tool")
                    .description("Completes without unsolicited MCP logs")
                    .handler(
                        (context, request, output) ->
                            mcpCallToolResult()
                                .resultType("complete")
                                .textContent(text -> text.text("No MCP log notifications"))
                                .build()))
        .build();
  }

  private static void handle(
      StreamableHttpMcpTransport<McpEmptyContext> transport, HttpExchange exchange)
      throws IOException {
    try (exchange) {
      var request =
          httpMcpRequest()
              .method(exchange.getRequestMethod())
              .headers(exchange.getRequestHeaders())
              .body(exchange.getRequestBody().readAllBytes())
              .build();
      var response =
          transport.handle(McpEmptyContext.INSTANCE, request).toCompletableFuture().join();
      response.headers().forEach(exchange.getResponseHeaders()::set);
      if (response instanceof HttpJsonResponse json) {
        byte[] body = json.body();
        exchange.sendResponseHeaders(json.status(), body.length);
        exchange.getResponseBody().write(body);
      } else if (response instanceof HttpEventStreamResponse stream) {
        try (stream) {
          exchange.sendResponseHeaders(stream.status(), 0);
          stream.writeTo(exchange.getResponseBody());
        }
      } else {
        exchange.sendResponseHeaders(response.status(), -1);
      }
    }
  }
}
