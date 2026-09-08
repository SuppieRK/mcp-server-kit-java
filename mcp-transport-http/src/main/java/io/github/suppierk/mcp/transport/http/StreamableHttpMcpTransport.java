package io.github.suppierk.mcp.transport.http;

import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.JsonRpcMessage;
import io.github.suppierk.mcp.protocol.JsonRpcNotification;
import io.github.suppierk.mcp.protocol.JsonRpcRequest;
import io.github.suppierk.mcp.protocol.JsonRpcResponse;
import io.github.suppierk.mcp.protocol.McpCallToolRequestParams;
import io.github.suppierk.mcp.protocol.McpClientNotification;
import io.github.suppierk.mcp.protocol.McpClientRequest;
import io.github.suppierk.mcp.protocol.McpGetPromptRequestParams;
import io.github.suppierk.mcp.protocol.McpJsonNull;
import io.github.suppierk.mcp.protocol.McpProtocol;
import io.github.suppierk.mcp.protocol.McpReadResourceRequestParams;
import io.github.suppierk.mcp.protocol.McpRequestParameters;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenRequest;
import io.github.suppierk.mcp.server.McpHeaderMismatchException;
import io.github.suppierk.mcp.server.McpInternalException;
import io.github.suppierk.mcp.server.McpInvalidRequestException;
import io.github.suppierk.mcp.server.McpMethodNotFoundException;
import io.github.suppierk.mcp.server.McpServerKit;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implements the framework-neutral MCP Streamable HTTP binding.
 *
 * @param <C> the application-context type
 * @see <a
 *     href="https://modelcontextprotocol.io/specification/2026-07-28/basic/transports#streamable-http">MCP
 *     Streamable HTTP transport</a>
 */
public final class StreamableHttpMcpTransport<C> {
  private static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static final String JSON_MEDIA_TYPE = "application/json";

  private static final Pattern JSON_NUMBER =
      Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
  private final McpServerKit<C> serverKit;
  private final Set<Origin> allowedOrigins;

  /**
   * Creates a binding that accepts non-browser requests without an Origin header.
   *
   * @param serverKit the MCP server kit
   */
  public StreamableHttpMcpTransport(McpServerKit<C> serverKit) {
    this(serverKit, Set.of());
  }

  /**
   * Creates a binding with an exact browser-origin allowlist.
   *
   * @param serverKit the MCP server kit
   * @param allowedOrigins the exact allowed HTTP origins
   */
  public StreamableHttpMcpTransport(McpServerKit<C> serverKit, Set<URI> allowedOrigins) {
    this.serverKit = Objects.requireNonNull(serverKit, "serverKit");
    this.allowedOrigins =
        Objects.requireNonNull(allowedOrigins, "allowedOrigins").stream()
            .map(StreamableHttpMcpTransport::configuredOrigin)
            .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Handles one framework-neutral request.
   *
   * @param applicationContext application-owned invocation data
   * @param request the HTTP request
   * @return a stage that supplies the HTTP response
   */
  public CompletionStage<HttpMcpResponse> handle(C applicationContext, HttpMcpRequest request) {
    Objects.requireNonNull(applicationContext, "applicationContext");
    Objects.requireNonNull(request, "request");
    HttpMcpResponse rejected = validateEnvelope(request);
    if (rejected != null) {
      return CompletableFuture.completedFuture(rejected);
    }
    JsonRpcMessage message = serverKit.decode(request.body());
    HttpMcpResponse mismatch = validateMirrors(request, message);
    if (mismatch != null) {
      return CompletableFuture.completedFuture(mismatch);
    }
    if (message instanceof McpClientNotification
        || message instanceof JsonRpcNotification notification
            && McpClientNotification.METHOD_NAME.equals(notification.method())) {
      return CompletableFuture.completedFuture(
          httpError(400, "Cancellation notifications are only supported over stdio"));
    }
    Flow.Publisher<JsonRpcMessage> publication = serverKit.handle(applicationContext, message);
    if (requiresEventStream(message)) {
      return CompletableFuture.completedFuture(stream(publication));
    }
    return collect(publication);
  }

  /** Selects the fixed event-stream response cases from decoded request semantics. */
  private static boolean requiresEventStream(JsonRpcMessage message) {
    if (message instanceof McpClientRequest request) {
      return McpSubscriptionsListenRequest.METHOD_NAME.equals(request.method())
          || request
              .params()
              .meta()
              .progressToken()
              .filter(token -> token != McpJsonNull.INSTANCE)
              .isPresent();
    }
    if (message instanceof JsonRpcRequest request) {
      Object metadata = request.params().get("_meta");
      return McpSubscriptionsListenRequest.METHOD_NAME.equals(request.method())
          || (metadata instanceof Map<?, ?> values
              && values.get("progressToken") != null
              && values.get("progressToken") != McpJsonNull.INSTANCE);
    }
    return false;
  }

  /** Validates HTTP method, media types, required headers, and origin. */
  private HttpMcpResponse validateEnvelope(HttpMcpRequest request) {
    if (!allowsOrigin(request.headerValues("Origin"))) {
      return httpError(403, "Origin is not allowed");
    }
    if (!"POST".equalsIgnoreCase(request.method())) {
      return httpError(405, "Only POST is supported");
    }
    List<String> contentTypes = request.headerValues(CONTENT_TYPE_HEADER);
    if (contentTypes.size() != 1) {
      return httpError(400, "Content-Type must have one value");
    }
    String contentType = contentTypes.get(0);
    String mediaType = contentType.split(";", 2)[0].trim();
    if (!JSON_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
      return httpError(415, "Content-Type must be application/json");
    }
    if (!accepts(request, JSON_MEDIA_TYPE) && !accepts(request, "text/event-stream")) {
      return httpError(406, "Accept must allow JSON or event streams");
    }
    if (!hasOneValue(request, "MCP-Protocol-Version") || !hasOneValue(request, "Mcp-Method")) {
      return httpError(400, "Required MCP headers are absent");
    }
    return null;
  }

  /** Tests an absent Origin or one exact normalized configured Origin. */
  private boolean allowsOrigin(List<String> values) {
    if (values.isEmpty()) {
      return true;
    }
    if (values.size() != 1 || allowedOrigins.isEmpty()) {
      return false;
    }
    try {
      return allowedOrigins.contains(origin(URI.create(values.get(0))));
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  /** Validates and normalizes one configured HTTP origin. */
  private static Origin configuredOrigin(URI uri) {
    try {
      return origin(Objects.requireNonNull(uri, "allowed origin"));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("An allowed origin must be an HTTP origin", exception);
    }
  }

  /** Normalizes scheme, host, and effective port for one exact HTTP origin. */
  private static Origin origin(URI uri) {
    String scheme = uri.getScheme();
    String host = uri.getHost();
    if (uri.isOpaque()
        || scheme == null
        || host == null
        || uri.getUserInfo() != null
        || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
        || uri.getRawQuery() != null
        || uri.getRawFragment() != null) {
      throw new IllegalArgumentException("Not an origin");
    }
    String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
    int port = uri.getPort();
    if (port > 65535) {
      throw new IllegalArgumentException("Invalid origin port");
    }
    if ("http".equals(normalizedScheme)) {
      port = port == -1 ? 80 : port;
    } else if ("https".equals(normalizedScheme)) {
      port = port == -1 ? 443 : port;
    } else {
      throw new IllegalArgumentException("Not an HTTP origin");
    }
    return new Origin(normalizedScheme, host.toLowerCase(Locale.ROOT), port);
  }

  /** Validates header values that mirror the decoded body. */
  private HttpMcpResponse validateMirrors(HttpMcpRequest request, JsonRpcMessage message) {
    if (!(message instanceof JsonRpcRequest)
        && !(message instanceof McpClientRequest)
        && !(message instanceof McpClientNotification)
        && !(message instanceof JsonRpcNotification)) {
      return null;
    }
    String method = messageMethod(message);
    Map<String, ?> params = message instanceof JsonRpcRequest call ? call.params() : null;
    if (!request.header("Mcp-Method").equals(method)) {
      return protocolError(
          400, McpHeaderMismatchException.ERROR_CODE, "Header and body method do not agree");
    }
    boolean isRequest = message instanceof JsonRpcRequest || message instanceof McpClientRequest;
    String bodyVersion = null;
    if (message instanceof McpClientRequest call) {
      bodyVersion = call.params().meta().protocolVersion();
    } else if (message instanceof JsonRpcRequest) {
      bodyVersion = bodyVersion(params);
    }
    if (isRequest && !Objects.equals(request.header("MCP-Protocol-Version"), bodyVersion)) {
      return protocolError(
          400, McpHeaderMismatchException.ERROR_CODE, "Header and body version do not agree");
    }
    String bodyName = message instanceof McpClientRequest call ? requestName(call.params()) : null;
    List<String> headerNames = request.headerValues("Mcp-Name");
    if ((bodyName == null && !headerNames.isEmpty())
        || (bodyName != null
            && (headerNames.size() != 1
                || !Objects.equals(decodeHeaderValue(headerNames.get(0)), bodyName)))) {
      return protocolError(
          400, McpHeaderMismatchException.ERROR_CODE, "Header and body name do not agree");
    }
    return validateToolHeaders(request, message);
  }

  /** Validates every custom header declared by the selected tool's input schema. */
  private HttpMcpResponse validateToolHeaders(HttpMcpRequest request, JsonRpcMessage message) {
    if (!(message instanceof McpClientRequest call)
        || !(call.params() instanceof McpCallToolRequestParams tool)) {
      return null;
    }
    Optional<Map<String, ?>> inputSchema = serverKit.toolInputSchema(tool.name());
    if (inputSchema.isEmpty()) {
      return null;
    }
    ArrayList<HeaderParameter> declarations = new ArrayList<>();
    collectHeaderParameters(inputSchema.get(), List.of(), declarations);
    Object arguments = tool.arguments().orElse(null);
    for (HeaderParameter declaration : declarations) {
      List<String> values = request.headerValues("Mcp-Param-" + declaration.headerName());
      Object bodyValue = valueAt(arguments, declaration.path());
      if (bodyValue == null || bodyValue == McpJsonNull.INSTANCE) {
        if (!values.isEmpty()) {
          return headerMismatch("Header exists without a body parameter");
        }
      } else if (values.size() != 1
          || !matchesHeaderValue(decodeHeaderValue(values.get(0)), bodyValue)) {
        return headerMismatch("Header and body parameter do not agree");
      }
    }
    return null;
  }

  /** Compares numbers by exact value, independently of decimal scale or exponent formatting. */
  private static boolean matchesHeaderValue(String headerValue, Object bodyValue) {
    if (bodyValue instanceof Number) {
      if (headerValue == null || !JSON_NUMBER.matcher(headerValue).matches()) {
        return false;
      }
      try {
        return new BigDecimal(headerValue).compareTo(new BigDecimal(bodyValue.toString())) == 0;
      } catch (NumberFormatException exception) {
        return false;
      }
    }
    return Objects.equals(
        headerValue,
        bodyValue instanceof String || bodyValue instanceof Boolean ? bodyValue.toString() : "");
  }

  /** Collects header declarations reachable through JSON Schema properties. */
  private static void collectHeaderParameters(
      Object schema, List<String> path, List<HeaderParameter> declarations) {
    if (!(schema instanceof Map<?, ?> object)
        || !(object.get("properties") instanceof Map<?, ?> properties)) {
      return;
    }
    properties
        .entrySet()
        .forEach(
            property -> {
              ArrayList<String> propertyPath = new ArrayList<>(path);
              propertyPath.add((String) property.getKey());
              Object headerName =
                  property.getValue() instanceof Map<?, ?> fields
                      ? fields.get("x-mcp-header")
                      : null;
              if (headerName instanceof String name) {
                declarations.add(new HeaderParameter(name, List.copyOf(propertyPath)));
              }
              collectHeaderParameters(property.getValue(), propertyPath, declarations);
            });
  }

  /** Gets one argument at its exact declared property path. */
  private static Object valueAt(Object arguments, List<String> path) {
    Object value = arguments;
    for (String property : path) {
      if (!(value instanceof Map<?, ?> object)) {
        return null;
      }
      value = object.get(property);
    }
    return value;
  }

  /** Decodes the exact Base64 sentinel or validates one plain ASCII header value. */
  private static String decodeHeaderValue(String supplied) {
    String value = trimOptionalWhitespace(supplied);
    if (value.startsWith("=?base64?") && value.endsWith("?=")) {
      String encoded = value.substring(9, value.length() - 2);
      try {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        if (!Base64.getEncoder().encodeToString(decoded).equals(encoded)) {
          return null;
        }
        return StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(decoded))
            .toString();
      } catch (IllegalArgumentException | CharacterCodingException exception) {
        return null;
      }
    }
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      if (character != '\t' && (character < 0x20 || character > 0x7e)) {
        return null;
      }
    }
    return value;
  }

  /** Removes HTTP optional whitespace from both ends of one field value. */
  private static String trimOptionalWhitespace(String value) {
    int start = 0;
    int end = value.length();
    while (start < end && (value.charAt(start) == ' ' || value.charAt(start) == '\t')) {
      start++;
    }
    while (end > start && (value.charAt(end - 1) == ' ' || value.charAt(end - 1) == '\t')) {
      end--;
    }
    return value.substring(start, end);
  }

  /** Creates a custom-header mismatch response. */
  private HttpMcpResponse headerMismatch(String message) {
    return protocolError(400, McpHeaderMismatchException.ERROR_CODE, message);
  }

  /** Gets the protocol version from generic parameters. */
  private static String bodyVersion(Map<String, ?> params) {
    Object metadata = params.get("_meta");
    Object version =
        metadata instanceof Map<?, ?> object ? object.get(McpProtocol.PROTOCOL_VERSION_KEY) : null;
    return version instanceof String text ? text : null;
  }

  /** Gets a protocol declaration name from typed parameters. */
  private static String requestName(McpRequestParameters params) {
    if (params instanceof McpCallToolRequestParams tool) {
      return tool.name();
    }
    if (params instanceof McpGetPromptRequestParams prompt) {
      return prompt.name();
    }
    return params instanceof McpReadResourceRequestParams resource
        ? resource.uri().toString()
        : null;
  }

  /** Gets the method after the message has been checked as a request or notification. */
  private static String messageMethod(JsonRpcMessage message) {
    if (message instanceof JsonRpcRequest call) {
      return call.method();
    }
    if (message instanceof McpClientRequest call) {
      return call.method();
    }
    if (message instanceof McpClientNotification notification) {
      return notification.method();
    }
    return ((JsonRpcNotification) message).method();
  }

  /** Maps protocol error codes to HTTP status codes. */
  private static int status(JsonRpcResponse response) {
    if (!(response instanceof JsonRpcErrorResponse error)) {
      return 200;
    }
    if (error.code() == McpMethodNotFoundException.ERROR_CODE) {
      return 404;
    }
    return error.code() == McpInternalException.ERROR_CODE ? 200 : 400;
  }

  /** Creates one encoded event stream and its close action. */
  private HttpEventStreamResponse stream(Flow.Publisher<JsonRpcMessage> source) {
    Flow.Publisher<ByteBuffer> events =
        subscriber ->
            source.subscribe(
                new Flow.Subscriber<>() {
                  /** {@inheritDoc} */
                  @Override
                  public void onSubscribe(Flow.Subscription subscription) {
                    subscriber.onSubscribe(subscription);
                  }

                  /** {@inheritDoc} */
                  @Override
                  public void onNext(JsonRpcMessage item) {
                    subscriber.onNext(encodeSse(item));
                  }

                  /** {@inheritDoc} */
                  @Override
                  public void onError(Throwable failure) {
                    subscriber.onError(failure);
                  }

                  /** {@inheritDoc} */
                  @Override
                  public void onComplete() {
                    subscriber.onComplete();
                  }
                });
    return new HttpEventStreamResponse(events);
  }

  /** Encodes one JSON-RPC message as one SSE event. */
  private ByteBuffer encodeSse(JsonRpcMessage message) {
    byte[] json = serverKit.encode(message);
    byte[] prefix = "event: message\ndata: ".getBytes(StandardCharsets.UTF_8);
    byte[] suffix = "\n\n".getBytes(StandardCharsets.UTF_8);
    ByteBuffer value = ByteBuffer.allocate(prefix.length + json.length + suffix.length);
    return value.put(prefix).put(json).put(suffix).flip().asReadOnlyBuffer();
  }

  /** Creates one general HTTP validation error. */
  private HttpJsonResponse httpError(int status, String message) {
    return protocolError(status, McpInvalidRequestException.ERROR_CODE, message);
  }

  /** Creates one encoded protocol error. */
  private HttpJsonResponse protocolError(int status, int code, String message) {
    JsonRpcErrorResponse response =
        new JsonRpcErrorResponse(McpJsonNull.INSTANCE, code, message, Optional.empty());
    return new HttpJsonResponse(
        status, Map.of(CONTENT_TYPE_HEADER, JSON_MEDIA_TYPE), serverKit.encode(response));
  }

  /** Collects one finite publication. */
  private CompletionStage<HttpMcpResponse> collect(Flow.Publisher<JsonRpcMessage> publisher) {
    MessageCollector collector = new MessageCollector();
    publisher.subscribe(collector);
    return collector.result;
  }

  /** Tests whether an Accept header permits one media type. */
  private static boolean accepts(HttpMcpRequest request, String mediaType) {
    String expected = mediaType.toLowerCase(Locale.ROOT);
    return request.headerValues("Accept").stream()
        .flatMap(value -> Arrays.stream(value.split(",")))
        .map(value -> value.trim().toLowerCase(Locale.ROOT))
        .map(value -> value.split(";", 2)[0].trim())
        .anyMatch(value -> value.equals("*/*") || value.equals(expected));
  }

  /** Tests whether one required header has exactly one non-blank value. */
  private static boolean hasOneValue(HttpMcpRequest request, String name) {
    List<String> values = request.headerValues(name);
    return values.size() == 1 && !blank(values.get(0));
  }

  /** Tests whether text is absent or blank. */
  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  /** Collects messages until the source completes. */
  private final class MessageCollector implements Flow.Subscriber<JsonRpcMessage> {
    private final ArrayList<JsonRpcMessage> messages = new ArrayList<>();
    private final CompletableFuture<HttpMcpResponse> result = new CompletableFuture<>();

    /** {@inheritDoc} */
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      result.whenComplete(
          (response, failure) -> {
            if (result.isCancelled()) {
              subscription.cancel();
            }
          });
      subscription.request(Long.MAX_VALUE);
    }

    /** {@inheritDoc} */
    @Override
    public void onNext(JsonRpcMessage item) {
      messages.add(item);
    }

    /** {@inheritDoc} */
    @Override
    public void onError(Throwable failure) {
      result.completeExceptionally(failure);
    }

    /** Converts one finite publication to an HTTP response. */
    private HttpMcpResponse finite(List<JsonRpcMessage> messages) {
      if (messages.isEmpty()) {
        return new HttpAcceptedResponse();
      }
      JsonRpcResponse response =
          messages.stream()
              .filter(JsonRpcResponse.class::isInstance)
              .map(JsonRpcResponse.class::cast)
              .reduce((first, second) -> second)
              .orElse(null);
      if (response == null) {
        return httpError(500, "The MCP server kit returned no response");
      }
      return new HttpJsonResponse(
          status(response),
          Map.of(CONTENT_TYPE_HEADER, JSON_MEDIA_TYPE),
          serverKit.encode(response));
    }

    /** {@inheritDoc} */
    @Override
    // Complete the returned future even if encoding fails with an Error.
    @SuppressWarnings("java:S1181")
    public void onComplete() {
      try {
        result.complete(finite(List.copyOf(messages)));
      } catch (Throwable failure) {
        result.completeExceptionally(failure);
      }
    }
  }

  /** A normalized HTTP origin. */
  private record Origin(String scheme, String host, int port) {}

  /** One tool-argument path mirrored to an HTTP header. */
  private record HeaderParameter(String headerName, List<String> path) {}
}
