package io.github.suppierk.mcp.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.suppierk.mcp.internal.JsonValues;
import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcNotification;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.JsonRpcResponse;
import io.github.suppierk.mcp.protocol.JsonRpcResultResponse;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpCallToolRequestParams;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpCallToolResultResponse;
import io.github.suppierk.mcp.protocol.McpClientNotification;
import io.github.suppierk.mcp.protocol.McpClientRequest;
import io.github.suppierk.mcp.protocol.McpCompleteRequest;
import io.github.suppierk.mcp.protocol.McpCompleteRequestParams;
import io.github.suppierk.mcp.protocol.McpCompleteResult;
import io.github.suppierk.mcp.protocol.McpCompleteResultResponse;
import io.github.suppierk.mcp.protocol.McpDiscoverRequest;
import io.github.suppierk.mcp.protocol.McpDiscoverResult;
import io.github.suppierk.mcp.protocol.McpDiscoverResultResponse;
import io.github.suppierk.mcp.protocol.McpGetPromptRequest;
import io.github.suppierk.mcp.protocol.McpGetPromptRequestParams;
import io.github.suppierk.mcp.protocol.McpGetPromptResultResponse;
import io.github.suppierk.mcp.protocol.McpIcon;
import io.github.suppierk.mcp.protocol.McpImplementation;
import io.github.suppierk.mcp.protocol.McpJsonNull;
import io.github.suppierk.mcp.protocol.McpJsonSchema;
import io.github.suppierk.mcp.protocol.McpListPromptsRequest;
import io.github.suppierk.mcp.protocol.McpListPromptsResult;
import io.github.suppierk.mcp.protocol.McpListPromptsResultResponse;
import io.github.suppierk.mcp.protocol.McpListResourceTemplatesRequest;
import io.github.suppierk.mcp.protocol.McpListResourceTemplatesResult;
import io.github.suppierk.mcp.protocol.McpListResourceTemplatesResultResponse;
import io.github.suppierk.mcp.protocol.McpListResourcesRequest;
import io.github.suppierk.mcp.protocol.McpListResourcesResult;
import io.github.suppierk.mcp.protocol.McpListResourcesResultResponse;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpListToolsResult;
import io.github.suppierk.mcp.protocol.McpListToolsResultResponse;
import io.github.suppierk.mcp.protocol.McpMetaObject;
import io.github.suppierk.mcp.protocol.McpNotificationMetaObject;
import io.github.suppierk.mcp.protocol.McpProgressNotification;
import io.github.suppierk.mcp.protocol.McpProgressNotificationParams;
import io.github.suppierk.mcp.protocol.McpPrompt;
import io.github.suppierk.mcp.protocol.McpPromptArgument;
import io.github.suppierk.mcp.protocol.McpPromptListChangedNotification;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.protocol.McpReadResourceRequest;
import io.github.suppierk.mcp.protocol.McpReadResourceRequestParams;
import io.github.suppierk.mcp.protocol.McpReadResourceResultResponse;
import io.github.suppierk.mcp.protocol.McpResource;
import io.github.suppierk.mcp.protocol.McpResourceListChangedNotification;
import io.github.suppierk.mcp.protocol.McpResourceTemplate;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotification;
import io.github.suppierk.mcp.protocol.McpResultMetaObject;
import io.github.suppierk.mcp.protocol.McpServerCapabilities;
import io.github.suppierk.mcp.protocol.McpSubscriptionFilter;
import io.github.suppierk.mcp.protocol.McpSubscriptionNotification;
import io.github.suppierk.mcp.protocol.McpSubscriptionsAcknowledgedNotification;
import io.github.suppierk.mcp.protocol.McpSubscriptionsAcknowledgedNotificationParams;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenRequest;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenResult;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenResultMetaObject;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenResultResponse;
import io.github.suppierk.mcp.protocol.McpToolAnnotations;
import io.github.suppierk.mcp.protocol.McpToolListChangedNotification;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Processes MCP messages through an immutable set of application registrations.
 *
 * @param <C> the application-context type
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/server">MCP server
 *     features</a>
 */
public final class McpServerKit<C> implements AutoCloseable {
  private static final System.Logger LOGGER = System.getLogger(McpServerKit.class.getName());

  private final String name;
  private final String version;
  private final Optional<String> title;
  private final Optional<String> description;
  private final Optional<URI> websiteUrl;
  private final Optional<String> instructions;
  private final List<McpIcon> icons;
  private final ObjectMapper mapper;
  private final JsonSchemaValidator schemaValidator;
  private final Map<String, ToolRegistration<C>> tools;
  private final Map<String, ResourceRegistration<C>> resources;
  private final Map<Pattern, ResourceTemplateRegistration<C>> resourceTemplates;
  private final Map<String, PromptRegistration<C>> prompts;
  private final RegisteredHandler<C, McpCompleteRequestParams, McpCompleteResult> completion;
  private final Map<String, RegisteredHandler<C, JsonRpcRequest, JsonRpcResponse>> methods;
  private final Map<String, JsonSchemaValidator.Compiled> toolInputs;
  private final Map<String, JsonSchemaValidator.Compiled> toolOutputs;
  private final Set<Output> activeRequests = ConcurrentHashMap.newKeySet();
  private final CopyOnWriteArrayList<ActiveSubscription> subscriptions =
      new CopyOnWriteArrayList<>();
  private final AtomicBoolean closed = new AtomicBoolean();

  /** Creates one server kit from a validated builder. */
  private McpServerKit(Builder<C> builder) {
    name = builder.name;
    version = builder.version;
    title = builder.title;
    description = builder.description;
    websiteUrl = builder.websiteUrl;
    instructions = builder.instructions;
    icons = List.copyOf(builder.icons);
    mapper = builder.mapper;
    schemaValidator = new JsonSchemaValidator(mapper);
    tools = immutableMap(builder.tools);
    resources = immutableMap(builder.resources);
    resourceTemplates = immutableMap(builder.resourceTemplates);
    prompts = immutableMap(builder.prompts);
    completion = builder.completion;
    methods = immutableMap(builder.methods);
    rejectBuiltInConflicts();
    LinkedHashMap<String, JsonSchemaValidator.Compiled> inputs = new LinkedHashMap<>();
    LinkedHashMap<String, JsonSchemaValidator.Compiled> outputs = new LinkedHashMap<>();
    tools.forEach(
        (toolName, registration) -> {
          inputs.put(toolName, schemaValidator.compile(registration.inputSchema()));
          registration
              .outputSchema()
              .ifPresent(schema -> outputs.put(toolName, schemaValidator.compile(schema)));
        });
    toolInputs = immutableMap(inputs);
    toolOutputs = immutableMap(outputs);
  }

  /**
   * Creates a builder with the required server identity and application-context type.
   *
   * @param name the programmatic server name
   * @param version the server implementation version
   * @param applicationContextType the application-context type used for generic inference
   * @param <C> the application-context type
   * @return a new builder
   */
  public static <C> Builder<C> mcpServerKit(
      String name, String version, Class<C> applicationContextType) {
    Objects.requireNonNull(applicationContextType, "applicationContextType");
    return new Builder<>(name, version);
  }

  /**
   * Decodes one UTF-8 JSON-RPC message.
   *
   * <p>A malformed value produces a JSON-RPC error response. It does not throw a protocol
   * exception.
   *
   * @param input the UTF-8 JSON bytes
   * @return the decoded message or protocol error response
   * @throws NullPointerException if {@code input} is {@code null}
   */
  public JsonRpcMessage decode(byte[] input) {
    Objects.requireNonNull(input, "input");
    final JsonNode value;
    try {
      value = mapper.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(input);
    } catch (JacksonException exception) {
      return error(NullNode.getInstance(), McpParseException.CODE, "Parse error");
    }
    if (!(value instanceof ObjectNode object)) {
      return error(NullNode.getInstance(), McpParseException.CODE, "Parse error");
    }
    if (!McpProtocol.JSON_RPC_VERSION.equals(object.path("jsonrpc").stringValue(null))) {
      return error(
          NullNode.getInstance(),
          McpInvalidRequestException.CODE,
          "The message must be a JSON-RPC 2.0 object");
    }
    return object.has("method") ? decodeCall(object) : decodeResponse(object);
  }

  /**
   * Encodes one JSON-RPC message as UTF-8 JSON.
   *
   * @param message the message to encode
   * @return the encoded bytes
   * @throws IllegalArgumentException if the message cannot be encoded as JSON
   * @throws NullPointerException if {@code message} is {@code null}
   */
  public byte[] encode(JsonRpcMessage message) {
    Objects.requireNonNull(message, "message");
    try {
      ObjectNode object = mapper.createObjectNode().put("jsonrpc", McpProtocol.JSON_RPC_VERSION);
      ObjectNode messageObject = mapper.valueToTree(message);
      object.setAll(messageObject);
      return mapper.writeValueAsBytes(object);
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("The message cannot be encoded", exception);
    }
  }

  /**
   * Gets a copy of one registered tool's input schema.
   *
   * <p>This lookup lets transports validate metadata derived from a tool declaration without
   * exposing the registration or its application handler.
   *
   * @param toolName the exact tool name
   * @return the copied schema, or empty when the tool is not registered
   */
  public Optional<Map<String, ?>> toolInputSchema(String toolName) {
    ToolRegistration<C> registration = tools.get(Objects.requireNonNull(toolName, "toolName"));
    return registration == null ? Optional.empty() : Optional.of(registration.inputSchema());
  }

  /**
   * Handles one decoded message.
   *
   * <p>The returned publication is lazy and accepts one subscriber. Processing starts when that
   * subscriber makes its first positive demand. Cancellation before that demand invokes no
   * application handler. Each publication has an independent lifecycle, even when request IDs
   * match. Transports own cancellation notifications; callers cancel this publication through its
   * {@link Flow.Subscription}.
   *
   * <p>A protocol exception becomes its fixed error response. Another handler exception becomes a
   * generic internal-error response. A handler {@link Error} or an infrastructure failure
   * terminates the publication with {@code onError}.
   *
   * @param applicationContext application-owned invocation data
   * @param message the decoded message
   * @return the message publication
   * @throws NullPointerException if either argument is {@code null}
   */
  public Flow.Publisher<JsonRpcMessage> handle(C applicationContext, JsonRpcMessage message) {
    Objects.requireNonNull(applicationContext, "applicationContext");
    Objects.requireNonNull(message, "message");
    AtomicBoolean subscribed = new AtomicBoolean();
    return subscriber -> {
      Objects.requireNonNull(subscriber, "subscriber");
      if (!subscribed.compareAndSet(false, true)) {
        rejectSecondSubscriber(subscriber);
        return;
      }
      new Output(
          subscriber,
          output -> {
            if (closed.get()) {
              output.fail(new IllegalStateException("The MCP server kit is closed"));
            } else {
              process(applicationContext, message, output);
            }
          });
    };
  }

  /**
   * Sends one server notification to matching active subscriptions.
   *
   * @param notification the notification to send
   * @throws IllegalStateException if the server kit is closed
   */
  public void emit(McpSubscriptionNotification notification) {
    Objects.requireNonNull(notification, "notification");
    if (closed.get()) {
      throw new IllegalStateException("The MCP server kit is closed");
    }
    subscriptions.stream()
        .filter(subscription -> subscription.accepts(notification))
        .forEach(subscription -> subscription.emit(notification, mapper));
  }

  /** Closes all active publications. */
  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      McpImplementation implementation = serverInfo();
      List.copyOf(subscriptions)
          .forEach(subscription -> subscription.closeGracefully(implementation));
      activeRequests.forEach(Output::cancel);
      subscriptions.clear();
    }
  }

  /** Processes one message after a subscriber attaches. */
  private void process(C applicationContext, JsonRpcMessage message, Output output) {
    try {
      if (message instanceof McpClientNotification || message instanceof JsonRpcNotification) {
        output.close();
        return;
      }
      if (message instanceof JsonRpcResponse response) {
        if (response instanceof JsonRpcErrorResponse errorResponse
            && (errorResponse.code() == McpInvalidParamsException.CODE
                || response.id() == McpJsonNull.INSTANCE
                    && (errorResponse.code() == McpParseException.CODE
                        || errorResponse.code() == McpInvalidRequestException.CODE))) {
          output.emit(response);
        } else {
          output.emit(
              error(response.id(), McpInvalidRequestException.CODE, "A server needs a request"));
        }
        output.close();
        return;
      }
      JsonRpcRequest request =
          message instanceof JsonRpcRequest generic
              ? generic
              : genericRequest((McpClientRequest) message);
      Object metadata = request.params().get("_meta");
      output.progressToken(
          metadata instanceof Map<?, ?> fields ? fields.get("progressToken") : null);
      activeRequests.add(output);
      output.onClose(() -> activeRequests.remove(output));
      if (closed.get()) {
        output.cancel();
        return;
      }
      JsonRpcResponse response;
      try {
        response = dispatch(applicationContext, request, output);
      } catch (McpProtocolException exception) {
        response = protocolError(request.id(), exception);
      }
      if (response != null) {
        output.complete(response);
      }
    } catch (RuntimeException exception) {
      output.fail(exception);
    }
  }

  /** Dispatches one request or opens one subscription. */
  private JsonRpcResponse dispatch(C applicationContext, JsonRpcRequest request, Output output) {
    JsonRpcErrorResponse metadataFailure = validateMetadata(request);
    if (metadataFailure != null) {
      return metadataFailure;
    }
    return switch (request.method()) {
      case McpDiscoverRequest.METHOD -> new McpDiscoverResultResponse(request.id(), discover());
      case McpListToolsRequest.METHOD ->
          tools.isEmpty()
              ? methodNotFound(request)
              : new McpListToolsResultResponse(
                  request.id(),
                  new McpListToolsResult(
                      resultMeta(),
                      "private",
                      Optional.empty(),
                      "complete",
                      tools.values().stream().map(ToolRegistration::declaration).toList(),
                      0L));
      case McpCallToolRequest.METHOD ->
          tools.isEmpty() ? methodNotFound(request) : callTool(applicationContext, request, output);
      case McpListResourcesRequest.METHOD ->
          resources.isEmpty() && resourceTemplates.isEmpty()
              ? methodNotFound(request)
              : new McpListResourcesResultResponse(
                  request.id(),
                  new McpListResourcesResult(
                      resultMeta(),
                      "private",
                      Optional.empty(),
                      resources.values().stream().map(ResourceRegistration::declaration).toList(),
                      "complete",
                      0L));
      case McpListResourceTemplatesRequest.METHOD ->
          resourceTemplates.isEmpty()
              ? methodNotFound(request)
              : new McpListResourceTemplatesResultResponse(
                  request.id(),
                  new McpListResourceTemplatesResult(
                      resultMeta(),
                      "private",
                      Optional.empty(),
                      resourceTemplates.values().stream()
                          .map(ResourceTemplateRegistration::declaration)
                          .toList(),
                      "complete",
                      0L));
      case McpReadResourceRequest.METHOD ->
          resources.isEmpty() && resourceTemplates.isEmpty()
              ? methodNotFound(request)
              : readResource(applicationContext, request, output);
      case McpSubscriptionsListenRequest.METHOD ->
          tools.isEmpty() && prompts.isEmpty() && resources.isEmpty() && resourceTemplates.isEmpty()
              ? methodNotFound(request)
              : listen(request, output);
      case McpListPromptsRequest.METHOD ->
          prompts.isEmpty()
              ? methodNotFound(request)
              : new McpListPromptsResultResponse(
                  request.id(),
                  new McpListPromptsResult(
                      resultMeta(),
                      "private",
                      Optional.empty(),
                      prompts.values().stream().map(PromptRegistration::declaration).toList(),
                      "complete",
                      0L));
      case McpGetPromptRequest.METHOD ->
          prompts.isEmpty()
              ? methodNotFound(request)
              : getPrompt(applicationContext, request, output);
      case McpCompleteRequest.METHOD -> complete(applicationContext, request, output);
      default -> invoke(applicationContext, methods.get(request.method()), request, output);
    };
  }

  /** Validates the required per-request MCP metadata. */
  private JsonRpcErrorResponse validateMetadata(JsonRpcRequest request) {
    if (!(request.params().get("_meta") instanceof Map<?, ?> metadata)) {
      return error(
          request.id(), McpInvalidParamsException.CODE, "The request needs a _meta object");
    }
    Object protocolVersion = metadata.get(McpProtocol.PROTOCOL_VERSION_KEY);
    if (!(protocolVersion instanceof String)) {
      return error(
          request.id(), McpInvalidParamsException.CODE, "The request needs a protocol version");
    }
    if (!(metadata.get(McpProtocol.CLIENT_CAPABILITIES_KEY) instanceof Map<?, ?>)) {
      return error(
          request.id(),
          McpInvalidParamsException.CODE,
          "The request needs a client capabilities object");
    }
    if (!McpProtocol.REVISION.equals(protocolVersion)) {
      var data = Map.of("requested", protocolVersion, "supported", List.of(McpProtocol.REVISION));
      return new JsonRpcErrorResponse(
          request.id(),
          McpUnsupportedProtocolVersionException.CODE,
          "Unsupported protocol version",
          Optional.of(data));
    }
    return null;
  }

  /** Calls one registered tool. */
  private JsonRpcResponse callTool(C applicationContext, JsonRpcRequest request, Output output) {
    Object nameNode = request.params().get("name");
    if (!(nameNode instanceof String toolName)) {
      return invalidParams(request, "A tool name is required");
    }
    ToolRegistration<C> registration = tools.get(toolName);
    if (registration == null) {
      return invalidParams(request, "Unknown tool");
    }
    Map<?, ?> arguments =
        request.params().get("arguments") instanceof Map<?, ?> supplied ? supplied : Map.of();
    List<String> inputFailures = toolInputs.get(toolName).validate(arguments);
    if (!inputFailures.isEmpty()) {
      return validationError(
          request, McpInvalidParamsException.CODE, "Tool input is invalid", inputFailures);
    }
    McpCallToolRequestParams parameters;
    try {
      parameters = mapper.convertValue(request.params(), McpCallToolRequestParams.class);
    } catch (IllegalArgumentException | JacksonException exception) {
      return invalidParams(request, "Tool parameters are invalid");
    }
    invokeHandler(
        applicationContext,
        parameters,
        registration.handler(),
        request,
        output,
        result -> toolResponse(request, toolName, result));
    return null;
  }

  /** Creates and validates one tool response. */
  private JsonRpcResponse toolResponse(
      JsonRpcRequest request, String toolName, McpCallToolResultResponse.Result result) {
    McpCallToolResultResponse response = new McpCallToolResultResponse(request.id(), result);
    JsonSchemaValidator.Compiled outputSchema = toolOutputs.get(toolName);
    if (outputSchema == null
        || !(result instanceof McpCallToolResult toolResult)
        || toolResult.isError().orElse(false)) {
      return response;
    }
    Optional<Object> structured = toolResult.structuredContent();
    if (structured.isEmpty()) {
      return error(request.id(), McpInternalException.CODE, "Tool structured output is missing");
    }
    List<String> outputFailures = outputSchema.validate(structured.get());
    return outputFailures.isEmpty()
        ? response
        : validationError(
            request,
            McpInternalException.CODE,
            "Tool structured output is invalid",
            outputFailures);
  }

  /** Reads one exact or pattern-matched resource. */
  private JsonRpcResponse readResource(
      C applicationContext, JsonRpcRequest request, Output output) {
    JsonNode uriNode = mapper.valueToTree(request.params().get("uri"));
    if (uriNode == null || !uriNode.isString()) {
      return invalidParams(request, "A resource URI is required");
    }
    McpReadResourceRequestParams parameters;
    try {
      parameters = mapper.convertValue(request.params(), McpReadResourceRequestParams.class);
    } catch (IllegalArgumentException | JacksonException exception) {
      return invalidParams(request, "Resource parameters are invalid");
    }
    ResourceRegistration<C> exact = resources.get(uriNode.stringValue());
    if (exact != null) {
      invokeHandler(
          applicationContext,
          parameters,
          exact.handler(),
          request,
          output,
          result -> new McpReadResourceResultResponse(request.id(), result));
      return null;
    }
    Optional<ResourceTemplateRegistration<C>> template =
        resourceTemplates.entrySet().stream()
            .filter(entry -> entry.getKey().matcher(uriNode.stringValue()).matches())
            .map(Map.Entry::getValue)
            .findFirst();
    if (template.isEmpty()) {
      return invalidParams(request, "Unknown resource URI");
    }
    invokeHandler(
        applicationContext,
        parameters,
        template.get().handler(),
        request,
        output,
        result -> new McpReadResourceResultResponse(request.id(), result));
    return null;
  }

  /** Gets one prompt after it validates string arguments. */
  private JsonRpcResponse getPrompt(C applicationContext, JsonRpcRequest request, Output output) {
    JsonNode nameNode = mapper.valueToTree(request.params().get("name"));
    if (nameNode == null || !nameNode.isString()) {
      return invalidParams(request, "A prompt name is required");
    }
    PromptRegistration<C> registration = prompts.get(nameNode.stringValue());
    if (registration == null) {
      return invalidParams(request, "Unknown prompt");
    }
    McpGetPromptRequestParams parameters;
    try {
      parameters = mapper.convertValue(request.params(), McpGetPromptRequestParams.class);
    } catch (IllegalArgumentException | JacksonException exception) {
      return invalidParams(request, "Prompt parameters are invalid");
    }
    Map<String, ?> arguments = parameters.arguments().orElseGet(Map::of);
    if (arguments.values().stream().anyMatch(value -> !(value instanceof String))) {
      return invalidParams(request, "Prompt arguments must contain string values");
    }
    Optional<McpPromptArgument> missing =
        registration.declaration().arguments().orElse(List.of()).stream()
            .filter(argument -> argument.required().orElse(false))
            .filter(argument -> !arguments.containsKey(argument.name()))
            .findFirst();
    if (missing.isPresent()) {
      return invalidParams(
          request, "A required prompt argument is absent: " + missing.get().name());
    }
    invokeHandler(
        applicationContext,
        parameters,
        registration.handler(),
        request,
        output,
        result -> new McpGetPromptResultResponse(request.id(), result));
    return null;
  }

  /** Completes one prompt or resource argument. */
  private JsonRpcResponse complete(C applicationContext, JsonRpcRequest request, Output output) {
    if (completion == null) {
      return methodNotFound(request);
    }
    McpCompleteRequestParams parameters;
    try {
      parameters = mapper.convertValue(request.params(), McpCompleteRequestParams.class);
    } catch (IllegalArgumentException | JacksonException exception) {
      return invalidParams(request, "Completion parameters are invalid");
    }
    invokeHandler(
        applicationContext,
        parameters,
        completion,
        request,
        output,
        result -> new McpCompleteResultResponse(request.id(), result));
    return null;
  }

  /** Opens one filtered subscription publication. */
  private JsonRpcResponse listen(JsonRpcRequest request, Output output) {
    if (!(request.params().get("notifications") instanceof Map<?, ?> requested)) {
      return invalidParams(request, "A subscription notification filter is required");
    }
    Set<String> resourceUris = new HashSet<>();
    Object requestedUris = requested.get("resourceSubscriptions");
    if (requestedUris != null) {
      if (!(requestedUris instanceof List<?> uris)
          || !uris.stream().allMatch(String.class::isInstance)) {
        return invalidParams(request, "Resource subscriptions must contain URI strings");
      }
      uris.forEach(uri -> resourceUris.add((String) uri));
    }
    ActiveSubscription subscription =
        new ActiveSubscription(
            request.id(),
            Boolean.TRUE.equals(requested.get("toolsListChanged")),
            Boolean.TRUE.equals(requested.get("resourcesListChanged")),
            Boolean.TRUE.equals(requested.get("promptsListChanged")),
            Set.copyOf(resourceUris),
            output);
    McpSubscriptionFilter accepted =
        new McpSubscriptionFilter(
            subscription.promptsChanged() ? Optional.of(true) : Optional.empty(),
            resourceUris.isEmpty()
                ? Optional.empty()
                : Optional.of(resourceUris.stream().sorted().toList()),
            subscription.resourcesChanged() ? Optional.of(true) : Optional.empty(),
            subscription.toolsChanged() ? Optional.of(true) : Optional.empty());
    output.enqueue(
        new McpSubscriptionsAcknowledgedNotification(
            new McpSubscriptionsAcknowledgedNotificationParams(
                Optional.of(new McpNotificationMetaObject(Optional.of(request.id()))), accepted)));
    subscriptions.add(subscription);
    output.onClose(() -> subscriptions.remove(subscription));
    output.drain();
    return null;
  }

  /** Calls one application function and checks its response contract. */
  private JsonRpcResponse invoke(
      C applicationContext,
      RegisteredHandler<C, JsonRpcRequest, JsonRpcResponse> handler,
      JsonRpcRequest request,
      Output output) {
    if (handler == null) {
      return methodNotFound(request);
    }
    JsonRpcRequest copy = request;
    invokeHandler(
        applicationContext,
        copy,
        handler,
        request,
        output,
        response ->
            request.id().equals(response.id())
                ? response
                : protocolError(
                    request.id(), new McpInternalException("Internal error", Optional.empty())));
    return null;
  }

  /** Invokes one normalized handler and publishes its terminal response. */
  private <P, R> void invokeHandler(
      C applicationContext,
      P parameters,
      RegisteredHandler<C, P, R> handler,
      JsonRpcRequest request,
      Output output,
      Function<R, JsonRpcResponse> responseFactory) {
    CompletableFuture<? extends R> future;
    try {
      future = Objects.requireNonNull(handler.handle(applicationContext, parameters, output));
    } catch (Throwable failure) {
      acceptHandlerFailure(request, output, failure);
      return;
    }
    output.track(future);
    future.whenComplete(
        (result, failure) -> {
          if (failure != null) {
            acceptHandlerFailure(request, output, failure);
            return;
          }
          try {
            R required = Objects.requireNonNull(result, "handler result");
            output.complete(
                Objects.requireNonNull(responseFactory.apply(required), "handler response"));
          } catch (Throwable responseFailure) {
            acceptHandlerFailure(request, output, responseFailure);
          }
        });
  }

  /** Maps one application-handler failure to its public lifecycle outcome. */
  private void acceptHandlerFailure(
      JsonRpcRequest request, Output output, Throwable suppliedFailure) {
    if (output.isCancelled()) {
      return;
    }
    Throwable failure = unwrapHandlerFailure(suppliedFailure);
    if (failure instanceof McpProtocolException protocolException) {
      output.complete(protocolError(request.id(), protocolException));
    } else if (failure instanceof Error error) {
      output.fail(error);
    } else {
      LOGGER.log(
          System.Logger.Level.ERROR,
          "Unexpected MCP handler exception [method="
              + request.method()
              + ", requestId="
              + renderRequestId(request.id())
              + "]",
          failure);
      output.complete(
          protocolError(
              request.id(), new McpInternalException("Internal error", Optional.empty())));
    }
  }

  /** Removes only the asynchronous wrapper types approved by the public contract. */
  private static Throwable unwrapHandlerFailure(Throwable suppliedFailure) {
    Throwable failure = Objects.requireNonNull(suppliedFailure, "suppliedFailure");
    while ((failure instanceof CompletionException || failure instanceof ExecutionException)
        && failure.getCause() != null) {
      failure = failure.getCause();
    }
    return failure;
  }

  /** Renders an escaped and bounded request identifier for failure logging. */
  private String renderRequestId(Object requestId) {
    String rendered;
    try {
      rendered = mapper.writeValueAsString(requestId);
    } catch (JacksonException exception) {
      rendered = "\"unrenderable\"";
    }
    return rendered.length() <= 256 ? rendered : rendered.substring(0, 256);
  }

  /** Creates the discovery result. */
  private McpDiscoverResult discover() {
    return new McpDiscoverResult(
        resultMeta(),
        "private",
        capabilities(),
        instructions,
        "complete",
        List.of(McpProtocol.REVISION),
        0L);
  }

  /** Creates response metadata with server identity. */
  private Optional<McpResultMetaObject> resultMeta() {
    return Optional.of(new McpResultMetaObject(Optional.of(serverInfo())));
  }

  /** Creates the server implementation declaration. */
  private McpImplementation serverInfo() {
    return new McpImplementation(
        description,
        icons.isEmpty() ? Optional.empty() : Optional.of(icons),
        name,
        title,
        version,
        websiteUrl);
  }

  /** Creates advertised capabilities from immutable registrations. */
  private McpServerCapabilities capabilities() {
    Optional<Map<String, ?>> completionCapability =
        completion == null ? Optional.empty() : Optional.of(Map.of());
    Optional<Map<String, ?>> toolCapability =
        tools.isEmpty()
                && methods.keySet().stream().noneMatch(method -> method.startsWith("tools/"))
            ? Optional.empty()
            : Optional.of(Map.of("listChanged", false));
    Optional<Map<String, ?>> resourceCapability =
        resources.isEmpty()
                && resourceTemplates.isEmpty()
                && methods.keySet().stream().noneMatch(method -> method.startsWith("resources/"))
            ? Optional.empty()
            : Optional.of(Map.of("listChanged", false, "subscribe", true));
    Optional<Map<String, ?>> promptCapability =
        prompts.isEmpty()
                && methods.keySet().stream().noneMatch(method -> method.startsWith("prompts/"))
            ? Optional.empty()
            : Optional.of(Map.of("listChanged", false));
    return new McpServerCapabilities(
        completionCapability,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        promptCapability,
        resourceCapability,
        toolCapability);
  }

  /** Creates one successful response. */
  private static JsonRpcResultResponse success(JsonRpcRequest request, JsonNode result) {
    return new JsonRpcResultResponse(request.id(), ProtocolJson.value(result));
  }

  /** Creates one method-not-found response. */
  private static JsonRpcErrorResponse methodNotFound(JsonRpcRequest request) {
    return error(request.id(), McpMethodNotFoundException.CODE, "Method not found");
  }

  /** Creates one invalid-parameters response. */
  private static JsonRpcErrorResponse invalidParams(JsonRpcRequest request, String message) {
    return error(request.id(), McpInvalidParamsException.CODE, message);
  }

  /** Creates one error response without data. */
  private static JsonRpcErrorResponse error(Object id, int code, String message) {
    return new JsonRpcErrorResponse(ProtocolJson.value(id), code, message, Optional.empty());
  }

  /** Converts one deliberate protocol exception to a correlated wire response. */
  private JsonRpcErrorResponse protocolError(Object id, McpProtocolException exception) {
    return new JsonRpcErrorResponse(
        ProtocolJson.value(id), exception.code(), exception.getMessage(), protocolData(exception));
  }

  /** Converts the concrete exception's data contract to its wire representation. */
  private Optional<Object> protocolData(McpProtocolException exception) {
    if (exception instanceof McpHeaderMismatchException failure) {
      return failure.data();
    }
    if (exception instanceof McpInternalException failure) {
      return failure.data();
    }
    if (exception instanceof McpInvalidParamsException failure) {
      return failure.data();
    }
    if (exception instanceof McpInvalidRequestException failure) {
      return failure.data();
    }
    if (exception instanceof McpMethodNotFoundException failure) {
      return failure.data();
    }
    if (exception instanceof McpParseException failure) {
      return failure.data();
    }
    if (exception instanceof McpMissingRequiredClientCapabilityException failure) {
      return Optional.of(
          Map.of(
              "requiredCapabilities",
              ProtocolJson.value(mapper.valueToTree(failure.requiredCapabilities()))));
    }
    McpUnsupportedProtocolVersionException failure =
        (McpUnsupportedProtocolVersionException) exception;
    return Optional.of(
        Map.of(
            "requested", failure.requestedRevision(), "supported", failure.supportedRevisions()));
  }

  /** Creates one validation error response. */
  private JsonRpcErrorResponse validationError(
      JsonRpcRequest request, int code, String message, List<String> failures) {
    var data = Map.of("violations", failures);
    return new JsonRpcErrorResponse(request.id(), code, message, Optional.of(data));
  }

  /** Decodes one request or notification. */
  private JsonRpcMessage decodeCall(ObjectNode object) {
    JsonNode method = object.get("method");
    if (method == null || !method.isString() || method.stringValue().isBlank()) {
      return error(
          NullNode.getInstance(), McpInvalidRequestException.CODE, "The method must be text");
    }
    ObjectNode params = mapper.createObjectNode();
    if (object.has("params")) {
      if (!(object.get("params") instanceof ObjectNode supplied)) {
        return error(
            NullNode.getInstance(),
            McpInvalidRequestException.CODE,
            "The params value must be an object");
      }
      params = supplied.deepCopy();
    }
    if (!object.has("id")) {
      if (McpClientNotification.METHOD.equals(method.stringValue())) {
        return typedMessage(object, McpClientNotification.class);
      }
      return new JsonRpcNotification(method.stringValue(), ProtocolJson.object(params));
    }
    JsonNode id = object.get("id");
    if (id == null || (!id.isString() && !id.isIntegralNumber())) {
      return error(
          NullNode.getInstance(),
          McpInvalidRequestException.CODE,
          "The request ID must be text or an integer");
    }
    return switch (method.stringValue()) {
      case McpDiscoverRequest.METHOD -> typedMessage(object, McpDiscoverRequest.class);
      case McpListToolsRequest.METHOD -> typedMessage(object, McpListToolsRequest.class);
      case McpCallToolRequest.METHOD -> typedMessage(object, McpCallToolRequest.class);
      case McpListResourcesRequest.METHOD -> typedMessage(object, McpListResourcesRequest.class);
      case McpListResourceTemplatesRequest.METHOD ->
          typedMessage(object, McpListResourceTemplatesRequest.class);
      case McpReadResourceRequest.METHOD -> typedMessage(object, McpReadResourceRequest.class);
      case McpSubscriptionsListenRequest.METHOD ->
          typedMessage(object, McpSubscriptionsListenRequest.class);
      case McpListPromptsRequest.METHOD -> typedMessage(object, McpListPromptsRequest.class);
      case McpGetPromptRequest.METHOD -> typedMessage(object, McpGetPromptRequest.class);
      case McpCompleteRequest.METHOD -> typedMessage(object, McpCompleteRequest.class);
      default ->
          new JsonRpcRequest(
              ProtocolJson.value(id), method.stringValue(), ProtocolJson.object(params));
    };
  }

  /** Decodes a message with its exact MCP type. */
  private JsonRpcMessage typedMessage(ObjectNode object, Class<? extends JsonRpcMessage> type) {
    try {
      ObjectNode values = object.deepCopy();
      values.remove(List.of("jsonrpc", "method"));
      return mapper.treeToValue(values, type);
    } catch (JacksonException | IllegalArgumentException exception) {
      if (McpClientRequest.class.isAssignableFrom(type)) {
        return error(
            object.get("id"), McpInvalidParamsException.CODE, "The request parameters are invalid");
      }
      return error(
          NullNode.getInstance(), McpInvalidRequestException.CODE, "The request is invalid");
    }
  }

  /** Converts one typed request to the internal extension-request shape. */
  private JsonRpcRequest genericRequest(McpClientRequest request) {
    return new JsonRpcRequest(
        request.id(), request.method(), ProtocolJson.object(mapper.valueToTree(request.params())));
  }

  /** Decodes one response. */
  private JsonRpcMessage decodeResponse(ObjectNode object) {
    JsonNode id = object.get("id");
    if (id == null) {
      return error(
          NullNode.getInstance(), McpInvalidRequestException.CODE, "A response needs an ID");
    }
    boolean hasResult = object.has("result");
    boolean hasError = object.has("error");
    if (hasResult == hasError) {
      return error(
          NullNode.getInstance(),
          McpInvalidRequestException.CODE,
          "A response needs one result or error");
    }
    if (hasResult) {
      return new JsonRpcResultResponse(
          ProtocolJson.value(id), ProtocolJson.value(object.get("result")));
    }
    if (!(object.get("error") instanceof ObjectNode error)
        || !error.path("code").isIntegralNumber()
        || !error.path("code").canConvertToInt()
        || !error.path("message").isString()) {
      return error(
          NullNode.getInstance(), McpInvalidRequestException.CODE, "The error value is invalid");
    }
    return new JsonRpcErrorResponse(
        ProtocolJson.value(id),
        error.path("code").intValue(),
        error.path("message").stringValue(),
        Optional.ofNullable(error.get("data")).map(ProtocolJson::value));
  }

  /** Reports a second subscriber without processing the request again. */
  private static void rejectSecondSubscriber(Flow.Subscriber<? super JsonRpcMessage> subscriber) {
    subscriber.onSubscribe(
        new Flow.Subscription() {
          /** {@inheritDoc} */
          @Override
          public void request(long count) {}

          /** {@inheritDoc} */
          @Override
          public void cancel() {}
        });
    subscriber.onError(new IllegalStateException("An MCP publication accepts one subscriber"));
  }

  /** Rejects custom handlers that replace a built-in method. */
  private void rejectBuiltInConflicts() {
    Set<String> builtIns =
        Set.of(
            McpDiscoverRequest.METHOD,
            McpListToolsRequest.METHOD,
            McpCallToolRequest.METHOD,
            McpListResourcesRequest.METHOD,
            McpListResourceTemplatesRequest.METHOD,
            McpReadResourceRequest.METHOD,
            McpSubscriptionsListenRequest.METHOD,
            McpListPromptsRequest.METHOD,
            McpGetPromptRequest.METHOD,
            McpCompleteRequest.METHOD);
    methods.keySet().stream()
        .filter(builtIns::contains)
        .findFirst()
        .ifPresent(
            method -> {
              throw new IllegalArgumentException("A custom method conflicts with: " + method);
            });
  }

  /** Copies one map while it preserves insertion order. */
  private static <K, V> Map<K, V> immutableMap(Map<K, V> values) {
    return Collections.unmodifiableMap(new LinkedHashMap<>(values));
  }

  /** Stores a tool with its handler. */
  private record ToolRegistration<C>(
      Map<String, ?> declaration,
      RegisteredHandler<C, McpCallToolRequestParams, McpCallToolResultResponse.Result> handler) {
    /** Returns the validated object schema from the immutable declaration. */
    @SuppressWarnings("unchecked")
    private Map<String, ?> inputSchema() {
      return (Map<String, ?>) declaration.get("inputSchema");
    }

    /** Returns the optional structured-output schema. */
    @SuppressWarnings("unchecked")
    private Optional<Map<String, ?>> outputSchema() {
      return Optional.ofNullable((Map<String, ?>) declaration.get("outputSchema"));
    }
  }

  /** Stores a resource with its handler. */
  private record ResourceRegistration<C>(
      McpResource declaration,
      RegisteredHandler<C, McpReadResourceRequestParams, McpReadResourceResultResponse.Result>
          handler) {}

  /** Stores a resource template with its handler. */
  private record ResourceTemplateRegistration<C>(
      McpResourceTemplate declaration,
      RegisteredHandler<C, McpReadResourceRequestParams, McpReadResourceResultResponse.Result>
          handler) {}

  /** Stores a prompt with its handler. */
  private record PromptRegistration<C>(
      McpPrompt declaration,
      RegisteredHandler<C, McpGetPromptRequestParams, McpGetPromptResultResponse.Result> handler) {}

  /** Normalizes synchronous and asynchronous registrations to one future-based lifecycle. */
  @FunctionalInterface
  private interface RegisteredHandler<C, P, R> {
    /** Invokes one registered application handler. */
    CompletableFuture<? extends R> handle(
        C applicationContext, P parameters, McpHandlerContext handlerContext);
  }

  /** Stores one active subscription filter and its publication. */
  private record ActiveSubscription(
      Object id,
      boolean toolsChanged,
      boolean resourcesChanged,
      boolean promptsChanged,
      Set<String> resourceUris,
      Output output) {

    /** Validates and copies the subscription fields. */
    private ActiveSubscription {
      Objects.requireNonNull(id, "id");
      resourceUris = Set.copyOf(resourceUris);
      Objects.requireNonNull(output, "output");
    }

    /** Tests whether this subscription accepts one notification. */
    private boolean accepts(McpSubscriptionNotification notification) {
      if (notification instanceof McpToolListChangedNotification) {
        return toolsChanged;
      }
      if (notification instanceof McpResourceListChangedNotification) {
        return resourcesChanged;
      }
      if (notification instanceof McpPromptListChangedNotification) {
        return promptsChanged;
      }
      McpResourceUpdatedNotification updated = (McpResourceUpdatedNotification) notification;
      return resourceUris.contains(updated.params().uri().toString());
    }

    /** Emits one notification with the subscription identifier. */
    private void emit(McpSubscriptionNotification notification, ObjectMapper mapper) {
      ObjectNode encoded = mapper.valueToTree(notification);
      ObjectNode params =
          encoded.get("params") instanceof ObjectNode supplied
              ? supplied
              : mapper.createObjectNode();
      params
          .putObject("_meta")
          .set("io.modelcontextprotocol/subscriptionId", mapper.valueToTree(id));
      Object key =
          notification instanceof McpResourceUpdatedNotification updated
              ? updated.params().uri().toString()
              : notification.getClass();
      output.emitInvalidation(
          key, new JsonRpcNotification(notification.method(), ProtocolJson.object(params)));
    }

    /** Ends this stream with its protocol terminal result and no pending invalidations. */
    private void closeGracefully(McpImplementation serverInfo) {
      output.completeSubscription(
          new McpSubscriptionsListenResultResponse(
              id,
              new McpSubscriptionsListenResult(
                  new McpSubscriptionsListenResultMetaObject(Optional.of(serverInfo), id),
                  "complete")));
    }
  }

  /** Owns one request-scoped JDK publication. */
  private static final class Output implements McpHandlerContext {
    private final Flow.Subscriber<? super JsonRpcMessage> subscriber;
    private final Consumer<Output> starter;
    private final CompletableFuture<Void> cancellation = new CompletableFuture<>();
    private final CompletionStage<Void> cancellationView = cancellation.minimalCompletionStage();
    private final List<Runnable> closeActions = new ArrayList<>();
    private final ArrayDeque<JsonRpcMessage> pendingMessages = new ArrayDeque<>();
    private final LinkedHashMap<Object, JsonRpcMessage> pendingInvalidations =
        new LinkedHashMap<>();
    private CompletableFuture<?> applicationFuture;
    private Optional<Object> progressToken = Optional.empty();
    private JsonRpcMessage pendingProgress;
    private JsonRpcMessage terminalMessage;
    private Throwable terminalFailure;
    private long demand;
    private double lastProgress = Double.NEGATIVE_INFINITY;
    private boolean started;
    private boolean finished;
    private boolean cancelled;
    private boolean completeWithoutMessage;
    private boolean terminalSignalSent;
    private boolean draining;

    /** Subscribes the application subscriber before request processing starts. */
    private Output(Flow.Subscriber<? super JsonRpcMessage> subscriber, Consumer<Output> starter) {
      this.subscriber = Objects.requireNonNull(subscriber, "subscriber");
      this.starter = Objects.requireNonNull(starter, "starter");
      subscriber.onSubscribe(
          new Flow.Subscription() {
            /** {@inheritDoc} */
            @Override
            public void request(long count) {
              Output.this.request(count);
            }

            /** {@inheritDoc} */
            @Override
            public void cancel() {
              Output.this.cancel(false);
            }
          });
    }

    /** Adds demand and starts processing on the first valid request. */
    private void request(long count) {
      if (count <= 0) {
        fail(new IllegalArgumentException("Demand must be positive"));
        return;
      }
      boolean invoke;
      synchronized (this) {
        if (cancelled || terminalSignalSent) {
          return;
        }
        demand = addDemand(demand, count);
        invoke = !started && !finished;
        started = true;
      }
      if (invoke) {
        try {
          starter.accept(this);
        } catch (Throwable failure) {
          fail(failure);
        }
      }
      drain();
    }

    /** Emits one item unless this publication is closed. */
    private void emit(JsonRpcMessage message) {
      enqueue(message);
      drain();
    }

    /** Queues an item before making a subscription visible to concurrent emitters. */
    private void enqueue(JsonRpcMessage message) {
      Objects.requireNonNull(message, "message");
      synchronized (this) {
        if (finished) {
          return;
        }
        pendingMessages.addLast(message);
      }
    }

    /** Retains the latest subscription invalidation for one logical key. */
    private void emitInvalidation(Object key, JsonRpcMessage message) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(message, "message");
      synchronized (this) {
        if (finished) {
          return;
        }
        pendingInvalidations.remove(key);
        pendingInvalidations.put(key, message);
      }
      drain();
    }

    /** Publishes one terminal response if completion wins the request. */
    private void complete(JsonRpcMessage message) {
      Objects.requireNonNull(message, "message");
      List<Runnable> actions;
      synchronized (this) {
        if (finished) {
          return;
        }
        finished = true;
        terminalMessage = message;
        actions = takeCloseActions();
      }
      run(actions);
      drain();
    }

    /** Retains a subscription's terminal response while discarding pending invalidations. */
    private void completeSubscription(JsonRpcMessage message) {
      Objects.requireNonNull(message, "message");
      List<Runnable> actions;
      synchronized (this) {
        if (finished) {
          return;
        }
        finished = true;
        pendingInvalidations.clear();
        terminalMessage = message;
        actions = takeCloseActions();
      }
      run(actions);
      drain();
    }

    /** Adds one idempotent close action. */
    private void onClose(Runnable action) {
      Objects.requireNonNull(action, "action");
      synchronized (this) {
        if (!finished) {
          closeActions.add(action);
          return;
        }
      }
      action.run();
    }

    /** Tracks the application future and propagates an earlier cancellation. */
    private void track(CompletableFuture<?> future) {
      boolean cancelFuture;
      synchronized (this) {
        applicationFuture = Objects.requireNonNull(future, "future");
        cancelFuture = cancelled;
      }
      if (cancelFuture) {
        cancellation.join();
        future.cancel(false);
      }
    }

    /** Stores the optional opaque progress token for this request. */
    private synchronized void progressToken(Object value) {
      progressToken = Optional.ofNullable(value);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean isCancelled() {
      return cancelled;
    }

    /** {@inheritDoc} */
    @Override
    public CompletionStage<Void> cancellation() {
      return cancellationView;
    }

    /** {@inheritDoc} */
    @Override
    public void progress(double value) {
      acceptProgress(value, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void progress(double value, String message) {
      acceptProgress(value, null, requireProgressMessage(message));
    }

    /** {@inheritDoc} */
    @Override
    public void progress(double value, double total) {
      acceptProgress(value, total, null);
    }

    /** {@inheritDoc} */
    @Override
    public void progress(double value, double total, String message) {
      acceptProgress(value, total, requireProgressMessage(message));
    }

    /** Validates and publishes one progress update. */
    private void acceptProgress(Double value, Double total, String message) {
      if (!Double.isFinite(value) || (total != null && !Double.isFinite(total))) {
        throw new IllegalArgumentException("Progress values must be finite");
      }
      synchronized (this) {
        if (finished) {
          throw new IllegalStateException("The request is complete");
        }
        if (value <= lastProgress) {
          throw new IllegalArgumentException("Progress must increase");
        }
        lastProgress = value;
        progressToken.ifPresent(
            token ->
                pendingProgress =
                    new McpProgressNotification(
                        new McpProgressNotificationParams(
                            Optional.empty(),
                            Optional.ofNullable(message),
                            value,
                            token,
                            Optional.ofNullable(total))));
      }
      drain();
    }

    /** Requires a non-null progress message. */
    private static String requireProgressMessage(String message) {
      if (message == null) {
        throw new IllegalArgumentException("Progress message must not be null");
      }
      return message;
    }

    /** Cancels this publication and optionally notifies a still-attached subscriber. */
    private void cancel(boolean notifySubscriber) {
      CompletableFuture<?> future;
      List<Runnable> actions;
      synchronized (this) {
        if (finished || terminalSignalSent) {
          return;
        }
        finished = true;
        cancelled = true;
        pendingMessages.clear();
        pendingProgress = null;
        pendingInvalidations.clear();
        terminalMessage = null;
        terminalFailure = null;
        completeWithoutMessage = notifySubscriber;
        cancellation.complete(null);
        future = applicationFuture;
        actions = takeCloseActions();
      }
      if (future != null) {
        future.cancel(false);
      }
      run(actions);
      if (notifySubscriber) {
        drain();
      }
    }

    /** Cancels this publication and completes an externally cancelled stream. */
    private void cancel() {
      cancel(true);
    }

    /** Completes this publication without a final item. */
    private void close() {
      List<Runnable> actions;
      synchronized (this) {
        if (finished) {
          return;
        }
        finished = true;
        pendingInvalidations.clear();
        completeWithoutMessage = true;
        actions = takeCloseActions();
      }
      run(actions);
      drain();
    }

    /** Fails this publication if failure wins the request. */
    private void fail(Throwable failure) {
      Objects.requireNonNull(failure, "failure");
      List<Runnable> actions;
      synchronized (this) {
        if (finished) {
          return;
        }
        finished = true;
        pendingMessages.clear();
        pendingProgress = null;
        pendingInvalidations.clear();
        terminalFailure = failure;
        actions = takeCloseActions();
      }
      run(actions);
      drain();
    }

    /** Serializes downstream signals while honoring demand. */
    private void drain() {
      synchronized (this) {
        if (draining) {
          return;
        }
        draining = true;
      }
      while (true) {
        JsonRpcMessage next = null;
        Throwable failure = null;
        boolean completeNow = false;
        boolean completeAfterNext = false;
        synchronized (this) {
          if (!terminalSignalSent && terminalFailure != null) {
            failure = terminalFailure;
            terminalFailure = null;
            terminalSignalSent = true;
          } else if (!terminalSignalSent && demand > 0) {
            if (!pendingMessages.isEmpty()) {
              next = pendingMessages.removeFirst();
            } else if (pendingProgress != null) {
              next = pendingProgress;
              pendingProgress = null;
            } else if (!pendingInvalidations.isEmpty()) {
              var iterator = pendingInvalidations.entrySet().iterator();
              next = iterator.next().getValue();
              iterator.remove();
            } else if (terminalMessage != null) {
              next = terminalMessage;
              terminalMessage = null;
              terminalSignalSent = true;
              completeAfterNext = true;
            }
            if (next != null && demand != Long.MAX_VALUE) {
              demand--;
            }
          }
          if (!terminalSignalSent
              && completeWithoutMessage
              && pendingMessages.isEmpty()
              && pendingProgress == null
              && pendingInvalidations.isEmpty()) {
            completeNow = true;
            terminalSignalSent = true;
          }
          if (next == null && failure == null && !completeNow) {
            draining = false;
            return;
          }
        }
        if (failure != null) {
          subscriber.onError(failure);
        } else if (next != null) {
          try {
            subscriber.onNext(next);
            if (completeAfterNext) {
              subscriber.onComplete();
            }
          } catch (Throwable subscriberFailure) {
            cancel(false);
          }
        } else if (completeNow) {
          subscriber.onComplete();
        }
      }
    }

    /** Takes the close actions while holding this output's monitor. */
    private List<Runnable> takeCloseActions() {
      List<Runnable> actions = List.copyOf(closeActions);
      closeActions.clear();
      return actions;
    }

    /** Runs request cleanup actions. */
    private static void run(List<Runnable> actions) {
      actions.forEach(Runnable::run);
    }

    /** Adds demand with saturation. */
    private static long addDemand(long current, long additional) {
      long sum = current + additional;
      return sum < 0 ? Long.MAX_VALUE : sum;
    }
  }

  /**
   * Builds one immutable server kit.
   *
   * <p>A synchronous registration invokes its handler inline on the thread that makes the first
   * positive demand. An asynchronous registration invokes its handler on that same thread, but the
   * application owns the returned {@link CompletableFuture} and its execution policy. The server
   * kit calls {@code cancel(false)} on that future when downstream cancellation wins. Each handler,
   * future, and eventual result must be non-null.
   *
   * <p>A duplicate tool name, resource URI, resource-template pattern, prompt name, or custom
   * method name causes {@link IllegalArgumentException}. The builder never silently replaces these
   * registrations.
   *
   * @param <C> the application-context type
   */
  public static final class Builder<C> {
    private final String name;
    private final String version;
    private Optional<String> title = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<URI> websiteUrl = Optional.empty();
    private Optional<String> instructions = Optional.empty();
    private final List<McpIcon> icons = new ArrayList<>();
    private final ObjectMapper mapper = configureMapper();
    private final LinkedHashMap<String, ToolRegistration<C>> tools = new LinkedHashMap<>();
    private final LinkedHashMap<String, ResourceRegistration<C>> resources = new LinkedHashMap<>();
    private final LinkedHashMap<Pattern, ResourceTemplateRegistration<C>> resourceTemplates =
        new LinkedHashMap<>();
    private final LinkedHashMap<String, PromptRegistration<C>> prompts = new LinkedHashMap<>();
    private RegisteredHandler<C, McpCompleteRequestParams, McpCompleteResult> completion;
    private final LinkedHashMap<String, RegisteredHandler<C, JsonRpcRequest, JsonRpcResponse>>
        methods = new LinkedHashMap<>();

    /** Creates a builder with required server identity. */
    private Builder(String name, String version) {
      this.name = requireText(name, "server name");
      this.version = requireText(version, "server version");
    }

    /**
     * Sets the display title.
     *
     * @param value the display title
     * @return this builder
     */
    public Builder<C> title(String value) {
      title = Optional.of(requireText(value, "server title"));
      return this;
    }

    /**
     * Sets the server description.
     *
     * @param value the description
     * @return this builder
     */
    public Builder<C> description(String value) {
      description = Optional.of(requireText(value, "server description"));
      return this;
    }

    /**
     * Sets the server website.
     *
     * @param value the website URI
     * @return this builder
     */
    public Builder<C> websiteUrl(URI value) {
      websiteUrl = Optional.of(Objects.requireNonNull(value, "value"));
      return this;
    }

    /**
     * Sets instructions for MCP clients.
     *
     * @param value the instructions
     * @return this builder
     */
    public Builder<C> instructions(String value) {
      instructions = Optional.of(requireText(value, "instructions"));
      return this;
    }

    /**
     * Adds one server icon.
     *
     * @param value the icon declaration
     * @return this builder
     */
    public Builder<C> icon(McpIcon value) {
      icons.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    /**
     * Registers one synchronous tool handler.
     *
     * <p>The callback runs immediately and once. Its values are validated and snapshotted after it
     * returns. A failed callback or invalid declaration registers nothing.
     *
     * @param configure the tool configuration, including a nonblank name and non-null handler
     * @return this builder
     * @throws IllegalArgumentException if the tool name is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> syncTool(
        Consumer<ToolBuilder<C, McpCallToolResultResponse.Result>> configure) {
      var tool = new ToolBuilder<C, McpCallToolResultResponse.Result>();
      Objects.requireNonNull(configure, "configure").accept(tool);
      var declaration = tool.declaration(mapper);
      put(tools, tool.name, new ToolRegistration<>(declaration, adaptSync(tool.handler)), "tool");
      return this;
    }

    /**
     * Registers one asynchronous tool handler.
     *
     * <p>The callback runs immediately and once. Its values are validated and snapshotted after it
     * returns. A failed callback or invalid declaration registers nothing.
     *
     * @param configure the tool configuration, including a nonblank name and non-null handler
     * @return this builder
     * @throws IllegalArgumentException if the tool name is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> asyncTool(
        Consumer<ToolBuilder<C, CompletableFuture<? extends McpCallToolResultResponse.Result>>>
            configure) {
      var tool =
          new ToolBuilder<C, CompletableFuture<? extends McpCallToolResultResponse.Result>>();
      Objects.requireNonNull(configure, "configure").accept(tool);
      var declaration = tool.declaration(mapper);
      put(tools, tool.name, new ToolRegistration<>(declaration, adaptAsync(tool.handler)), "tool");
      return this;
    }

    /**
     * Registers one synchronous concrete-resource handler.
     *
     * @param declaration the resource declaration
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the resource URI is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> syncResource(
        McpResource declaration,
        McpHandler<C, McpReadResourceRequestParams, McpReadResourceResultResponse.Result> handler) {
      return registerResource(declaration, adaptSync(handler));
    }

    /**
     * Registers one asynchronous concrete-resource handler.
     *
     * @param declaration the resource declaration
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the resource URI is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> asyncResource(
        McpResource declaration,
        McpHandler<
                C,
                McpReadResourceRequestParams,
                CompletableFuture<? extends McpReadResourceResultResponse.Result>>
            handler) {
      return registerResource(declaration, adaptAsync(handler));
    }

    /**
     * Registers one synchronous resource-template handler.
     *
     * @param declaration the resource-template declaration
     * @param uriPattern the concrete URI pattern
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the pattern is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> syncResourceTemplate(
        McpResourceTemplate declaration,
        Pattern uriPattern,
        McpHandler<C, McpReadResourceRequestParams, McpReadResourceResultResponse.Result> handler) {
      return registerResourceTemplate(declaration, uriPattern, adaptSync(handler));
    }

    /**
     * Registers one asynchronous resource-template handler.
     *
     * @param declaration the resource-template declaration
     * @param uriPattern the concrete URI pattern
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the pattern is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> asyncResourceTemplate(
        McpResourceTemplate declaration,
        Pattern uriPattern,
        McpHandler<
                C,
                McpReadResourceRequestParams,
                CompletableFuture<? extends McpReadResourceResultResponse.Result>>
            handler) {
      return registerResourceTemplate(declaration, uriPattern, adaptAsync(handler));
    }

    /**
     * Registers one synchronous prompt handler.
     *
     * @param declaration the prompt declaration
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the prompt name is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> syncPrompt(
        McpPrompt declaration,
        McpHandler<C, McpGetPromptRequestParams, McpGetPromptResultResponse.Result> handler) {
      McpPrompt required = Objects.requireNonNull(declaration, "declaration");
      put(
          prompts,
          required.name(),
          new PromptRegistration<>(required, adaptSync(handler)),
          "prompt");
      return this;
    }

    /**
     * Registers one asynchronous prompt handler.
     *
     * @param declaration the prompt declaration
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the prompt name is already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> asyncPrompt(
        McpPrompt declaration,
        McpHandler<
                C,
                McpGetPromptRequestParams,
                CompletableFuture<? extends McpGetPromptResultResponse.Result>>
            handler) {
      McpPrompt required = Objects.requireNonNull(declaration, "declaration");
      put(
          prompts,
          required.name(),
          new PromptRegistration<>(required, adaptAsync(handler)),
          "prompt");
      return this;
    }

    /**
     * Registers synchronous argument completion.
     *
     * @param handler the application handler
     * @return this builder
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    public Builder<C> syncCompletion(
        McpHandler<C, McpCompleteRequestParams, McpCompleteResult> handler) {
      completion = adaptSync(handler);
      return this;
    }

    /**
     * Registers asynchronous argument completion.
     *
     * @param handler the application handler
     * @return this builder
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    public Builder<C> asyncCompletion(
        McpHandler<C, McpCompleteRequestParams, CompletableFuture<? extends McpCompleteResult>>
            handler) {
      completion = adaptAsync(handler);
      return this;
    }

    /**
     * Registers one synchronous custom MCP method.
     *
     * @param method the exact method name
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the method is blank or already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> syncMethod(
        String method, McpHandler<C, JsonRpcRequest, JsonRpcResponse> handler) {
      String required = requireText(method, "method");
      put(methods, required, adaptSync(handler), "method");
      return this;
    }

    /**
     * Registers one asynchronous custom MCP method.
     *
     * @param method the exact method name
     * @param handler the application handler
     * @return this builder
     * @throws IllegalArgumentException if the method is blank or already registered
     * @throws NullPointerException if an argument is {@code null}
     */
    public Builder<C> asyncMethod(
        String method,
        McpHandler<C, JsonRpcRequest, CompletableFuture<? extends JsonRpcResponse>> handler) {
      String required = requireText(method, "method");
      put(methods, required, adaptAsync(handler), "method");
      return this;
    }

    /** Stores one concrete resource registration. */
    private Builder<C> registerResource(
        McpResource declaration,
        RegisteredHandler<C, McpReadResourceRequestParams, McpReadResourceResultResponse.Result>
            handler) {
      McpResource required = Objects.requireNonNull(declaration, "declaration");
      put(
          resources,
          required.uri().toString(),
          new ResourceRegistration<>(required, handler),
          "resource");
      return this;
    }

    /** Stores one resource-template registration. */
    private Builder<C> registerResourceTemplate(
        McpResourceTemplate declaration,
        Pattern uriPattern,
        RegisteredHandler<C, McpReadResourceRequestParams, McpReadResourceResultResponse.Result>
            handler) {
      McpResourceTemplate required = Objects.requireNonNull(declaration, "declaration");
      Pattern pattern = Objects.requireNonNull(uriPattern, "uriPattern");
      boolean duplicate =
          resourceTemplates.keySet().stream()
              .anyMatch(
                  item ->
                      item.flags() == pattern.flags() && item.pattern().equals(pattern.pattern()));
      if (duplicate) {
        throw new IllegalArgumentException("Duplicate MCP resource-template pattern: " + pattern);
      }
      resourceTemplates.put(pattern, new ResourceTemplateRegistration<>(required, handler));
      return this;
    }

    /**
     * Builds the immutable server kit.
     *
     * @return the server kit
     * @throws IllegalArgumentException if a custom method replaces a built-in MCP method
     */
    public McpServerKit<C> build() {
      return new McpServerKit<>(this);
    }

    /** Configures one builder-owned mapper. */
    private static ObjectMapper configureMapper() {
      return JsonMapper.builder()
          .configureForJackson2()
          .addModule(ProtocolJson.nullValues())
          .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
          .annotationIntrospector(ProtocolJson.INSTANCE)
          .changeDefaultPropertyInclusion(
              previous ->
                  JsonInclude.Value.construct(
                      JsonInclude.Include.NON_ABSENT, JsonInclude.Include.ALWAYS))
          .build();
    }

    /** Adapts one synchronous handler to the internal future lifecycle. */
    private static <C, P, R> RegisteredHandler<C, P, R> adaptSync(McpHandler<C, P, R> value) {
      McpHandler<C, P, R> required = Objects.requireNonNull(value, "handler");
      return (applicationContext, parameters, handlerContext) ->
          CompletableFuture.completedFuture(
              required.handle(applicationContext, parameters, handlerContext));
    }

    /** Adapts one asynchronous handler to the internal future lifecycle. */
    private static <C, P, R> RegisteredHandler<C, P, R> adaptAsync(
        McpHandler<C, P, CompletableFuture<? extends R>> value) {
      McpHandler<C, P, CompletableFuture<? extends R>> required =
          Objects.requireNonNull(value, "handler");
      return required::handle;
    }

    /** Adds one unique map value. */
    private static <K, V> void put(Map<K, V> values, K key, V value, String type) {
      if (values.putIfAbsent(key, value) != null) {
        throw new IllegalArgumentException("Duplicate MCP " + type + ": " + key);
      }
    }

    /** Requires nonblank text. */
    private static String requireText(String value, String field) {
      Objects.requireNonNull(value, field);
      if (value.isBlank()) {
        throw new IllegalArgumentException("The " + field + " must not be blank");
      }
      return value;
    }
  }

  /**
   * Configures one tool inside a registration callback.
   *
   * @param <C> the application-context type
   * @param <R> the handler result type
   */
  public static final class ToolBuilder<C, R> {
    private String name;
    private McpHandler<C, McpCallToolRequestParams, R> handler;
    private Map<String, ?> inputSchema = McpJsonSchema.mcpJsonObjectSchema();
    private Optional<Map<String, ?>> outputSchema = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<String> title = Optional.empty();
    private Optional<McpMetaObject> meta = Optional.empty();
    private Optional<McpToolAnnotations> annotations = Optional.empty();
    private Optional<List<McpIcon>> icons = Optional.empty();

    private ToolBuilder() {}

    /** Snapshots the declaration after the callback has completed. */
    private Map<String, ?> declaration(ObjectMapper mapper) {
      Builder.requireText(name, "tool name");
      Objects.requireNonNull(handler, "handler");
      if (!"object".equals(inputSchema.get("type"))) {
        throw new IllegalArgumentException("A tool input schema must have object type");
      }
      var values = new LinkedHashMap<String, Object>();
      values.put("name", name);
      values.put("inputSchema", inputSchema);
      outputSchema.ifPresent(value -> values.put("outputSchema", value));
      description.ifPresent(value -> values.put("description", value));
      title.ifPresent(value -> values.put("title", value));
      meta.ifPresent(value -> values.put("_meta", value.values()));
      annotations.ifPresent(
          value -> values.put("annotations", ProtocolJson.object(mapper.valueToTree(value))));
      icons.ifPresent(
          value ->
              values.put(
                  "icons",
                  value.stream()
                      .map(icon -> ProtocolJson.object(mapper.valueToTree(icon)))
                      .toList()));
      return JsonValues.copyObject(values);
    }

    /**
     * Sets a complete input schema, preserving arbitrary dialect keywords.
     *
     * @param value the schema with root type {@code object}
     * @return this builder
     */
    public ToolBuilder<C, R> inputSchema(Map<String, ?> value) {
      inputSchema = Objects.requireNonNull(value, "inputSchema");
      return this;
    }

    /**
     * Configures a closed object input schema using composable JDK schema maps.
     *
     * @param configure the schema configuration, invoked immediately and once
     * @return this builder
     */
    public ToolBuilder<C, R> inputSchema(Consumer<McpJsonSchema.ObjectBuilder> configure) {
      return inputSchema(McpJsonSchema.mcpJsonObjectSchema(configure));
    }

    /**
     * Sets the optional structured-output schema.
     *
     * @param value the schema
     * @return this builder
     */
    public ToolBuilder<C, R> outputSchema(Map<String, ?> value) {
      outputSchema = Optional.of(value);
      return this;
    }

    /**
     * Sets the tool description.
     *
     * @param value the description
     * @return this builder
     */
    public ToolBuilder<C, R> description(String value) {
      description = Optional.of(value);
      return this;
    }

    /**
     * Sets the display title.
     *
     * @param value the title
     * @return this builder
     */
    public ToolBuilder<C, R> title(String value) {
      title = Optional.of(value);
      return this;
    }

    /**
     * Sets protocol extension metadata.
     *
     * @param value the metadata
     * @return this builder
     */
    public ToolBuilder<C, R> meta(McpMetaObject value) {
      meta = Optional.of(value);
      return this;
    }

    /**
     * Sets tool behavior and display hints.
     *
     * @param value the annotations
     * @return this builder
     */
    public ToolBuilder<C, R> annotations(McpToolAnnotations value) {
      annotations = Optional.of(value);
      return this;
    }

    /**
     * Sets icons for the tool.
     *
     * @param value the icons
     * @return this builder
     */
    public ToolBuilder<C, R> icons(List<McpIcon> value) {
      icons = Optional.of(value);
      return this;
    }

    /**
     * Sets the programmatic tool name.
     *
     * @param value the name; it must be nonblank when the registration callback returns
     * @return this builder
     */
    public ToolBuilder<C, R> name(String value) {
      name = Objects.requireNonNull(value, "name");
      return this;
    }

    /**
     * Sets the context-first application handler.
     *
     * @param value the handler
     * @return this builder
     */
    public ToolBuilder<C, R> handler(McpHandler<C, McpCallToolRequestParams, R> value) {
      handler = Objects.requireNonNull(value, "handler");
      return this;
    }
  }
}
