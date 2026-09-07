# MCP Server Kit for Java

[![CI](https://github.com/SuppieRK/mcp-server-kit-java/actions/workflows/ci.yml/badge.svg)](https://github.com/SuppieRK/mcp-server-kit-java/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=suppierk_mcp-server-kit-java&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=suppierk_mcp-server-kit-java)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=suppierk_mcp-server-kit-java&metric=coverage)](https://sonarcloud.io/summary/new_code?id=suppierk_mcp-server-kit-java)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

MCP Server Kit for Java supplies small building blocks for Model Context Protocol (MCP) hosts. The
library implements MCP revision `2026-07-28`. It formats protocol messages and connects typed Java
handlers to transports. The host application owns its server, authentication, and authorization.

## Register a tool

This example registers one synchronous tool with an explicit application-context type.

```java
record RequestContext(String userName) {}

ObjectNode inputSchema = JsonNodeFactory.instance.objectNode()
    .put("type", "object")
    .put("additionalProperties", false);

McpServerKit<RequestContext> serverKit =
    McpServerKit.builder("example", "1.0.0", RequestContext.class)
        .syncTool(
            new McpTool("hello", inputSchema),
            (applicationContext, parameters, handlerContext) ->
                new McpCallToolResult(
                    List.of(
                        new McpTextContent(
                            "Hello, " + applicationContext.userName() + "!"))))
        .build();
```

The host passes one `RequestContext` to `McpServerKit.handle` for each invocation. Use
`McpEmptyContext` when a host has no invocation data. Use `asyncTool` when a handler returns a
`CompletableFuture`.

Use `StdioMcpTransport` for a process transport. Use `StreamableHttpMcpTransport` with a framework
adapter or a framework-native endpoint for HTTP.

## Modules

| Module | Purpose |
| --- | --- |
| `mcp-core` | Protocol types, validation, server registries, and dispatch |
| `mcp-transport-stdio` | Newline-delimited UTF-8 stdio transport |
| `mcp-transport-http` | Framework-neutral Streamable HTTP transport |
| `mcp-spring-webmvc` | Spring WebMVC adapter |
| `mcp-spring-webflux` | Spring WebFlux adapter |
| `mcp-javalin` | Javalin adapter |

The repository also contains executable Spring WebMVC, Spring WebFlux, Javalin, Quarkus, and
Micronaut examples. Each example shows how the host framework owns its HTTP endpoints and security.
The Quarkus and Micronaut examples use the neutral HTTP transport directly.

Published modules and the Spring, Javalin, and Quarkus examples target Java 17 bytecode. The
Micronaut example targets Java 25 because its platform and Gradle plugin require Java 25.

## Install

Use the module that you need. This example adds the core and neutral HTTP modules.

```groovy
dependencies {
    implementation 'io.github.suppierk:mcp-core:2026.07.28.1.0'
    implementation 'io.github.suppierk:mcp-transport-http:2026.07.28.1.0'
}
```

The version has the form `YYYY.MM.DD.GENERATION.PATCH`. The first three fields identify the MCP revision.

## Build

The Gradle daemon and toolchains use Java 25. Gradle downloads a matching JDK when necessary. The
published modules compile with `--release 17`. All examples also compile with `--release 17`, except
the Micronaut example, which compiles with `--release 25`.

```text
./gradlew clean build
```

Use `./gradlew spotlessApply` to format all module source and Gradle files.

## Project information

- [Contributing](CONTRIBUTING.md)
- [Code of conduct](CODE_OF_CONDUCT.md)
- [Security policy](SECURITY.md)

## License

MCP Server Kit for Java is available under the [MIT License](LICENSE).
