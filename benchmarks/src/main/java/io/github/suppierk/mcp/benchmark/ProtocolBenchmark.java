package io.github.suppierk.mcp.benchmark;

import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTool;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ProtocolBenchmark {
  @Param({"list", "hello", "nested"})
  public String scenario;

  private McpServerKit<McpEmptyContext> kit;
  private byte[] input;
  private JsonRpcMessage response;

  @Setup
  public void setup() {
    var schema = Map.<String, Object>of("type", "object");
    var nested =
        Map.<String, Object>of(
            "type",
            "object",
            "required",
            List.of("items"),
            "properties",
            Map.of(
                "items",
                Map.of(
                    "type",
                    "array",
                    "items",
                    Map.of(
                        "type",
                        "object",
                        "required",
                        List.of("name", "count"),
                        "properties",
                        Map.of(
                            "name",
                            Map.of("type", "string"),
                            "count",
                            Map.of("type", "integer"))))));
    var result =
        McpCallToolResult.mcpCallToolResult()
            .resultType("complete")
            .content(List.of(new McpTextContent("Hello, World!")))
            .build();
    kit =
        McpServerKit.builder("benchmark", "1", McpEmptyContext.class)
            .syncTool(new McpTool("hello", schema), (context, params, invocation) -> result)
            .syncTool(
                McpTool.mcpTool().name("nested").inputSchema(nested).outputSchema(nested).build(),
                (context, params, invocation) ->
                    McpCallToolResult.mcpCallToolResult()
                        .resultType("complete")
                        .content(List.of(new McpTextContent("items")))
                        .structuredContent(params.arguments().orElseThrow())
                        .build())
            .build();
    String json =
        switch (scenario) {
          case "list" -> "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}";
          case "hello" ->
              "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"hello\",\"arguments\":{}}}";
          default ->
              "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"nested\",\"arguments\":{\"items\":[{\"name\":\"one\",\"count\":1},{\"name\":\"two\",\"count\":2}]}}}";
        };
    json =
        json.replace(
                "\"params\":{",
                "\"params\":{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"2026-07-28\",\"io.modelcontextprotocol/clientCapabilities\":{}},")
            .replace(",}", "}");
    input = json.getBytes(StandardCharsets.UTF_8);
    response = invoke(kit.decode(input));
    if (response instanceof JsonRpcErrorResponse
        || new String(kit.encode(response), StandardCharsets.UTF_8).contains("\"isError\":true")) {
      throw new IllegalStateException("Benchmark must measure successful requests: " + response);
    }
  }

  @Benchmark
  public JsonRpcMessage decode() {
    return kit.decode(input);
  }

  @Benchmark
  public byte[] encode() {
    return kit.encode(response);
  }

  @Benchmark
  public byte[] roundTrip() {
    return kit.encode(invoke(kit.decode(input)));
  }

  private JsonRpcMessage invoke(JsonRpcMessage request) {
    var subscriber = new Result();
    kit.handle(McpEmptyContext.INSTANCE, request).subscribe(subscriber);
    if (!subscriber.complete || subscriber.value == null) {
      throw new IllegalStateException("Expected a synchronous terminal response");
    }
    return subscriber.value;
  }

  @TearDown
  public void close() {
    kit.close();
  }

  private static final class Result implements Flow.Subscriber<JsonRpcMessage> {
    private JsonRpcMessage value;
    private boolean complete;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(JsonRpcMessage message) {
      value = message;
    }

    @Override
    public void onError(Throwable failure) {
      throw new IllegalStateException(failure);
    }

    @Override
    public void onComplete() {
      complete = true;
    }
  }
}
