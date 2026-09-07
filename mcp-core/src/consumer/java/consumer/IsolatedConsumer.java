package consumer;

import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcNotification;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTool;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpServerKit;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.jar.JarFile;

/** Compiles and runs with the published kit JAR and the JDK alone. */
public final class IsolatedConsumer {
  private IsolatedConsumer() {}

  public static void main(String[] args) throws Exception {
    try (var jar = new JarFile(args[0])) {
      var entries = jar.stream().map(entry -> entry.getName()).toList();
      require(
          entries.stream()
              .filter(name -> name.endsWith(".class"))
              .allMatch(
                  name ->
                      name.replaceFirst("^META-INF/versions/\\d+/", "")
                          .startsWith("io/github/suppierk/mcp/")),
          "Original dependency packages in JAR");
      require(
          entries.contains("io/github/suppierk/mcp/internal/jsv-messages.properties"),
          "Private schema messages");
      require(
          entries.contains("io/github/suppierk/mcp/internal/ucd/RFC5892-appendix-B.txt"),
          "Private Unicode data");
      require(
          entries.contains("io/github/suppierk/mcp/internal/leap_second_dates.csv"),
          "Private leap-second data");
      require(
          entries.stream()
              .noneMatch(
                  name ->
                      name.startsWith("jsv-messages")
                          || name.startsWith("ucd/")
                          || name.startsWith("draft")
                          || name.equals("leap_second_dates.csv")),
          "Unrelocated schema resources");
      for (String entry : entries) {
        if (!entry.startsWith("io/github/suppierk/mcp/")
            || entry.contains("/internal/")
            || !entry.endsWith(".class")) continue;
        Class<?> type =
            Class.forName(
                entry.replace('/', '.').replace(".class", ""),
                false,
                IsolatedConsumer.class.getClassLoader());
        if (!Modifier.isPublic(type.getModifiers())) continue;
        require(!type.toGenericString().contains("jackson"), "Jackson class contract: " + type);
        for (var method : type.getDeclaredMethods()) {
          if (Modifier.isPublic(method.getModifiers())
              || Modifier.isProtected(method.getModifiers())) {
            require(!method.toGenericString().contains("jackson"), "Jackson method: " + method);
            for (var annotation : method.getDeclaredAnnotations())
              require(
                  !annotation.annotationType().getName().contains("jackson"), "Jackson annotation");
          }
        }
        for (var constructor : type.getDeclaredConstructors()) {
          if (Modifier.isPublic(constructor.getModifiers())
              || Modifier.isProtected(constructor.getModifiers()))
            require(!constructor.toGenericString().contains("jackson"), "Jackson constructor");
        }
        for (var field : type.getDeclaredFields()) {
          if (Modifier.isPublic(field.getModifiers()) || Modifier.isProtected(field.getModifiers()))
            require(!field.toGenericString().contains("jackson"), "Jackson field");
        }
        for (var annotation : type.getDeclaredAnnotations())
          require(
              !annotation.annotationType().getName().contains("jackson"),
              "Jackson type annotation");
      }
    }
    if (args.length == 1) {
      for (String original :
          List.of(
              "com.fasterxml.jackson.databind.ObjectMapper",
              "tools.jackson.databind.ObjectMapper",
              "com.networknt.schema.SchemaRegistry")) {
        try {
          Class.forName(original);
          throw new AssertionError("Exposed " + original);
        } catch (ClassNotFoundException expected) {
          /* Dependency is private. */
        }
      }
    } else {
      for (String mapperName :
          List.of(
              "com.fasterxml.jackson.databind.ObjectMapper",
              "tools.jackson.databind.ObjectMapper")) {
        Class<?> mapperType = Class.forName(mapperName);
        Object mapper = mapperType.getConstructor().newInstance();
        Object tree =
            mapperType.getMethod("readTree", String.class).invoke(mapper, "{\"host\":true}");
        require(
            tree.toString().equals("{\"host\":true}"), "Independent host mapper: " + mapperName);
      }
      require(
          Class.forName("com.networknt.schema.SchemaRegistry")
              .getName()
              .equals("com.networknt.schema.SchemaRegistry"),
          "Independent host schema library");
    }
    var schema =
        Map.<String, Object>of(
            "$schema",
            "https://json-schema.org/draft/2020-12/schema",
            "type",
            "object",
            "required",
            List.of("name"),
            "properties",
            Map.of("name", Map.of("$ref", "#/$defs/name")),
            "$defs",
            Map.of("name", Map.of("type", "string")));
    try (var kit =
        McpServerKit.builder("consumer", "1", McpEmptyContext.class)
            .syncTool(
                new McpTool("hello", schema),
                (context, params, invocation) ->
                    new McpCallToolResult(List.of(new McpTextContent("Hello, World!"))))
            .build()) {
      var notice = new JsonRpcNotification("example/notice", Map.of("value", List.of(1, "two")));
      require(notice.equals(kit.decode(kit.encode(notice))), "Protocol round trip");
      String prefix =
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"2026-07-28\",\"io.modelcontextprotocol/clientCapabilities\":{}},\"name\":\"hello\",\"arguments\":";
      var valid = invoke(kit, prefix + "{\"name\":\"Ada\"}}}");
      require(!(valid instanceof JsonRpcErrorResponse), "Successful schema validation");
      require(
          new String(kit.encode(valid), StandardCharsets.UTF_8).contains("Hello, World!"),
          "Tool response");
      var invalid = invoke(kit, prefix + "{\"name\":42}}}");
      require(
          invalid instanceof JsonRpcErrorResponse error && error.code() == -32602,
          "Rejected invalid arguments");
      require(
          !new String(kit.encode(invalid), StandardCharsets.UTF_8).contains("HOST RESOURCE"),
          "Schema messages must not come from the host");
    }
    verifiesDateTimeValidation();
    System.out.println("Packaged consumer passed: " + Path.of(args[0]).getFileName());
  }

  private static void verifiesDateTimeValidation() {
    var schema =
        Map.<String, Object>of(
            "$schema",
            "http://json-schema.org/draft-07/schema#",
            "type",
            "object",
            "required",
            List.of("timestamp"),
            "properties",
            Map.of("timestamp", Map.of("type", "string", "format", "date-time")));
    try (var kit =
        McpServerKit.builder("date-time-consumer", "1", McpEmptyContext.class)
            .syncTool(
                new McpTool("timestamp", schema),
                (context, params, invocation) ->
                    new McpCallToolResult(List.of(new McpTextContent("Timestamp accepted"))))
            .build()) {
      String prefix =
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"2026-07-28\",\"io.modelcontextprotocol/clientCapabilities\":{}},\"name\":\"timestamp\",\"arguments\":{\"timestamp\":\"";
      for (String timestamp : List.of("2024-01-01T00:00:00Z", "2016-12-31T23:59:60Z")) {
        var valid = invoke(kit, prefix + timestamp + "\"}}}");
        require(
            new String(kit.encode(valid), StandardCharsets.UTF_8).contains("Timestamp accepted"),
            "Valid date-time must reach the tool in the packaged runtime: " + timestamp);
      }
      for (String timestamp : List.of("not-a-timestamp", "2017-12-31T23:59:60Z")) {
        var invalid = invoke(kit, prefix + timestamp + "\"}}}");
        require(
            invalid instanceof JsonRpcErrorResponse error && error.code() == -32602,
            "Invalid date-time must be rejected in the packaged runtime: " + timestamp);
      }
    }
  }

  private static JsonRpcMessage invoke(McpServerKit<McpEmptyContext> kit, String wire) {
    var result = new Result();
    kit.handle(McpEmptyContext.INSTANCE, kit.decode(wire.getBytes(StandardCharsets.UTF_8)))
        .subscribe(result);
    require(result.complete && result.value != null, "Terminal response");
    return result.value;
  }

  private static void require(boolean condition, String description) {
    if (!condition) throw new AssertionError(description);
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
    public void onError(Throwable error) {
      throw new AssertionError(error);
    }

    @Override
    public void onComplete() {
      complete = true;
    }
  }
}
