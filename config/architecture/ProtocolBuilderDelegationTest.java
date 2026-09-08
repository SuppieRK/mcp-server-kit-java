package io.github.suppierk.mcp;

import io.github.suppierk.mcp.protocol.JsonRpcErrorResponse;
import io.github.suppierk.mcp.protocol.McpAnnotations;
import io.github.suppierk.mcp.protocol.McpAudioContent;
import io.github.suppierk.mcp.protocol.McpBlobResourceContents;
import io.github.suppierk.mcp.protocol.McpCacheableResult;
import io.github.suppierk.mcp.protocol.McpCallToolRequest;
import io.github.suppierk.mcp.protocol.McpCallToolRequestParams;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpCallToolResultResponse;
import io.github.suppierk.mcp.protocol.McpCancelledNotification;
import io.github.suppierk.mcp.protocol.McpCancelledNotificationParams;
import io.github.suppierk.mcp.protocol.McpClientCapabilities;
import io.github.suppierk.mcp.protocol.McpClientNotification;
import io.github.suppierk.mcp.protocol.McpCompleteRequest;
import io.github.suppierk.mcp.protocol.McpCompleteRequestParams;
import io.github.suppierk.mcp.protocol.McpCompleteResult;
import io.github.suppierk.mcp.protocol.McpCompleteResultResponse;
import io.github.suppierk.mcp.protocol.McpCreateMessageRequest;
import io.github.suppierk.mcp.protocol.McpCreateMessageRequestParams;
import io.github.suppierk.mcp.protocol.McpCreateMessageResult;
import io.github.suppierk.mcp.protocol.McpDiscoverRequest;
import io.github.suppierk.mcp.protocol.McpDiscoverResult;
import io.github.suppierk.mcp.protocol.McpDiscoverResultResponse;
import io.github.suppierk.mcp.protocol.McpElicitRequest;
import io.github.suppierk.mcp.protocol.McpElicitRequestFormParams;
import io.github.suppierk.mcp.protocol.McpElicitRequestURLParams;
import io.github.suppierk.mcp.protocol.McpElicitResult;
import io.github.suppierk.mcp.protocol.McpEmbeddedResource;
import io.github.suppierk.mcp.protocol.McpError;
import io.github.suppierk.mcp.protocol.McpGetPromptRequest;
import io.github.suppierk.mcp.protocol.McpGetPromptRequestParams;
import io.github.suppierk.mcp.protocol.McpGetPromptResult;
import io.github.suppierk.mcp.protocol.McpGetPromptResultResponse;
import io.github.suppierk.mcp.protocol.McpHeaderMismatchError;
import io.github.suppierk.mcp.protocol.McpIcon;
import io.github.suppierk.mcp.protocol.McpImageContent;
import io.github.suppierk.mcp.protocol.McpImplementation;
import io.github.suppierk.mcp.protocol.McpInputRequests;
import io.github.suppierk.mcp.protocol.McpInputRequiredResult;
import io.github.suppierk.mcp.protocol.McpInputResponseRequestParams;
import io.github.suppierk.mcp.protocol.McpInputResponses;
import io.github.suppierk.mcp.protocol.McpListPromptsRequest;
import io.github.suppierk.mcp.protocol.McpListPromptsResult;
import io.github.suppierk.mcp.protocol.McpListPromptsResultResponse;
import io.github.suppierk.mcp.protocol.McpListResourceTemplatesRequest;
import io.github.suppierk.mcp.protocol.McpListResourceTemplatesResult;
import io.github.suppierk.mcp.protocol.McpListResourceTemplatesResultResponse;
import io.github.suppierk.mcp.protocol.McpListResourcesRequest;
import io.github.suppierk.mcp.protocol.McpListResourcesResult;
import io.github.suppierk.mcp.protocol.McpListResourcesResultResponse;
import io.github.suppierk.mcp.protocol.McpListRootsRequest;
import io.github.suppierk.mcp.protocol.McpListRootsResult;
import io.github.suppierk.mcp.protocol.McpListToolsRequest;
import io.github.suppierk.mcp.protocol.McpListToolsResult;
import io.github.suppierk.mcp.protocol.McpListToolsResultResponse;
import io.github.suppierk.mcp.protocol.McpLoggingLevel;
import io.github.suppierk.mcp.protocol.McpLoggingMessageNotification;
import io.github.suppierk.mcp.protocol.McpLoggingMessageNotificationParams;
import io.github.suppierk.mcp.protocol.McpMetaObject;
import io.github.suppierk.mcp.protocol.McpMissingRequiredClientCapabilityError;
import io.github.suppierk.mcp.protocol.McpModelHint;
import io.github.suppierk.mcp.protocol.McpModelPreferences;
import io.github.suppierk.mcp.protocol.McpNotificationMetaObject;
import io.github.suppierk.mcp.protocol.McpNotificationParams;
import io.github.suppierk.mcp.protocol.McpPaginatedRequest;
import io.github.suppierk.mcp.protocol.McpPaginatedRequestParams;
import io.github.suppierk.mcp.protocol.McpPaginatedResult;
import io.github.suppierk.mcp.protocol.McpProgressNotification;
import io.github.suppierk.mcp.protocol.McpProgressNotificationParams;
import io.github.suppierk.mcp.protocol.McpPrompt;
import io.github.suppierk.mcp.protocol.McpPromptArgument;
import io.github.suppierk.mcp.protocol.McpPromptListChangedNotification;
import io.github.suppierk.mcp.protocol.McpPromptMessage;
import io.github.suppierk.mcp.protocol.McpReadResourceRequest;
import io.github.suppierk.mcp.protocol.McpReadResourceRequestParams;
import io.github.suppierk.mcp.protocol.McpReadResourceResult;
import io.github.suppierk.mcp.protocol.McpReadResourceResultResponse;
import io.github.suppierk.mcp.protocol.McpRequestMetaObject;
import io.github.suppierk.mcp.protocol.McpRequestParams;
import io.github.suppierk.mcp.protocol.McpResource;
import io.github.suppierk.mcp.protocol.McpResourceLink;
import io.github.suppierk.mcp.protocol.McpResourceListChangedNotification;
import io.github.suppierk.mcp.protocol.McpResourceRequestParams;
import io.github.suppierk.mcp.protocol.McpResourceTemplate;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotification;
import io.github.suppierk.mcp.protocol.McpResourceUpdatedNotificationParams;
import io.github.suppierk.mcp.protocol.McpResult;
import io.github.suppierk.mcp.protocol.McpResultMetaObject;
import io.github.suppierk.mcp.protocol.McpRole;
import io.github.suppierk.mcp.protocol.McpRoot;
import io.github.suppierk.mcp.protocol.McpSamplingMessage;
import io.github.suppierk.mcp.protocol.McpServerCapabilities;
import io.github.suppierk.mcp.protocol.McpSubscriptionFilter;
import io.github.suppierk.mcp.protocol.McpSubscriptionsAcknowledgedNotification;
import io.github.suppierk.mcp.protocol.McpSubscriptionsAcknowledgedNotificationParams;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenRequest;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenRequestParams;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenResult;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenResultMetaObject;
import io.github.suppierk.mcp.protocol.McpSubscriptionsListenResultResponse;
import io.github.suppierk.mcp.protocol.McpTextContent;
import io.github.suppierk.mcp.protocol.McpTextResourceContents;
import io.github.suppierk.mcp.protocol.McpToolChoice;
import io.github.suppierk.mcp.protocol.McpToolListChangedNotification;
import io.github.suppierk.mcp.protocol.McpToolResultContent;
import io.github.suppierk.mcp.protocol.McpToolUseContent;
import io.github.suppierk.mcp.protocol.McpUnsupportedProtocolVersionError;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Explicit fixtures for the remaining protocol relationships, using independent record
 * constructors.
 */
final class ProtocolBuilderDelegationTest {
  @TestFactory
  Stream<DynamicTest> protocolValuesBuildTheirConfiguredChildren() {
    return cases().stream()
        .map(
            example ->
                DynamicTest.dynamicTest(
                    example.parent().getTypeName()
                        + "."
                        + example.property()
                        + " -> "
                        + example.child().getTypeName(),
                    example::verify));
  }

  static List<PrimitiveBuilderCase<?, ?>> cases() {
    return List.of(
        new PrimitiveBuilderCase<>(
            JsonRpcErrorResponse.class,
            "error",
            McpError.class,
            "error",
            () ->
                JsonRpcErrorResponse.jsonRpcErrorResponse()
                    .id("original")
                    .error(originalMcpError()),
            (McpError.Builder child) ->
                child.code(2L).data(Optional.of("configured")).message("configured"),
            configuredMcpError(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpAudioContent.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpAudioContent.mcpAudioContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .data("original")
                    .mimeType("original"),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpAudioContent.class,
            "annotations",
            McpAnnotations.class,
            "annotations",
            () ->
                McpAudioContent.mcpAudioContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .data("original")
                    .mimeType("original"),
            (McpAnnotations.Builder child) ->
                child
                    .audience(Optional.of(List.of(McpRole.ASSISTANT)))
                    .lastModified(Optional.of("configured"))
                    .priority(Optional.of(0.75D)),
            Optional.of(configuredMcpAnnotations()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpBlobResourceContents.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpBlobResourceContents.mcpBlobResourceContents()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .blob("original")
                    .mimeType(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCacheableResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpCacheableResult.mcpCacheableResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .resultType("complete")
                    .ttlMs(1L),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolRequest.class,
            "params",
            McpCallToolRequestParams.class,
            "params",
            () ->
                McpCallToolRequest.mcpCallToolRequest()
                    .id("original")
                    .params(originalMcpCallToolRequestParams()),
            (McpCallToolRequestParams.Builder child) ->
                child
                    .meta(configuredMcpRequestMetaObject())
                    .arguments(Optional.of(Map.of("existing", "configured")))
                    .inputResponses(Optional.of(configuredMcpInputResponses()))
                    .name("configured")
                    .requestState(Optional.of("configured")),
            configuredMcpCallToolRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpCallToolRequestParams.mcpCallToolRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .arguments(Optional.of(Map.of("existing", "original")))
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .name("original")
                    .requestState(Optional.of("original")),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolRequestParams.class,
            "inputResponses",
            McpInputResponses.class,
            "inputResponses",
            () ->
                McpCallToolRequestParams.mcpCallToolRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .arguments(Optional.of(Map.of("existing", "original")))
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .name("original")
                    .requestState(Optional.of("original")),
            (McpInputResponses.Builder child) ->
                child.values(Map.of("existing", configuredMcpCreateMessageResult())),
            Optional.of(configuredMcpInputResponses()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolResultResponse.class,
            "result",
            McpCallToolResult.class,
            "callToolResult",
            () ->
                McpCallToolResultResponse.mcpCallToolResultResponse()
                    .id("original")
                    .result(originalMcpCallToolResult()),
            (McpCallToolResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .content(List.of(configuredMcpAudioContent()))
                    .isError(Optional.of(true))
                    .resultType("complete")
                    .structuredContent(Optional.of("configured")),
            configuredMcpCallToolResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCallToolResultResponse.class,
            "result",
            McpInputRequiredResult.class,
            "inputRequiredResult",
            () ->
                McpCallToolResultResponse.mcpCallToolResultResponse()
                    .id("original")
                    .result(originalMcpCallToolResult()),
            (McpInputRequiredResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .inputRequests(Optional.of(configuredMcpInputRequests()))
                    .requestState(Optional.of("configured"))
                    .resultType("complete"),
            configuredMcpInputRequiredResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCancelledNotification.class,
            "params",
            McpCancelledNotificationParams.class,
            "params",
            () ->
                McpCancelledNotification.mcpCancelledNotification()
                    .params(originalMcpCancelledNotificationParams()),
            (McpCancelledNotificationParams.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpNotificationMetaObject()))
                    .reason(Optional.of("configured"))
                    .requestId("configured"),
            configuredMcpCancelledNotificationParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCancelledNotificationParams.class,
            "meta",
            McpNotificationMetaObject.class,
            "meta",
            () ->
                McpCancelledNotificationParams.mcpCancelledNotificationParams()
                    .meta(Optional.of(originalMcpNotificationMetaObject()))
                    .reason(Optional.of("original"))
                    .requestId("original"),
            (McpNotificationMetaObject.Builder child) ->
                child.subscriptionId(Optional.of("configured")),
            Optional.of(configuredMcpNotificationMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpClientNotification.class,
            "params",
            McpCancelledNotificationParams.class,
            "params",
            () ->
                McpClientNotification.mcpClientNotification()
                    .params(originalMcpCancelledNotificationParams()),
            (McpCancelledNotificationParams.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpNotificationMetaObject()))
                    .reason(Optional.of("configured"))
                    .requestId("configured"),
            configuredMcpCancelledNotificationParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCompleteRequest.class,
            "params",
            McpCompleteRequestParams.class,
            "params",
            () ->
                McpCompleteRequest.mcpCompleteRequest()
                    .id("original")
                    .params(originalMcpCompleteRequestParams()),
            (McpCompleteRequestParams.Builder child) ->
                child
                    .meta(configuredMcpRequestMetaObject())
                    .argument(Map.of("existing", "configured"))
                    .context(Optional.of(Map.of("existing", "configured")))
                    .ref("configured"),
            configuredMcpCompleteRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCompleteRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpCompleteRequestParams.mcpCompleteRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .argument(Map.of("existing", "original"))
                    .context(Optional.of(Map.of("existing", "original")))
                    .ref("original"),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCompleteResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpCompleteResult.mcpCompleteResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .completion(Map.of("existing", "original"))
                    .resultType("complete"),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCompleteResultResponse.class,
            "result",
            McpCompleteResult.class,
            "result",
            () ->
                McpCompleteResultResponse.mcpCompleteResultResponse()
                    .id("original")
                    .result(originalMcpCompleteResult()),
            (McpCompleteResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .completion(Map.of("existing", "configured"))
                    .resultType("complete"),
            configuredMcpCompleteResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCreateMessageRequest.class,
            "params",
            McpCreateMessageRequestParams.class,
            "params",
            () ->
                McpCreateMessageRequest.mcpCreateMessageRequest()
                    .params(originalMcpCreateMessageRequestParams()),
            (McpCreateMessageRequestParams.Builder child) ->
                child
                    .includeContext(Optional.of("configured"))
                    .maxTokens(2L)
                    .messages(List.of(configuredMcpSamplingMessage()))
                    .metadata(Optional.of(Map.of("existing", "configured")))
                    .modelPreferences(Optional.of(configuredMcpModelPreferences()))
                    .stopSequences(Optional.of(List.of("configured")))
                    .systemPrompt(Optional.of("configured"))
                    .temperature(Optional.of(0.75D))
                    .toolChoice(Optional.of(configuredMcpToolChoice()))
                    .tools(Optional.of(List.of(Map.of("existing", "configured")))),
            configuredMcpCreateMessageRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCreateMessageRequestParams.class,
            "messages",
            McpSamplingMessage.class,
            "samplingMessage",
            () ->
                McpCreateMessageRequestParams.mcpCreateMessageRequestParams()
                    .includeContext(Optional.of("original"))
                    .maxTokens(1L)
                    .messages(List.of(originalMcpSamplingMessage()))
                    .metadata(Optional.of(Map.of("existing", "original")))
                    .modelPreferences(Optional.of(originalMcpModelPreferences()))
                    .stopSequences(Optional.of(List.of("original")))
                    .systemPrompt(Optional.of("original"))
                    .temperature(Optional.of(0.25D))
                    .toolChoice(Optional.of(originalMcpToolChoice()))
                    .tools(Optional.of(List.of(Map.of("existing", "original")))),
            (McpSamplingMessage.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .content("configured")
                    .role(McpRole.ASSISTANT),
            List.of(originalMcpSamplingMessage(), configuredMcpSamplingMessage()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCreateMessageRequestParams.class,
            "modelPreferences",
            McpModelPreferences.class,
            "modelPreferences",
            () ->
                McpCreateMessageRequestParams.mcpCreateMessageRequestParams()
                    .includeContext(Optional.of("original"))
                    .maxTokens(1L)
                    .messages(List.of(originalMcpSamplingMessage()))
                    .metadata(Optional.of(Map.of("existing", "original")))
                    .modelPreferences(Optional.of(originalMcpModelPreferences()))
                    .stopSequences(Optional.of(List.of("original")))
                    .systemPrompt(Optional.of("original"))
                    .temperature(Optional.of(0.25D))
                    .toolChoice(Optional.of(originalMcpToolChoice()))
                    .tools(Optional.of(List.of(Map.of("existing", "original")))),
            (McpModelPreferences.Builder child) ->
                child
                    .costPriority(Optional.of(0.75D))
                    .hints(Optional.of(List.of(configuredMcpModelHint())))
                    .intelligencePriority(Optional.of(0.75D))
                    .speedPriority(Optional.of(0.75D)),
            Optional.of(configuredMcpModelPreferences()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCreateMessageRequestParams.class,
            "toolChoice",
            McpToolChoice.class,
            "toolChoice",
            () ->
                McpCreateMessageRequestParams.mcpCreateMessageRequestParams()
                    .includeContext(Optional.of("original"))
                    .maxTokens(1L)
                    .messages(List.of(originalMcpSamplingMessage()))
                    .metadata(Optional.of(Map.of("existing", "original")))
                    .modelPreferences(Optional.of(originalMcpModelPreferences()))
                    .stopSequences(Optional.of(List.of("original")))
                    .systemPrompt(Optional.of("original"))
                    .temperature(Optional.of(0.25D))
                    .toolChoice(Optional.of(originalMcpToolChoice()))
                    .tools(Optional.of(List.of(Map.of("existing", "original")))),
            (McpToolChoice.Builder child) -> child.mode(Optional.of("configured")),
            Optional.of(configuredMcpToolChoice()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpCreateMessageResult.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpCreateMessageResult.mcpCreateMessageResult()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content("original")
                    .model("original")
                    .role(McpRole.USER)
                    .stopReason(Optional.of("original")),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpDiscoverRequest.class,
            "params",
            McpRequestParams.class,
            "params",
            () ->
                McpDiscoverRequest.mcpDiscoverRequest()
                    .id("original")
                    .params(originalMcpRequestParams()),
            (McpRequestParams.Builder child) -> child.meta(configuredMcpRequestMetaObject()),
            configuredMcpRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpDiscoverResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpDiscoverResult.mcpDiscoverResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .capabilities(originalMcpServerCapabilities())
                    .instructions(Optional.of("original"))
                    .resultType("complete")
                    .supportedVersions(List.of("original"))
                    .ttlMs(1L),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpDiscoverResult.class,
            "capabilities",
            McpServerCapabilities.class,
            "capabilities",
            () ->
                McpDiscoverResult.mcpDiscoverResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .capabilities(originalMcpServerCapabilities())
                    .instructions(Optional.of("original"))
                    .resultType("complete")
                    .supportedVersions(List.of("original"))
                    .ttlMs(1L),
            (McpServerCapabilities.Builder child) ->
                child
                    .completions(Optional.of(Map.of("existing", "configured")))
                    .experimental(Optional.of(Map.of("existing", "configured")))
                    .extensions(Optional.of(Map.of("existing", "configured")))
                    .logging(Optional.of(Map.of("existing", "configured")))
                    .prompts(Optional.of(Map.of("existing", "configured")))
                    .resources(Optional.of(Map.of("existing", "configured")))
                    .tools(Optional.of(Map.of("existing", "configured"))),
            configuredMcpServerCapabilities(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpDiscoverResultResponse.class,
            "result",
            McpDiscoverResult.class,
            "result",
            () ->
                McpDiscoverResultResponse.mcpDiscoverResultResponse()
                    .id("original")
                    .result(originalMcpDiscoverResult()),
            (McpDiscoverResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .cacheScope("public")
                    .capabilities(configuredMcpServerCapabilities())
                    .instructions(Optional.of("configured"))
                    .resultType("complete")
                    .supportedVersions(List.of("configured"))
                    .ttlMs(2L),
            configuredMcpDiscoverResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpElicitRequest.class,
            "params",
            McpElicitRequestFormParams.class,
            "elicitRequestFormParams",
            () -> McpElicitRequest.mcpElicitRequest().params(originalMcpElicitRequestFormParams()),
            (McpElicitRequestFormParams.Builder child) ->
                child.message("configured").requestedSchema(Map.of("existing", "configured")),
            configuredMcpElicitRequestFormParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpElicitRequest.class,
            "params",
            McpElicitRequestURLParams.class,
            "elicitRequestURLParams",
            () -> McpElicitRequest.mcpElicitRequest().params(originalMcpElicitRequestFormParams()),
            (McpElicitRequestURLParams.Builder child) ->
                child.message("configured").url(URI.create("file:///configured")),
            configuredMcpElicitRequestURLParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpEmbeddedResource.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpEmbeddedResource.mcpEmbeddedResource()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .resource("original"),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpEmbeddedResource.class,
            "annotations",
            McpAnnotations.class,
            "annotations",
            () ->
                McpEmbeddedResource.mcpEmbeddedResource()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .resource("original"),
            (McpAnnotations.Builder child) ->
                child
                    .audience(Optional.of(List.of(McpRole.ASSISTANT)))
                    .lastModified(Optional.of("configured"))
                    .priority(Optional.of(0.75D)),
            Optional.of(configuredMcpAnnotations()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpGetPromptRequest.class,
            "params",
            McpGetPromptRequestParams.class,
            "params",
            () ->
                McpGetPromptRequest.mcpGetPromptRequest()
                    .id("original")
                    .params(originalMcpGetPromptRequestParams()),
            (McpGetPromptRequestParams.Builder child) ->
                child
                    .meta(configuredMcpRequestMetaObject())
                    .arguments(Optional.of(Map.of("existing", "configured")))
                    .inputResponses(Optional.of(configuredMcpInputResponses()))
                    .name("configured")
                    .requestState(Optional.of("configured")),
            configuredMcpGetPromptRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpGetPromptRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpGetPromptRequestParams.mcpGetPromptRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .arguments(Optional.of(Map.of("existing", "original")))
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .name("original")
                    .requestState(Optional.of("original")),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpGetPromptRequestParams.class,
            "inputResponses",
            McpInputResponses.class,
            "inputResponses",
            () ->
                McpGetPromptRequestParams.mcpGetPromptRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .arguments(Optional.of(Map.of("existing", "original")))
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .name("original")
                    .requestState(Optional.of("original")),
            (McpInputResponses.Builder child) ->
                child.values(Map.of("existing", configuredMcpCreateMessageResult())),
            Optional.of(configuredMcpInputResponses()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpGetPromptResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpGetPromptResult.mcpGetPromptResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .description(Optional.of("original"))
                    .messages(List.of(originalMcpPromptMessage()))
                    .resultType("complete"),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpGetPromptResult.class,
            "messages",
            McpPromptMessage.class,
            "promptMessage",
            () ->
                McpGetPromptResult.mcpGetPromptResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .description(Optional.of("original"))
                    .messages(List.of(originalMcpPromptMessage()))
                    .resultType("complete"),
            (McpPromptMessage.Builder child) ->
                child.content(configuredMcpAudioContent()).role(McpRole.ASSISTANT),
            List.of(originalMcpPromptMessage(), configuredMcpPromptMessage()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpGetPromptResultResponse.class,
            "result",
            McpGetPromptResult.class,
            "getPromptResult",
            () ->
                McpGetPromptResultResponse.mcpGetPromptResultResponse()
                    .id("original")
                    .result(originalMcpGetPromptResult()),
            (McpGetPromptResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .description(Optional.of("configured"))
                    .messages(List.of(configuredMcpPromptMessage()))
                    .resultType("complete"),
            configuredMcpGetPromptResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpGetPromptResultResponse.class,
            "result",
            McpInputRequiredResult.class,
            "inputRequiredResult",
            () ->
                McpGetPromptResultResponse.mcpGetPromptResultResponse()
                    .id("original")
                    .result(originalMcpGetPromptResult()),
            (McpInputRequiredResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .inputRequests(Optional.of(configuredMcpInputRequests()))
                    .requestState(Optional.of("configured"))
                    .resultType("complete"),
            configuredMcpInputRequiredResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpHeaderMismatchError.class,
            "error",
            McpHeaderMismatchError.Error.class,
            "error",
            () ->
                McpHeaderMismatchError.mcpHeaderMismatchError()
                    .error(originalMcpHeaderMismatchErrorError())
                    .id(Optional.of("original")),
            (McpHeaderMismatchError.Error.Builder child) ->
                child.message("configured").data(Optional.of("configured")),
            configuredMcpHeaderMismatchErrorError(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpImageContent.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpImageContent.mcpImageContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .data("original")
                    .mimeType("original"),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpImageContent.class,
            "annotations",
            McpAnnotations.class,
            "annotations",
            () ->
                McpImageContent.mcpImageContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .data("original")
                    .mimeType("original"),
            (McpAnnotations.Builder child) ->
                child
                    .audience(Optional.of(List.of(McpRole.ASSISTANT)))
                    .lastModified(Optional.of("configured"))
                    .priority(Optional.of(0.75D)),
            Optional.of(configuredMcpAnnotations()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpImplementation.class,
            "icons",
            McpIcon.class,
            "icon",
            () ->
                McpImplementation.mcpImplementation()
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .name("original")
                    .title(Optional.of("original"))
                    .version("original")
                    .websiteUrl(Optional.of(URI.create("file:///original"))),
            (McpIcon.Builder child) ->
                child
                    .mimeType(Optional.of("configured"))
                    .sizes(Optional.of(List.of("configured")))
                    .src(URI.create("file:///configured"))
                    .theme(Optional.of("dark")),
            Optional.of(List.of(originalMcpIcon(), configuredMcpIcon())),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpInputRequests.class,
            "values",
            McpCreateMessageRequest.class,
            "createMessageRequest",
            () ->
                McpInputRequests.mcpInputRequests()
                    .values(Map.of("existing", originalMcpCreateMessageRequest())),
            (McpCreateMessageRequest.Builder child) ->
                child.params(configuredMcpCreateMessageRequestParams()),
            Map.of(
                "existing",
                originalMcpCreateMessageRequest(),
                "added",
                configuredMcpCreateMessageRequest()),
            List.of("added")),
        new PrimitiveBuilderCase<>(
            McpInputRequests.class,
            "values",
            McpElicitRequest.class,
            "elicitRequest",
            () ->
                McpInputRequests.mcpInputRequests()
                    .values(Map.of("existing", originalMcpCreateMessageRequest())),
            (McpElicitRequest.Builder child) ->
                child.params(configuredMcpElicitRequestFormParams()),
            Map.of(
                "existing",
                originalMcpCreateMessageRequest(),
                "added",
                configuredMcpElicitRequest()),
            List.of("added")),
        new PrimitiveBuilderCase<>(
            McpInputRequests.class,
            "values",
            McpListRootsRequest.class,
            "listRootsRequest",
            () ->
                McpInputRequests.mcpInputRequests()
                    .values(Map.of("existing", originalMcpCreateMessageRequest())),
            (McpListRootsRequest.Builder child) ->
                child.params(Optional.of(Map.of("existing", "configured"))),
            Map.of(
                "existing",
                originalMcpCreateMessageRequest(),
                "added",
                configuredMcpListRootsRequest()),
            List.of("added")),
        new PrimitiveBuilderCase<>(
            McpInputRequiredResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpInputRequiredResult.mcpInputRequiredResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .inputRequests(Optional.of(originalMcpInputRequests()))
                    .requestState(Optional.of("original"))
                    .resultType("complete"),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpInputRequiredResult.class,
            "inputRequests",
            McpInputRequests.class,
            "inputRequests",
            () ->
                McpInputRequiredResult.mcpInputRequiredResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .inputRequests(Optional.of(originalMcpInputRequests()))
                    .requestState(Optional.of("original"))
                    .resultType("complete"),
            (McpInputRequests.Builder child) ->
                child.values(Map.of("existing", configuredMcpCreateMessageRequest())),
            Optional.of(configuredMcpInputRequests()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpInputResponseRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpInputResponseRequestParams.mcpInputResponseRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .requestState(Optional.of("original")),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpInputResponseRequestParams.class,
            "inputResponses",
            McpInputResponses.class,
            "inputResponses",
            () ->
                McpInputResponseRequestParams.mcpInputResponseRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .requestState(Optional.of("original")),
            (McpInputResponses.Builder child) ->
                child.values(Map.of("existing", configuredMcpCreateMessageResult())),
            Optional.of(configuredMcpInputResponses()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpInputResponses.class,
            "values",
            McpCreateMessageResult.class,
            "createMessageResult",
            () ->
                McpInputResponses.mcpInputResponses()
                    .values(Map.of("existing", originalMcpCreateMessageResult())),
            (McpCreateMessageResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .content("configured")
                    .model("configured")
                    .role(McpRole.ASSISTANT)
                    .stopReason(Optional.of("configured")),
            Map.of(
                "existing",
                originalMcpCreateMessageResult(),
                "added",
                configuredMcpCreateMessageResult()),
            List.of("added")),
        new PrimitiveBuilderCase<>(
            McpInputResponses.class,
            "values",
            McpElicitResult.class,
            "elicitResult",
            () ->
                McpInputResponses.mcpInputResponses()
                    .values(Map.of("existing", originalMcpCreateMessageResult())),
            (McpElicitResult.Builder child) ->
                child.action("configured").content(Optional.of(Map.of("existing", "configured"))),
            Map.of(
                "existing", originalMcpCreateMessageResult(), "added", configuredMcpElicitResult()),
            List.of("added")),
        new PrimitiveBuilderCase<>(
            McpInputResponses.class,
            "values",
            McpListRootsResult.class,
            "listRootsResult",
            () ->
                McpInputResponses.mcpInputResponses()
                    .values(Map.of("existing", originalMcpCreateMessageResult())),
            (McpListRootsResult.Builder child) -> child.roots(List.of(configuredMcpRoot())),
            Map.of(
                "existing",
                originalMcpCreateMessageResult(),
                "added",
                configuredMcpListRootsResult()),
            List.of("added")),
        new PrimitiveBuilderCase<>(
            McpListPromptsRequest.class,
            "params",
            McpPaginatedRequestParams.class,
            "params",
            () ->
                McpListPromptsRequest.mcpListPromptsRequest()
                    .id("original")
                    .params(originalMcpPaginatedRequestParams()),
            (McpPaginatedRequestParams.Builder child) ->
                child.meta(configuredMcpRequestMetaObject()).cursor(Optional.of("configured")),
            configuredMcpPaginatedRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListPromptsResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpListPromptsResult.mcpListPromptsResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .nextCursor(Optional.of("original"))
                    .prompts(List.of(originalMcpPrompt()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListPromptsResult.class,
            "prompts",
            McpPrompt.class,
            "prompt",
            () ->
                McpListPromptsResult.mcpListPromptsResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .nextCursor(Optional.of("original"))
                    .prompts(List.of(originalMcpPrompt()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpPrompt.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .arguments(Optional.of(List.of(configuredMcpPromptArgument())))
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .name("configured")
                    .title(Optional.of("configured")),
            List.of(originalMcpPrompt(), configuredMcpPrompt()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListPromptsResultResponse.class,
            "result",
            McpListPromptsResult.class,
            "result",
            () ->
                McpListPromptsResultResponse.mcpListPromptsResultResponse()
                    .id("original")
                    .result(originalMcpListPromptsResult()),
            (McpListPromptsResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .cacheScope("public")
                    .nextCursor(Optional.of("configured"))
                    .prompts(List.of(configuredMcpPrompt()))
                    .resultType("complete")
                    .ttlMs(2L),
            configuredMcpListPromptsResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourceTemplatesRequest.class,
            "params",
            McpPaginatedRequestParams.class,
            "params",
            () ->
                McpListResourceTemplatesRequest.mcpListResourceTemplatesRequest()
                    .id("original")
                    .params(originalMcpPaginatedRequestParams()),
            (McpPaginatedRequestParams.Builder child) ->
                child.meta(configuredMcpRequestMetaObject()).cursor(Optional.of("configured")),
            configuredMcpPaginatedRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourceTemplatesResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpListResourceTemplatesResult.mcpListResourceTemplatesResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .nextCursor(Optional.of("original"))
                    .resourceTemplates(List.of(originalMcpResourceTemplate()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourceTemplatesResult.class,
            "resourceTemplates",
            McpResourceTemplate.class,
            "resourceTemplate",
            () ->
                McpListResourceTemplatesResult.mcpListResourceTemplatesResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .nextCursor(Optional.of("original"))
                    .resourceTemplates(List.of(originalMcpResourceTemplate()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpResourceTemplate.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .mimeType(Optional.of("configured"))
                    .name("configured")
                    .title(Optional.of("configured"))
                    .uriTemplate("configured"),
            List.of(originalMcpResourceTemplate(), configuredMcpResourceTemplate()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourceTemplatesResultResponse.class,
            "result",
            McpListResourceTemplatesResult.class,
            "result",
            () ->
                McpListResourceTemplatesResultResponse.mcpListResourceTemplatesResultResponse()
                    .id("original")
                    .result(originalMcpListResourceTemplatesResult()),
            (McpListResourceTemplatesResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .cacheScope("public")
                    .nextCursor(Optional.of("configured"))
                    .resourceTemplates(List.of(configuredMcpResourceTemplate()))
                    .resultType("complete")
                    .ttlMs(2L),
            configuredMcpListResourceTemplatesResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourcesRequest.class,
            "params",
            McpPaginatedRequestParams.class,
            "params",
            () ->
                McpListResourcesRequest.mcpListResourcesRequest()
                    .id("original")
                    .params(originalMcpPaginatedRequestParams()),
            (McpPaginatedRequestParams.Builder child) ->
                child.meta(configuredMcpRequestMetaObject()).cursor(Optional.of("configured")),
            configuredMcpPaginatedRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourcesResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpListResourcesResult.mcpListResourcesResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .nextCursor(Optional.of("original"))
                    .resources(List.of(originalMcpResource()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourcesResult.class,
            "resources",
            McpResource.class,
            "resource",
            () ->
                McpListResourcesResult.mcpListResourcesResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .nextCursor(Optional.of("original"))
                    .resources(List.of(originalMcpResource()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpResource.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .mimeType(Optional.of("configured"))
                    .name("configured")
                    .size(Optional.of(2L))
                    .title(Optional.of("configured"))
                    .uri(URI.create("file:///configured")),
            List.of(originalMcpResource(), configuredMcpResource()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListResourcesResultResponse.class,
            "result",
            McpListResourcesResult.class,
            "result",
            () ->
                McpListResourcesResultResponse.mcpListResourcesResultResponse()
                    .id("original")
                    .result(originalMcpListResourcesResult()),
            (McpListResourcesResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .cacheScope("public")
                    .nextCursor(Optional.of("configured"))
                    .resources(List.of(configuredMcpResource()))
                    .resultType("complete")
                    .ttlMs(2L),
            configuredMcpListResourcesResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListRootsResult.class,
            "roots",
            McpRoot.class,
            "root",
            () -> McpListRootsResult.mcpListRootsResult().roots(List.of(originalMcpRoot())),
            (McpRoot.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .name(Optional.of("configured"))
                    .uri(URI.create("file:///configured")),
            List.of(originalMcpRoot(), configuredMcpRoot()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListToolsRequest.class,
            "params",
            McpPaginatedRequestParams.class,
            "params",
            () ->
                McpListToolsRequest.mcpListToolsRequest()
                    .id("original")
                    .params(originalMcpPaginatedRequestParams()),
            (McpPaginatedRequestParams.Builder child) ->
                child.meta(configuredMcpRequestMetaObject()).cursor(Optional.of("configured")),
            configuredMcpPaginatedRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListToolsResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpListToolsResult.mcpListToolsResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .nextCursor(Optional.of("original"))
                    .resultType("complete")
                    .tools(List.of(Map.of("existing", "original")))
                    .ttlMs(1L),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpListToolsResultResponse.class,
            "result",
            McpListToolsResult.class,
            "result",
            () ->
                McpListToolsResultResponse.mcpListToolsResultResponse()
                    .id("original")
                    .result(originalMcpListToolsResult()),
            (McpListToolsResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .cacheScope("public")
                    .nextCursor(Optional.of("configured"))
                    .resultType("complete")
                    .tools(List.of(Map.of("existing", "configured")))
                    .ttlMs(2L),
            configuredMcpListToolsResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpLoggingMessageNotification.class,
            "params",
            McpLoggingMessageNotificationParams.class,
            "params",
            () ->
                McpLoggingMessageNotification.mcpLoggingMessageNotification()
                    .params(originalMcpLoggingMessageNotificationParams()),
            (McpLoggingMessageNotificationParams.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpNotificationMetaObject()))
                    .data("configured")
                    .level(McpLoggingLevel.WARNING)
                    .logger(Optional.of("configured")),
            configuredMcpLoggingMessageNotificationParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpLoggingMessageNotificationParams.class,
            "meta",
            McpNotificationMetaObject.class,
            "meta",
            () ->
                McpLoggingMessageNotificationParams.mcpLoggingMessageNotificationParams()
                    .meta(Optional.of(originalMcpNotificationMetaObject()))
                    .data("original")
                    .level(McpLoggingLevel.INFO)
                    .logger(Optional.of("original")),
            (McpNotificationMetaObject.Builder child) ->
                child.subscriptionId(Optional.of("configured")),
            Optional.of(configuredMcpNotificationMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpMissingRequiredClientCapabilityError.Data.class,
            "requiredCapabilities",
            McpClientCapabilities.class,
            "requiredCapabilities",
            () ->
                McpMissingRequiredClientCapabilityError.Data
                    .mcpMissingRequiredClientCapabilityErrorData()
                    .requiredCapabilities(originalMcpClientCapabilities()),
            (McpClientCapabilities.Builder child) ->
                child
                    .elicitation(Optional.of(Map.of("existing", "configured")))
                    .experimental(Optional.of(Map.of("existing", "configured")))
                    .extensions(Optional.of(Map.of("existing", "configured")))
                    .roots(Optional.of(Map.of("existing", "configured")))
                    .sampling(Optional.of(Map.of("existing", "configured"))),
            configuredMcpClientCapabilities(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpMissingRequiredClientCapabilityError.Error.class,
            "data",
            McpMissingRequiredClientCapabilityError.Data.class,
            "data",
            () ->
                McpMissingRequiredClientCapabilityError.Error
                    .mcpMissingRequiredClientCapabilityErrorError()
                    .message("original")
                    .data(originalMcpMissingRequiredClientCapabilityErrorData()),
            (McpMissingRequiredClientCapabilityError.Data.Builder child) ->
                child.requiredCapabilities(configuredMcpClientCapabilities()),
            configuredMcpMissingRequiredClientCapabilityErrorData(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpMissingRequiredClientCapabilityError.class,
            "error",
            McpMissingRequiredClientCapabilityError.Error.class,
            "error",
            () ->
                McpMissingRequiredClientCapabilityError.mcpMissingRequiredClientCapabilityError()
                    .error(originalMcpMissingRequiredClientCapabilityErrorError())
                    .id(Optional.of("original")),
            (McpMissingRequiredClientCapabilityError.Error.Builder child) ->
                child
                    .message("configured")
                    .data(configuredMcpMissingRequiredClientCapabilityErrorData()),
            configuredMcpMissingRequiredClientCapabilityErrorError(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpModelPreferences.class,
            "hints",
            McpModelHint.class,
            "modelHint",
            () ->
                McpModelPreferences.mcpModelPreferences()
                    .costPriority(Optional.of(0.25D))
                    .hints(Optional.of(List.of(originalMcpModelHint())))
                    .intelligencePriority(Optional.of(0.25D))
                    .speedPriority(Optional.of(0.25D)),
            (McpModelHint.Builder child) -> child.name(Optional.of("configured")),
            Optional.of(List.of(originalMcpModelHint(), configuredMcpModelHint())),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpNotificationParams.class,
            "meta",
            McpNotificationMetaObject.class,
            "meta",
            () ->
                McpNotificationParams.mcpNotificationParams()
                    .meta(Optional.of(originalMcpNotificationMetaObject())),
            (McpNotificationMetaObject.Builder child) ->
                child.subscriptionId(Optional.of("configured")),
            Optional.of(configuredMcpNotificationMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPaginatedRequest.class,
            "params",
            McpPaginatedRequestParams.class,
            "params",
            () ->
                McpPaginatedRequest.mcpPaginatedRequest()
                    .id("original")
                    .method("original")
                    .params(originalMcpPaginatedRequestParams()),
            (McpPaginatedRequestParams.Builder child) ->
                child.meta(configuredMcpRequestMetaObject()).cursor(Optional.of("configured")),
            configuredMcpPaginatedRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPaginatedRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpPaginatedRequestParams.mcpPaginatedRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .cursor(Optional.of("original")),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPaginatedResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpPaginatedResult.mcpPaginatedResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .nextCursor(Optional.of("original"))
                    .resultType("complete"),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpProgressNotification.class,
            "params",
            McpProgressNotificationParams.class,
            "params",
            () ->
                McpProgressNotification.mcpProgressNotification()
                    .params(originalMcpProgressNotificationParams()),
            (McpProgressNotificationParams.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpNotificationMetaObject()))
                    .message(Optional.of("configured"))
                    .progress(0.75D)
                    .progressToken("configured")
                    .total(Optional.of(0.75D)),
            configuredMcpProgressNotificationParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpProgressNotificationParams.class,
            "meta",
            McpNotificationMetaObject.class,
            "meta",
            () ->
                McpProgressNotificationParams.mcpProgressNotificationParams()
                    .meta(Optional.of(originalMcpNotificationMetaObject()))
                    .message(Optional.of("original"))
                    .progress(0.25D)
                    .progressToken("original")
                    .total(Optional.of(0.25D)),
            (McpNotificationMetaObject.Builder child) ->
                child.subscriptionId(Optional.of("configured")),
            Optional.of(configuredMcpNotificationMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPrompt.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpPrompt.mcpPrompt()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .arguments(Optional.of(List.of(originalMcpPromptArgument())))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .name("original")
                    .title(Optional.of("original")),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPrompt.class,
            "arguments",
            McpPromptArgument.class,
            "promptArgument",
            () ->
                McpPrompt.mcpPrompt()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .arguments(Optional.of(List.of(originalMcpPromptArgument())))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .name("original")
                    .title(Optional.of("original")),
            (McpPromptArgument.Builder child) ->
                child
                    .description(Optional.of("configured"))
                    .name("configured")
                    .required(Optional.of(true))
                    .title(Optional.of("configured")),
            Optional.of(List.of(originalMcpPromptArgument(), configuredMcpPromptArgument())),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPrompt.class,
            "icons",
            McpIcon.class,
            "icon",
            () ->
                McpPrompt.mcpPrompt()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .arguments(Optional.of(List.of(originalMcpPromptArgument())))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .name("original")
                    .title(Optional.of("original")),
            (McpIcon.Builder child) ->
                child
                    .mimeType(Optional.of("configured"))
                    .sizes(Optional.of(List.of("configured")))
                    .src(URI.create("file:///configured"))
                    .theme(Optional.of("dark")),
            Optional.of(List.of(originalMcpIcon(), configuredMcpIcon())),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPromptListChangedNotification.class,
            "params",
            McpNotificationParams.class,
            "params",
            () ->
                McpPromptListChangedNotification.mcpPromptListChangedNotification()
                    .params(Optional.of(originalMcpNotificationParams())),
            (McpNotificationParams.Builder child) ->
                child.meta(Optional.of(configuredMcpNotificationMetaObject())),
            Optional.of(configuredMcpNotificationParams()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPromptMessage.class,
            "content",
            McpAudioContent.class,
            "audioContent",
            () ->
                McpPromptMessage.mcpPromptMessage()
                    .content(originalMcpAudioContent())
                    .role(McpRole.USER),
            (McpAudioContent.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .data("configured")
                    .mimeType("configured"),
            configuredMcpAudioContent(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPromptMessage.class,
            "content",
            McpEmbeddedResource.class,
            "embeddedResource",
            () ->
                McpPromptMessage.mcpPromptMessage()
                    .content(originalMcpAudioContent())
                    .role(McpRole.USER),
            (McpEmbeddedResource.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .resource("configured"),
            configuredMcpEmbeddedResource(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPromptMessage.class,
            "content",
            McpImageContent.class,
            "imageContent",
            () ->
                McpPromptMessage.mcpPromptMessage()
                    .content(originalMcpAudioContent())
                    .role(McpRole.USER),
            (McpImageContent.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .data("configured")
                    .mimeType("configured"),
            configuredMcpImageContent(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPromptMessage.class,
            "content",
            McpResourceLink.class,
            "resourceLink",
            () ->
                McpPromptMessage.mcpPromptMessage()
                    .content(originalMcpAudioContent())
                    .role(McpRole.USER),
            (McpResourceLink.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .mimeType(Optional.of("configured"))
                    .name("configured")
                    .size(Optional.of(2L))
                    .title(Optional.of("configured"))
                    .uri(URI.create("file:///configured")),
            configuredMcpResourceLink(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpPromptMessage.class,
            "content",
            McpTextContent.class,
            "textContent",
            () ->
                McpPromptMessage.mcpPromptMessage()
                    .content(originalMcpAudioContent())
                    .role(McpRole.USER),
            (McpTextContent.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .text("configured"),
            configuredMcpTextContent(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceRequest.class,
            "params",
            McpReadResourceRequestParams.class,
            "params",
            () ->
                McpReadResourceRequest.mcpReadResourceRequest()
                    .id("original")
                    .params(originalMcpReadResourceRequestParams()),
            (McpReadResourceRequestParams.Builder child) ->
                child
                    .meta(configuredMcpRequestMetaObject())
                    .inputResponses(Optional.of(configuredMcpInputResponses()))
                    .requestState(Optional.of("configured"))
                    .uri(URI.create("file:///configured")),
            configuredMcpReadResourceRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpReadResourceRequestParams.mcpReadResourceRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .requestState(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceRequestParams.class,
            "inputResponses",
            McpInputResponses.class,
            "inputResponses",
            () ->
                McpReadResourceRequestParams.mcpReadResourceRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .inputResponses(Optional.of(originalMcpInputResponses()))
                    .requestState(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpInputResponses.Builder child) ->
                child.values(Map.of("existing", configuredMcpCreateMessageResult())),
            Optional.of(configuredMcpInputResponses()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpReadResourceResult.mcpReadResourceResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .contents(List.of(originalMcpBlobResourceContents()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceResult.class,
            "contents",
            McpBlobResourceContents.class,
            "blobResourceContents",
            () ->
                McpReadResourceResult.mcpReadResourceResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .contents(List.of(originalMcpBlobResourceContents()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpBlobResourceContents.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .blob("configured")
                    .mimeType(Optional.of("configured"))
                    .uri(URI.create("file:///configured")),
            List.of(originalMcpBlobResourceContents(), configuredMcpBlobResourceContents()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceResult.class,
            "contents",
            McpTextResourceContents.class,
            "textResourceContents",
            () ->
                McpReadResourceResult.mcpReadResourceResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .cacheScope("private")
                    .contents(List.of(originalMcpBlobResourceContents()))
                    .resultType("complete")
                    .ttlMs(1L),
            (McpTextResourceContents.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .mimeType(Optional.of("configured"))
                    .text("configured")
                    .uri(URI.create("file:///configured")),
            List.of(originalMcpBlobResourceContents(), configuredMcpTextResourceContents()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceResultResponse.class,
            "result",
            McpInputRequiredResult.class,
            "inputRequiredResult",
            () ->
                McpReadResourceResultResponse.mcpReadResourceResultResponse()
                    .id("original")
                    .result(originalMcpInputRequiredResult()),
            (McpInputRequiredResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .inputRequests(Optional.of(configuredMcpInputRequests()))
                    .requestState(Optional.of("configured"))
                    .resultType("complete"),
            configuredMcpInputRequiredResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpReadResourceResultResponse.class,
            "result",
            McpReadResourceResult.class,
            "readResourceResult",
            () ->
                McpReadResourceResultResponse.mcpReadResourceResultResponse()
                    .id("original")
                    .result(originalMcpInputRequiredResult()),
            (McpReadResourceResult.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpResultMetaObject()))
                    .cacheScope("public")
                    .contents(List.of(configuredMcpBlobResourceContents()))
                    .resultType("complete")
                    .ttlMs(2L),
            configuredMcpReadResourceResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpRequestMetaObject.class,
            "clientCapabilities",
            McpClientCapabilities.class,
            "clientCapabilities",
            () ->
                McpRequestMetaObject.mcpRequestMetaObject()
                    .clientCapabilities(originalMcpClientCapabilities())
                    .clientInfo(Optional.of(originalMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.INFO))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("original"))
                    .extensions(Map.of("existing", "original")),
            (McpClientCapabilities.Builder child) ->
                child
                    .elicitation(Optional.of(Map.of("existing", "configured")))
                    .experimental(Optional.of(Map.of("existing", "configured")))
                    .extensions(Optional.of(Map.of("existing", "configured")))
                    .roots(Optional.of(Map.of("existing", "configured")))
                    .sampling(Optional.of(Map.of("existing", "configured"))),
            configuredMcpClientCapabilities(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpRequestMetaObject.class,
            "clientInfo",
            McpImplementation.class,
            "clientInfo",
            () ->
                McpRequestMetaObject.mcpRequestMetaObject()
                    .clientCapabilities(originalMcpClientCapabilities())
                    .clientInfo(Optional.of(originalMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.INFO))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("original"))
                    .extensions(Map.of("existing", "original")),
            (McpImplementation.Builder child) ->
                child
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .name("configured")
                    .title(Optional.of("configured"))
                    .version("configured")
                    .websiteUrl(Optional.of(URI.create("file:///configured"))),
            Optional.of(configuredMcpImplementation()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () -> McpRequestParams.mcpRequestParams().meta(originalMcpRequestMetaObject()),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResource.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpResource.mcpResource()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .size(Optional.of(1L))
                    .title(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResource.class,
            "annotations",
            McpAnnotations.class,
            "annotations",
            () ->
                McpResource.mcpResource()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .size(Optional.of(1L))
                    .title(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpAnnotations.Builder child) ->
                child
                    .audience(Optional.of(List.of(McpRole.ASSISTANT)))
                    .lastModified(Optional.of("configured"))
                    .priority(Optional.of(0.75D)),
            Optional.of(configuredMcpAnnotations()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResource.class,
            "icons",
            McpIcon.class,
            "icon",
            () ->
                McpResource.mcpResource()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .size(Optional.of(1L))
                    .title(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpIcon.Builder child) ->
                child
                    .mimeType(Optional.of("configured"))
                    .sizes(Optional.of(List.of("configured")))
                    .src(URI.create("file:///configured"))
                    .theme(Optional.of("dark")),
            Optional.of(List.of(originalMcpIcon(), configuredMcpIcon())),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceLink.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpResourceLink.mcpResourceLink()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .size(Optional.of(1L))
                    .title(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceLink.class,
            "annotations",
            McpAnnotations.class,
            "annotations",
            () ->
                McpResourceLink.mcpResourceLink()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .size(Optional.of(1L))
                    .title(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpAnnotations.Builder child) ->
                child
                    .audience(Optional.of(List.of(McpRole.ASSISTANT)))
                    .lastModified(Optional.of("configured"))
                    .priority(Optional.of(0.75D)),
            Optional.of(configuredMcpAnnotations()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceLink.class,
            "icons",
            McpIcon.class,
            "icon",
            () ->
                McpResourceLink.mcpResourceLink()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .size(Optional.of(1L))
                    .title(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpIcon.Builder child) ->
                child
                    .mimeType(Optional.of("configured"))
                    .sizes(Optional.of(List.of("configured")))
                    .src(URI.create("file:///configured"))
                    .theme(Optional.of("dark")),
            Optional.of(List.of(originalMcpIcon(), configuredMcpIcon())),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceListChangedNotification.class,
            "params",
            McpNotificationParams.class,
            "params",
            () ->
                McpResourceListChangedNotification.mcpResourceListChangedNotification()
                    .params(Optional.of(originalMcpNotificationParams())),
            (McpNotificationParams.Builder child) ->
                child.meta(Optional.of(configuredMcpNotificationMetaObject())),
            Optional.of(configuredMcpNotificationParams()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpResourceRequestParams.mcpResourceRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .uri(URI.create("file:///original")),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceTemplate.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpResourceTemplate.mcpResourceTemplate()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .title(Optional.of("original"))
                    .uriTemplate("original"),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceTemplate.class,
            "annotations",
            McpAnnotations.class,
            "annotations",
            () ->
                McpResourceTemplate.mcpResourceTemplate()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .title(Optional.of("original"))
                    .uriTemplate("original"),
            (McpAnnotations.Builder child) ->
                child
                    .audience(Optional.of(List.of(McpRole.ASSISTANT)))
                    .lastModified(Optional.of("configured"))
                    .priority(Optional.of(0.75D)),
            Optional.of(configuredMcpAnnotations()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceTemplate.class,
            "icons",
            McpIcon.class,
            "icon",
            () ->
                McpResourceTemplate.mcpResourceTemplate()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .description(Optional.of("original"))
                    .icons(Optional.of(List.of(originalMcpIcon())))
                    .mimeType(Optional.of("original"))
                    .name("original")
                    .title(Optional.of("original"))
                    .uriTemplate("original"),
            (McpIcon.Builder child) ->
                child
                    .mimeType(Optional.of("configured"))
                    .sizes(Optional.of(List.of("configured")))
                    .src(URI.create("file:///configured"))
                    .theme(Optional.of("dark")),
            Optional.of(List.of(originalMcpIcon(), configuredMcpIcon())),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceUpdatedNotification.class,
            "params",
            McpResourceUpdatedNotificationParams.class,
            "params",
            () ->
                McpResourceUpdatedNotification.mcpResourceUpdatedNotification()
                    .params(originalMcpResourceUpdatedNotificationParams()),
            (McpResourceUpdatedNotificationParams.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpNotificationMetaObject()))
                    .uri(URI.create("file:///configured")),
            configuredMcpResourceUpdatedNotificationParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResourceUpdatedNotificationParams.class,
            "meta",
            McpNotificationMetaObject.class,
            "meta",
            () ->
                McpResourceUpdatedNotificationParams.mcpResourceUpdatedNotificationParams()
                    .meta(Optional.of(originalMcpNotificationMetaObject()))
                    .uri(URI.create("file:///original")),
            (McpNotificationMetaObject.Builder child) ->
                child.subscriptionId(Optional.of("configured")),
            Optional.of(configuredMcpNotificationMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResult.class,
            "meta",
            McpResultMetaObject.class,
            "meta",
            () ->
                McpResult.mcpResult()
                    .meta(Optional.of(originalMcpResultMetaObject()))
                    .resultType("complete"),
            (McpResultMetaObject.Builder child) ->
                child.serverInfo(Optional.of(configuredMcpImplementation())),
            Optional.of(configuredMcpResultMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpResultMetaObject.class,
            "serverInfo",
            McpImplementation.class,
            "serverInfo",
            () ->
                McpResultMetaObject.mcpResultMetaObject()
                    .serverInfo(Optional.of(originalMcpImplementation())),
            (McpImplementation.Builder child) ->
                child
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .name("configured")
                    .title(Optional.of("configured"))
                    .version("configured")
                    .websiteUrl(Optional.of(URI.create("file:///configured"))),
            Optional.of(configuredMcpImplementation()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpRoot.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpRoot.mcpRoot()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .name(Optional.of("original"))
                    .uri(URI.create("file:///original")),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSamplingMessage.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpSamplingMessage.mcpSamplingMessage()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content("original")
                    .role(McpRole.USER),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsAcknowledgedNotification.class,
            "params",
            McpSubscriptionsAcknowledgedNotificationParams.class,
            "params",
            () ->
                McpSubscriptionsAcknowledgedNotification.mcpSubscriptionsAcknowledgedNotification()
                    .params(originalMcpSubscriptionsAcknowledgedNotificationParams()),
            (McpSubscriptionsAcknowledgedNotificationParams.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpNotificationMetaObject()))
                    .notifications(configuredMcpSubscriptionFilter()),
            configuredMcpSubscriptionsAcknowledgedNotificationParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsAcknowledgedNotificationParams.class,
            "meta",
            McpNotificationMetaObject.class,
            "meta",
            () ->
                McpSubscriptionsAcknowledgedNotificationParams
                    .mcpSubscriptionsAcknowledgedNotificationParams()
                    .meta(Optional.of(originalMcpNotificationMetaObject()))
                    .notifications(originalMcpSubscriptionFilter()),
            (McpNotificationMetaObject.Builder child) ->
                child.subscriptionId(Optional.of("configured")),
            Optional.of(configuredMcpNotificationMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsAcknowledgedNotificationParams.class,
            "notifications",
            McpSubscriptionFilter.class,
            "notifications",
            () ->
                McpSubscriptionsAcknowledgedNotificationParams
                    .mcpSubscriptionsAcknowledgedNotificationParams()
                    .meta(Optional.of(originalMcpNotificationMetaObject()))
                    .notifications(originalMcpSubscriptionFilter()),
            (McpSubscriptionFilter.Builder child) ->
                child
                    .promptsListChanged(Optional.of(true))
                    .resourceSubscriptions(Optional.of(List.of("configured")))
                    .resourcesListChanged(Optional.of(true))
                    .toolsListChanged(Optional.of(true)),
            configuredMcpSubscriptionFilter(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsListenRequest.class,
            "params",
            McpSubscriptionsListenRequestParams.class,
            "params",
            () ->
                McpSubscriptionsListenRequest.mcpSubscriptionsListenRequest()
                    .id("original")
                    .params(originalMcpSubscriptionsListenRequestParams()),
            (McpSubscriptionsListenRequestParams.Builder child) ->
                child
                    .meta(configuredMcpRequestMetaObject())
                    .notifications(configuredMcpSubscriptionFilter()),
            configuredMcpSubscriptionsListenRequestParams(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsListenRequestParams.class,
            "meta",
            McpRequestMetaObject.class,
            "meta",
            () ->
                McpSubscriptionsListenRequestParams.mcpSubscriptionsListenRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .notifications(originalMcpSubscriptionFilter()),
            (McpRequestMetaObject.Builder child) ->
                child
                    .clientCapabilities(configuredMcpClientCapabilities())
                    .clientInfo(Optional.of(configuredMcpImplementation()))
                    .logLevel(Optional.of(McpLoggingLevel.WARNING))
                    .protocolVersion("2026-07-28")
                    .progressToken(Optional.of("configured"))
                    .extensions(Map.of("existing", "configured")),
            configuredMcpRequestMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsListenRequestParams.class,
            "notifications",
            McpSubscriptionFilter.class,
            "notifications",
            () ->
                McpSubscriptionsListenRequestParams.mcpSubscriptionsListenRequestParams()
                    .meta(originalMcpRequestMetaObject())
                    .notifications(originalMcpSubscriptionFilter()),
            (McpSubscriptionFilter.Builder child) ->
                child
                    .promptsListChanged(Optional.of(true))
                    .resourceSubscriptions(Optional.of(List.of("configured")))
                    .resourcesListChanged(Optional.of(true))
                    .toolsListChanged(Optional.of(true)),
            configuredMcpSubscriptionFilter(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsListenResult.class,
            "meta",
            McpSubscriptionsListenResultMetaObject.class,
            "meta",
            () ->
                McpSubscriptionsListenResult.mcpSubscriptionsListenResult()
                    .meta(originalMcpSubscriptionsListenResultMetaObject())
                    .resultType("complete"),
            (McpSubscriptionsListenResultMetaObject.Builder child) ->
                child
                    .serverInfo(Optional.of(configuredMcpImplementation()))
                    .subscriptionId("configured"),
            configuredMcpSubscriptionsListenResultMetaObject(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsListenResultMetaObject.class,
            "serverInfo",
            McpImplementation.class,
            "serverInfo",
            () ->
                McpSubscriptionsListenResultMetaObject.mcpSubscriptionsListenResultMetaObject()
                    .serverInfo(Optional.of(originalMcpImplementation()))
                    .subscriptionId("original"),
            (McpImplementation.Builder child) ->
                child
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .name("configured")
                    .title(Optional.of("configured"))
                    .version("configured")
                    .websiteUrl(Optional.of(URI.create("file:///configured"))),
            Optional.of(configuredMcpImplementation()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpSubscriptionsListenResultResponse.class,
            "result",
            McpSubscriptionsListenResult.class,
            "result",
            () ->
                McpSubscriptionsListenResultResponse.mcpSubscriptionsListenResultResponse()
                    .id("original")
                    .result(originalMcpSubscriptionsListenResult()),
            (McpSubscriptionsListenResult.Builder child) ->
                child
                    .meta(configuredMcpSubscriptionsListenResultMetaObject())
                    .resultType("complete"),
            configuredMcpSubscriptionsListenResult(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpTextContent.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpTextContent.mcpTextContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .text("original"),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpTextContent.class,
            "annotations",
            McpAnnotations.class,
            "annotations",
            () ->
                McpTextContent.mcpTextContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .annotations(Optional.of(originalMcpAnnotations()))
                    .text("original"),
            (McpAnnotations.Builder child) ->
                child
                    .audience(Optional.of(List.of(McpRole.ASSISTANT)))
                    .lastModified(Optional.of("configured"))
                    .priority(Optional.of(0.75D)),
            Optional.of(configuredMcpAnnotations()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpTextResourceContents.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpTextResourceContents.mcpTextResourceContents()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .mimeType(Optional.of("original"))
                    .text("original")
                    .uri(URI.create("file:///original")),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolListChangedNotification.class,
            "params",
            McpNotificationParams.class,
            "params",
            () ->
                McpToolListChangedNotification.mcpToolListChangedNotification()
                    .params(Optional.of(originalMcpNotificationParams())),
            (McpNotificationParams.Builder child) ->
                child.meta(Optional.of(configuredMcpNotificationMetaObject())),
            Optional.of(configuredMcpNotificationParams()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolResultContent.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpToolResultContent.mcpToolResultContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content(List.of(originalMcpAudioContent()))
                    .isError(Optional.of(false))
                    .structuredContent(Optional.of("original"))
                    .toolUseId("original"),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolResultContent.class,
            "content",
            McpAudioContent.class,
            "audioContent",
            () ->
                McpToolResultContent.mcpToolResultContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content(List.of(originalMcpAudioContent()))
                    .isError(Optional.of(false))
                    .structuredContent(Optional.of("original"))
                    .toolUseId("original"),
            (McpAudioContent.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .data("configured")
                    .mimeType("configured"),
            List.of(originalMcpAudioContent(), configuredMcpAudioContent()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolResultContent.class,
            "content",
            McpEmbeddedResource.class,
            "embeddedResource",
            () ->
                McpToolResultContent.mcpToolResultContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content(List.of(originalMcpAudioContent()))
                    .isError(Optional.of(false))
                    .structuredContent(Optional.of("original"))
                    .toolUseId("original"),
            (McpEmbeddedResource.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .resource("configured"),
            List.of(originalMcpAudioContent(), configuredMcpEmbeddedResource()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolResultContent.class,
            "content",
            McpImageContent.class,
            "imageContent",
            () ->
                McpToolResultContent.mcpToolResultContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content(List.of(originalMcpAudioContent()))
                    .isError(Optional.of(false))
                    .structuredContent(Optional.of("original"))
                    .toolUseId("original"),
            (McpImageContent.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .data("configured")
                    .mimeType("configured"),
            List.of(originalMcpAudioContent(), configuredMcpImageContent()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolResultContent.class,
            "content",
            McpResourceLink.class,
            "resourceLink",
            () ->
                McpToolResultContent.mcpToolResultContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content(List.of(originalMcpAudioContent()))
                    .isError(Optional.of(false))
                    .structuredContent(Optional.of("original"))
                    .toolUseId("original"),
            (McpResourceLink.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .description(Optional.of("configured"))
                    .icons(Optional.of(List.of(configuredMcpIcon())))
                    .mimeType(Optional.of("configured"))
                    .name("configured")
                    .size(Optional.of(2L))
                    .title(Optional.of("configured"))
                    .uri(URI.create("file:///configured")),
            List.of(originalMcpAudioContent(), configuredMcpResourceLink()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolResultContent.class,
            "content",
            McpTextContent.class,
            "textContent",
            () ->
                McpToolResultContent.mcpToolResultContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .content(List.of(originalMcpAudioContent()))
                    .isError(Optional.of(false))
                    .structuredContent(Optional.of("original"))
                    .toolUseId("original"),
            (McpTextContent.Builder child) ->
                child
                    .meta(Optional.of(configuredMcpMetaObject()))
                    .annotations(Optional.of(configuredMcpAnnotations()))
                    .text("configured"),
            List.of(originalMcpAudioContent(), configuredMcpTextContent()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpToolUseContent.class,
            "meta",
            McpMetaObject.class,
            "meta",
            () ->
                McpToolUseContent.mcpToolUseContent()
                    .meta(Optional.of(originalMcpMetaObject()))
                    .id("original")
                    .input(Map.of("existing", "original"))
                    .name("original"),
            (McpMetaObject.Builder child) -> child.values(Map.of("existing", "configured")),
            Optional.of(configuredMcpMetaObject()),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpUnsupportedProtocolVersionError.Error.class,
            "data",
            McpUnsupportedProtocolVersionError.Data.class,
            "data",
            () ->
                McpUnsupportedProtocolVersionError.Error.mcpUnsupportedProtocolVersionErrorError()
                    .message("original")
                    .data(originalMcpUnsupportedProtocolVersionErrorData()),
            (McpUnsupportedProtocolVersionError.Data.Builder child) ->
                child.requested("configured").supported(List.of("configured")),
            configuredMcpUnsupportedProtocolVersionErrorData(),
            List.of()),
        new PrimitiveBuilderCase<>(
            McpUnsupportedProtocolVersionError.class,
            "error",
            McpUnsupportedProtocolVersionError.Error.class,
            "error",
            () ->
                McpUnsupportedProtocolVersionError.mcpUnsupportedProtocolVersionError()
                    .error(originalMcpUnsupportedProtocolVersionErrorError())
                    .id(Optional.of("original")),
            (McpUnsupportedProtocolVersionError.Error.Builder child) ->
                child
                    .message("configured")
                    .data(configuredMcpUnsupportedProtocolVersionErrorData()),
            configuredMcpUnsupportedProtocolVersionErrorError(),
            List.of()));
  }

  private static McpError configuredMcpError() {
    return new McpError(2L, Optional.of("configured"), "configured");
  }

  private static McpError originalMcpError() {
    return new McpError(1L, Optional.of("original"), "original");
  }

  private static McpMetaObject configuredMcpMetaObject() {
    return new McpMetaObject(Map.of("existing", "configured"));
  }

  private static McpMetaObject originalMcpMetaObject() {
    return new McpMetaObject(Map.of("existing", "original"));
  }

  private static McpAnnotations originalMcpAnnotations() {
    return new McpAnnotations(
        Optional.of(List.of(McpRole.USER)), Optional.of("original"), Optional.of(0.25D));
  }

  private static McpAnnotations configuredMcpAnnotations() {
    return new McpAnnotations(
        Optional.of(List.of(McpRole.ASSISTANT)), Optional.of("configured"), Optional.of(0.75D));
  }

  private static McpResultMetaObject configuredMcpResultMetaObject() {
    return new McpResultMetaObject(Optional.of(configuredMcpImplementation()));
  }

  private static McpImplementation configuredMcpImplementation() {
    return new McpImplementation(
        Optional.of("configured"),
        Optional.of(List.of(configuredMcpIcon())),
        "configured",
        Optional.of("configured"),
        "configured",
        Optional.of(URI.create("file:///configured")));
  }

  private static McpIcon configuredMcpIcon() {
    return new McpIcon(
        Optional.of("configured"),
        Optional.of(List.of("configured")),
        URI.create("file:///configured"),
        Optional.of("dark"));
  }

  private static McpResultMetaObject originalMcpResultMetaObject() {
    return new McpResultMetaObject(Optional.of(originalMcpImplementation()));
  }

  private static McpImplementation originalMcpImplementation() {
    return new McpImplementation(
        Optional.of("original"),
        Optional.of(List.of(originalMcpIcon())),
        "original",
        Optional.of("original"),
        "original",
        Optional.of(URI.create("file:///original")));
  }

  private static McpIcon originalMcpIcon() {
    return new McpIcon(
        Optional.of("original"),
        Optional.of(List.of("original")),
        URI.create("file:///original"),
        Optional.of("light"));
  }

  private static McpCallToolRequestParams configuredMcpCallToolRequestParams() {
    return new McpCallToolRequestParams(
        configuredMcpRequestMetaObject(),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(configuredMcpInputResponses()),
        "configured",
        Optional.of("configured"));
  }

  private static McpRequestMetaObject configuredMcpRequestMetaObject() {
    return new McpRequestMetaObject(
        configuredMcpClientCapabilities(),
        Optional.of(configuredMcpImplementation()),
        Optional.of(McpLoggingLevel.WARNING),
        "2026-07-28",
        Optional.of("configured"),
        Map.of("existing", "configured"));
  }

  private static McpClientCapabilities configuredMcpClientCapabilities() {
    return new McpClientCapabilities(
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")));
  }

  private static McpInputResponses configuredMcpInputResponses() {
    return new McpInputResponses(Map.of("existing", configuredMcpCreateMessageResult()));
  }

  private static McpCreateMessageResult configuredMcpCreateMessageResult() {
    return new McpCreateMessageResult(
        Optional.of(configuredMcpMetaObject()),
        "configured",
        "configured",
        McpRole.ASSISTANT,
        Optional.of("configured"));
  }

  private static McpCallToolRequestParams originalMcpCallToolRequestParams() {
    return new McpCallToolRequestParams(
        originalMcpRequestMetaObject(),
        Optional.of(Map.of("existing", "original")),
        Optional.of(originalMcpInputResponses()),
        "original",
        Optional.of("original"));
  }

  private static McpRequestMetaObject originalMcpRequestMetaObject() {
    return new McpRequestMetaObject(
        originalMcpClientCapabilities(),
        Optional.of(originalMcpImplementation()),
        Optional.of(McpLoggingLevel.INFO),
        "2026-07-28",
        Optional.of("original"),
        Map.of("existing", "original"));
  }

  private static McpClientCapabilities originalMcpClientCapabilities() {
    return new McpClientCapabilities(
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")));
  }

  private static McpInputResponses originalMcpInputResponses() {
    return new McpInputResponses(Map.of("existing", originalMcpCreateMessageResult()));
  }

  private static McpCreateMessageResult originalMcpCreateMessageResult() {
    return new McpCreateMessageResult(
        Optional.of(originalMcpMetaObject()),
        "original",
        "original",
        McpRole.USER,
        Optional.of("original"));
  }

  private static McpCallToolResult configuredMcpCallToolResult() {
    return new McpCallToolResult(
        Optional.of(configuredMcpResultMetaObject()),
        List.of(configuredMcpAudioContent()),
        Optional.of(true),
        "complete",
        Optional.of("configured"));
  }

  private static McpAudioContent configuredMcpAudioContent() {
    return new McpAudioContent(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(configuredMcpAnnotations()),
        "configured",
        "configured");
  }

  private static McpCallToolResult originalMcpCallToolResult() {
    return new McpCallToolResult(
        Optional.of(originalMcpResultMetaObject()),
        List.of(originalMcpAudioContent()),
        Optional.of(false),
        "complete",
        Optional.of("original"));
  }

  private static McpAudioContent originalMcpAudioContent() {
    return new McpAudioContent(
        Optional.of(originalMcpMetaObject()),
        Optional.of(originalMcpAnnotations()),
        "original",
        "original");
  }

  private static McpInputRequiredResult configuredMcpInputRequiredResult() {
    return new McpInputRequiredResult(
        Optional.of(configuredMcpResultMetaObject()),
        Optional.of(configuredMcpInputRequests()),
        Optional.of("configured"),
        "complete");
  }

  private static McpInputRequests configuredMcpInputRequests() {
    return new McpInputRequests(Map.of("existing", configuredMcpCreateMessageRequest()));
  }

  private static McpCreateMessageRequest configuredMcpCreateMessageRequest() {
    return new McpCreateMessageRequest(configuredMcpCreateMessageRequestParams());
  }

  private static McpCreateMessageRequestParams configuredMcpCreateMessageRequestParams() {
    return new McpCreateMessageRequestParams(
        Optional.of("configured"),
        2L,
        List.of(configuredMcpSamplingMessage()),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(configuredMcpModelPreferences()),
        Optional.of(List.of("configured")),
        Optional.of("configured"),
        Optional.of(0.75D),
        Optional.of(configuredMcpToolChoice()),
        Optional.of(List.of(Map.of("existing", "configured"))));
  }

  private static McpSamplingMessage configuredMcpSamplingMessage() {
    return new McpSamplingMessage(
        Optional.of(configuredMcpMetaObject()), "configured", McpRole.ASSISTANT);
  }

  private static McpModelPreferences configuredMcpModelPreferences() {
    return new McpModelPreferences(
        Optional.of(0.75D),
        Optional.of(List.of(configuredMcpModelHint())),
        Optional.of(0.75D),
        Optional.of(0.75D));
  }

  private static McpModelHint configuredMcpModelHint() {
    return new McpModelHint(Optional.of("configured"));
  }

  private static McpToolChoice configuredMcpToolChoice() {
    return new McpToolChoice(Optional.of("configured"));
  }

  private static McpCancelledNotificationParams configuredMcpCancelledNotificationParams() {
    return new McpCancelledNotificationParams(
        Optional.of(configuredMcpNotificationMetaObject()),
        Optional.of("configured"),
        "configured");
  }

  private static McpNotificationMetaObject configuredMcpNotificationMetaObject() {
    return new McpNotificationMetaObject(Optional.of("configured"));
  }

  private static McpCancelledNotificationParams originalMcpCancelledNotificationParams() {
    return new McpCancelledNotificationParams(
        Optional.of(originalMcpNotificationMetaObject()), Optional.of("original"), "original");
  }

  private static McpNotificationMetaObject originalMcpNotificationMetaObject() {
    return new McpNotificationMetaObject(Optional.of("original"));
  }

  private static McpCompleteRequestParams configuredMcpCompleteRequestParams() {
    return new McpCompleteRequestParams(
        configuredMcpRequestMetaObject(),
        Map.of("existing", "configured"),
        Optional.of(Map.of("existing", "configured")),
        "configured");
  }

  private static McpCompleteRequestParams originalMcpCompleteRequestParams() {
    return new McpCompleteRequestParams(
        originalMcpRequestMetaObject(),
        Map.of("existing", "original"),
        Optional.of(Map.of("existing", "original")),
        "original");
  }

  private static McpCompleteResult configuredMcpCompleteResult() {
    return new McpCompleteResult(
        Optional.of(configuredMcpResultMetaObject()), Map.of("existing", "configured"), "complete");
  }

  private static McpCompleteResult originalMcpCompleteResult() {
    return new McpCompleteResult(
        Optional.of(originalMcpResultMetaObject()), Map.of("existing", "original"), "complete");
  }

  private static McpCreateMessageRequestParams originalMcpCreateMessageRequestParams() {
    return new McpCreateMessageRequestParams(
        Optional.of("original"),
        1L,
        List.of(originalMcpSamplingMessage()),
        Optional.of(Map.of("existing", "original")),
        Optional.of(originalMcpModelPreferences()),
        Optional.of(List.of("original")),
        Optional.of("original"),
        Optional.of(0.25D),
        Optional.of(originalMcpToolChoice()),
        Optional.of(List.of(Map.of("existing", "original"))));
  }

  private static McpSamplingMessage originalMcpSamplingMessage() {
    return new McpSamplingMessage(Optional.of(originalMcpMetaObject()), "original", McpRole.USER);
  }

  private static McpModelPreferences originalMcpModelPreferences() {
    return new McpModelPreferences(
        Optional.of(0.25D),
        Optional.of(List.of(originalMcpModelHint())),
        Optional.of(0.25D),
        Optional.of(0.25D));
  }

  private static McpModelHint originalMcpModelHint() {
    return new McpModelHint(Optional.of("original"));
  }

  private static McpToolChoice originalMcpToolChoice() {
    return new McpToolChoice(Optional.of("original"));
  }

  private static McpRequestParams configuredMcpRequestParams() {
    return new McpRequestParams(configuredMcpRequestMetaObject());
  }

  private static McpRequestParams originalMcpRequestParams() {
    return new McpRequestParams(originalMcpRequestMetaObject());
  }

  private static McpServerCapabilities originalMcpServerCapabilities() {
    return new McpServerCapabilities(
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")),
        Optional.of(Map.of("existing", "original")));
  }

  private static McpServerCapabilities configuredMcpServerCapabilities() {
    return new McpServerCapabilities(
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(Map.of("existing", "configured")));
  }

  private static McpDiscoverResult configuredMcpDiscoverResult() {
    return new McpDiscoverResult(
        Optional.of(configuredMcpResultMetaObject()),
        "public",
        configuredMcpServerCapabilities(),
        Optional.of("configured"),
        "complete",
        List.of("configured"),
        2L);
  }

  private static McpDiscoverResult originalMcpDiscoverResult() {
    return new McpDiscoverResult(
        Optional.of(originalMcpResultMetaObject()),
        "private",
        originalMcpServerCapabilities(),
        Optional.of("original"),
        "complete",
        List.of("original"),
        1L);
  }

  private static McpElicitRequestFormParams configuredMcpElicitRequestFormParams() {
    return new McpElicitRequestFormParams("configured", Map.of("existing", "configured"));
  }

  private static McpElicitRequestFormParams originalMcpElicitRequestFormParams() {
    return new McpElicitRequestFormParams("original", Map.of("existing", "original"));
  }

  private static McpElicitRequestURLParams configuredMcpElicitRequestURLParams() {
    return new McpElicitRequestURLParams("configured", URI.create("file:///configured"));
  }

  private static McpGetPromptRequestParams configuredMcpGetPromptRequestParams() {
    return new McpGetPromptRequestParams(
        configuredMcpRequestMetaObject(),
        Optional.of(Map.of("existing", "configured")),
        Optional.of(configuredMcpInputResponses()),
        "configured",
        Optional.of("configured"));
  }

  private static McpGetPromptRequestParams originalMcpGetPromptRequestParams() {
    return new McpGetPromptRequestParams(
        originalMcpRequestMetaObject(),
        Optional.of(Map.of("existing", "original")),
        Optional.of(originalMcpInputResponses()),
        "original",
        Optional.of("original"));
  }

  private static McpPromptMessage originalMcpPromptMessage() {
    return new McpPromptMessage(originalMcpAudioContent(), McpRole.USER);
  }

  private static McpPromptMessage configuredMcpPromptMessage() {
    return new McpPromptMessage(configuredMcpAudioContent(), McpRole.ASSISTANT);
  }

  private static McpGetPromptResult configuredMcpGetPromptResult() {
    return new McpGetPromptResult(
        Optional.of(configuredMcpResultMetaObject()),
        Optional.of("configured"),
        List.of(configuredMcpPromptMessage()),
        "complete");
  }

  private static McpGetPromptResult originalMcpGetPromptResult() {
    return new McpGetPromptResult(
        Optional.of(originalMcpResultMetaObject()),
        Optional.of("original"),
        List.of(originalMcpPromptMessage()),
        "complete");
  }

  private static McpHeaderMismatchError.Error configuredMcpHeaderMismatchErrorError() {
    return new McpHeaderMismatchError.Error("configured", Optional.of("configured"));
  }

  private static McpHeaderMismatchError.Error originalMcpHeaderMismatchErrorError() {
    return new McpHeaderMismatchError.Error("original", Optional.of("original"));
  }

  private static McpCreateMessageRequest originalMcpCreateMessageRequest() {
    return new McpCreateMessageRequest(originalMcpCreateMessageRequestParams());
  }

  private static McpElicitRequest configuredMcpElicitRequest() {
    return new McpElicitRequest(configuredMcpElicitRequestFormParams());
  }

  private static McpListRootsRequest configuredMcpListRootsRequest() {
    return new McpListRootsRequest(Optional.of(Map.of("existing", "configured")));
  }

  private static McpInputRequests originalMcpInputRequests() {
    return new McpInputRequests(Map.of("existing", originalMcpCreateMessageRequest()));
  }

  private static McpElicitResult configuredMcpElicitResult() {
    return new McpElicitResult("configured", Optional.of(Map.of("existing", "configured")));
  }

  private static McpListRootsResult configuredMcpListRootsResult() {
    return new McpListRootsResult(List.of(configuredMcpRoot()));
  }

  private static McpRoot configuredMcpRoot() {
    return new McpRoot(
        Optional.of(configuredMcpMetaObject()),
        Optional.of("configured"),
        URI.create("file:///configured"));
  }

  private static McpPaginatedRequestParams configuredMcpPaginatedRequestParams() {
    return new McpPaginatedRequestParams(
        configuredMcpRequestMetaObject(), Optional.of("configured"));
  }

  private static McpPaginatedRequestParams originalMcpPaginatedRequestParams() {
    return new McpPaginatedRequestParams(originalMcpRequestMetaObject(), Optional.of("original"));
  }

  private static McpPrompt originalMcpPrompt() {
    return new McpPrompt(
        Optional.of(originalMcpMetaObject()),
        Optional.of(List.of(originalMcpPromptArgument())),
        Optional.of("original"),
        Optional.of(List.of(originalMcpIcon())),
        "original",
        Optional.of("original"));
  }

  private static McpPromptArgument originalMcpPromptArgument() {
    return new McpPromptArgument(
        Optional.of("original"), "original", Optional.of(false), Optional.of("original"));
  }

  private static McpPrompt configuredMcpPrompt() {
    return new McpPrompt(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(List.of(configuredMcpPromptArgument())),
        Optional.of("configured"),
        Optional.of(List.of(configuredMcpIcon())),
        "configured",
        Optional.of("configured"));
  }

  private static McpPromptArgument configuredMcpPromptArgument() {
    return new McpPromptArgument(
        Optional.of("configured"), "configured", Optional.of(true), Optional.of("configured"));
  }

  private static McpListPromptsResult configuredMcpListPromptsResult() {
    return new McpListPromptsResult(
        Optional.of(configuredMcpResultMetaObject()),
        "public",
        Optional.of("configured"),
        List.of(configuredMcpPrompt()),
        "complete",
        2L);
  }

  private static McpListPromptsResult originalMcpListPromptsResult() {
    return new McpListPromptsResult(
        Optional.of(originalMcpResultMetaObject()),
        "private",
        Optional.of("original"),
        List.of(originalMcpPrompt()),
        "complete",
        1L);
  }

  private static McpResourceTemplate originalMcpResourceTemplate() {
    return new McpResourceTemplate(
        Optional.of(originalMcpMetaObject()),
        Optional.of(originalMcpAnnotations()),
        Optional.of("original"),
        Optional.of(List.of(originalMcpIcon())),
        Optional.of("original"),
        "original",
        Optional.of("original"),
        "original");
  }

  private static McpResourceTemplate configuredMcpResourceTemplate() {
    return new McpResourceTemplate(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(configuredMcpAnnotations()),
        Optional.of("configured"),
        Optional.of(List.of(configuredMcpIcon())),
        Optional.of("configured"),
        "configured",
        Optional.of("configured"),
        "configured");
  }

  private static McpListResourceTemplatesResult configuredMcpListResourceTemplatesResult() {
    return new McpListResourceTemplatesResult(
        Optional.of(configuredMcpResultMetaObject()),
        "public",
        Optional.of("configured"),
        List.of(configuredMcpResourceTemplate()),
        "complete",
        2L);
  }

  private static McpListResourceTemplatesResult originalMcpListResourceTemplatesResult() {
    return new McpListResourceTemplatesResult(
        Optional.of(originalMcpResultMetaObject()),
        "private",
        Optional.of("original"),
        List.of(originalMcpResourceTemplate()),
        "complete",
        1L);
  }

  private static McpResource originalMcpResource() {
    return new McpResource(
        Optional.of(originalMcpMetaObject()),
        Optional.of(originalMcpAnnotations()),
        Optional.of("original"),
        Optional.of(List.of(originalMcpIcon())),
        Optional.of("original"),
        "original",
        Optional.of(1L),
        Optional.of("original"),
        URI.create("file:///original"));
  }

  private static McpResource configuredMcpResource() {
    return new McpResource(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(configuredMcpAnnotations()),
        Optional.of("configured"),
        Optional.of(List.of(configuredMcpIcon())),
        Optional.of("configured"),
        "configured",
        Optional.of(2L),
        Optional.of("configured"),
        URI.create("file:///configured"));
  }

  private static McpListResourcesResult configuredMcpListResourcesResult() {
    return new McpListResourcesResult(
        Optional.of(configuredMcpResultMetaObject()),
        "public",
        Optional.of("configured"),
        List.of(configuredMcpResource()),
        "complete",
        2L);
  }

  private static McpListResourcesResult originalMcpListResourcesResult() {
    return new McpListResourcesResult(
        Optional.of(originalMcpResultMetaObject()),
        "private",
        Optional.of("original"),
        List.of(originalMcpResource()),
        "complete",
        1L);
  }

  private static McpRoot originalMcpRoot() {
    return new McpRoot(
        Optional.of(originalMcpMetaObject()),
        Optional.of("original"),
        URI.create("file:///original"));
  }

  private static McpListToolsResult configuredMcpListToolsResult() {
    return new McpListToolsResult(
        Optional.of(configuredMcpResultMetaObject()),
        "public",
        Optional.of("configured"),
        "complete",
        List.of(Map.of("existing", "configured")),
        2L);
  }

  private static McpListToolsResult originalMcpListToolsResult() {
    return new McpListToolsResult(
        Optional.of(originalMcpResultMetaObject()),
        "private",
        Optional.of("original"),
        "complete",
        List.of(Map.of("existing", "original")),
        1L);
  }

  private static McpLoggingMessageNotificationParams
      configuredMcpLoggingMessageNotificationParams() {
    return new McpLoggingMessageNotificationParams(
        Optional.of(configuredMcpNotificationMetaObject()),
        "configured",
        McpLoggingLevel.WARNING,
        Optional.of("configured"));
  }

  private static McpLoggingMessageNotificationParams originalMcpLoggingMessageNotificationParams() {
    return new McpLoggingMessageNotificationParams(
        Optional.of(originalMcpNotificationMetaObject()),
        "original",
        McpLoggingLevel.INFO,
        Optional.of("original"));
  }

  private static McpMissingRequiredClientCapabilityError.Data
      configuredMcpMissingRequiredClientCapabilityErrorData() {
    return new McpMissingRequiredClientCapabilityError.Data(configuredMcpClientCapabilities());
  }

  private static McpMissingRequiredClientCapabilityError.Data
      originalMcpMissingRequiredClientCapabilityErrorData() {
    return new McpMissingRequiredClientCapabilityError.Data(originalMcpClientCapabilities());
  }

  private static McpMissingRequiredClientCapabilityError.Error
      configuredMcpMissingRequiredClientCapabilityErrorError() {
    return new McpMissingRequiredClientCapabilityError.Error(
        "configured", configuredMcpMissingRequiredClientCapabilityErrorData());
  }

  private static McpMissingRequiredClientCapabilityError.Error
      originalMcpMissingRequiredClientCapabilityErrorError() {
    return new McpMissingRequiredClientCapabilityError.Error(
        "original", originalMcpMissingRequiredClientCapabilityErrorData());
  }

  private static McpProgressNotificationParams configuredMcpProgressNotificationParams() {
    return new McpProgressNotificationParams(
        Optional.of(configuredMcpNotificationMetaObject()),
        Optional.of("configured"),
        0.75D,
        "configured",
        Optional.of(0.75D));
  }

  private static McpProgressNotificationParams originalMcpProgressNotificationParams() {
    return new McpProgressNotificationParams(
        Optional.of(originalMcpNotificationMetaObject()),
        Optional.of("original"),
        0.25D,
        "original",
        Optional.of(0.25D));
  }

  private static McpNotificationParams configuredMcpNotificationParams() {
    return new McpNotificationParams(Optional.of(configuredMcpNotificationMetaObject()));
  }

  private static McpNotificationParams originalMcpNotificationParams() {
    return new McpNotificationParams(Optional.of(originalMcpNotificationMetaObject()));
  }

  private static McpEmbeddedResource configuredMcpEmbeddedResource() {
    return new McpEmbeddedResource(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(configuredMcpAnnotations()),
        "configured");
  }

  private static McpImageContent configuredMcpImageContent() {
    return new McpImageContent(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(configuredMcpAnnotations()),
        "configured",
        "configured");
  }

  private static McpResourceLink configuredMcpResourceLink() {
    return new McpResourceLink(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(configuredMcpAnnotations()),
        Optional.of("configured"),
        Optional.of(List.of(configuredMcpIcon())),
        Optional.of("configured"),
        "configured",
        Optional.of(2L),
        Optional.of("configured"),
        URI.create("file:///configured"));
  }

  private static McpTextContent configuredMcpTextContent() {
    return new McpTextContent(
        Optional.of(configuredMcpMetaObject()),
        Optional.of(configuredMcpAnnotations()),
        "configured");
  }

  private static McpReadResourceRequestParams configuredMcpReadResourceRequestParams() {
    return new McpReadResourceRequestParams(
        configuredMcpRequestMetaObject(),
        Optional.of(configuredMcpInputResponses()),
        Optional.of("configured"),
        URI.create("file:///configured"));
  }

  private static McpReadResourceRequestParams originalMcpReadResourceRequestParams() {
    return new McpReadResourceRequestParams(
        originalMcpRequestMetaObject(),
        Optional.of(originalMcpInputResponses()),
        Optional.of("original"),
        URI.create("file:///original"));
  }

  private static McpBlobResourceContents originalMcpBlobResourceContents() {
    return new McpBlobResourceContents(
        Optional.of(originalMcpMetaObject()),
        "original",
        Optional.of("original"),
        URI.create("file:///original"));
  }

  private static McpBlobResourceContents configuredMcpBlobResourceContents() {
    return new McpBlobResourceContents(
        Optional.of(configuredMcpMetaObject()),
        "configured",
        Optional.of("configured"),
        URI.create("file:///configured"));
  }

  private static McpTextResourceContents configuredMcpTextResourceContents() {
    return new McpTextResourceContents(
        Optional.of(configuredMcpMetaObject()),
        Optional.of("configured"),
        "configured",
        URI.create("file:///configured"));
  }

  private static McpInputRequiredResult originalMcpInputRequiredResult() {
    return new McpInputRequiredResult(
        Optional.of(originalMcpResultMetaObject()),
        Optional.of(originalMcpInputRequests()),
        Optional.of("original"),
        "complete");
  }

  private static McpReadResourceResult configuredMcpReadResourceResult() {
    return new McpReadResourceResult(
        Optional.of(configuredMcpResultMetaObject()),
        "public",
        List.of(configuredMcpBlobResourceContents()),
        "complete",
        2L);
  }

  private static McpResourceUpdatedNotificationParams
      configuredMcpResourceUpdatedNotificationParams() {
    return new McpResourceUpdatedNotificationParams(
        Optional.of(configuredMcpNotificationMetaObject()), URI.create("file:///configured"));
  }

  private static McpResourceUpdatedNotificationParams
      originalMcpResourceUpdatedNotificationParams() {
    return new McpResourceUpdatedNotificationParams(
        Optional.of(originalMcpNotificationMetaObject()), URI.create("file:///original"));
  }

  private static McpSubscriptionsAcknowledgedNotificationParams
      configuredMcpSubscriptionsAcknowledgedNotificationParams() {
    return new McpSubscriptionsAcknowledgedNotificationParams(
        Optional.of(configuredMcpNotificationMetaObject()), configuredMcpSubscriptionFilter());
  }

  private static McpSubscriptionFilter configuredMcpSubscriptionFilter() {
    return new McpSubscriptionFilter(
        Optional.of(true),
        Optional.of(List.of("configured")),
        Optional.of(true),
        Optional.of(true));
  }

  private static McpSubscriptionsAcknowledgedNotificationParams
      originalMcpSubscriptionsAcknowledgedNotificationParams() {
    return new McpSubscriptionsAcknowledgedNotificationParams(
        Optional.of(originalMcpNotificationMetaObject()), originalMcpSubscriptionFilter());
  }

  private static McpSubscriptionFilter originalMcpSubscriptionFilter() {
    return new McpSubscriptionFilter(
        Optional.of(false),
        Optional.of(List.of("original")),
        Optional.of(false),
        Optional.of(false));
  }

  private static McpSubscriptionsListenRequestParams
      configuredMcpSubscriptionsListenRequestParams() {
    return new McpSubscriptionsListenRequestParams(
        configuredMcpRequestMetaObject(), configuredMcpSubscriptionFilter());
  }

  private static McpSubscriptionsListenRequestParams originalMcpSubscriptionsListenRequestParams() {
    return new McpSubscriptionsListenRequestParams(
        originalMcpRequestMetaObject(), originalMcpSubscriptionFilter());
  }

  private static McpSubscriptionsListenResultMetaObject
      configuredMcpSubscriptionsListenResultMetaObject() {
    return new McpSubscriptionsListenResultMetaObject(
        Optional.of(configuredMcpImplementation()), "configured");
  }

  private static McpSubscriptionsListenResultMetaObject
      originalMcpSubscriptionsListenResultMetaObject() {
    return new McpSubscriptionsListenResultMetaObject(
        Optional.of(originalMcpImplementation()), "original");
  }

  private static McpSubscriptionsListenResult configuredMcpSubscriptionsListenResult() {
    return new McpSubscriptionsListenResult(
        configuredMcpSubscriptionsListenResultMetaObject(), "complete");
  }

  private static McpSubscriptionsListenResult originalMcpSubscriptionsListenResult() {
    return new McpSubscriptionsListenResult(
        originalMcpSubscriptionsListenResultMetaObject(), "complete");
  }

  private static McpUnsupportedProtocolVersionError.Data
      configuredMcpUnsupportedProtocolVersionErrorData() {
    return new McpUnsupportedProtocolVersionError.Data("configured", List.of("configured"));
  }

  private static McpUnsupportedProtocolVersionError.Data
      originalMcpUnsupportedProtocolVersionErrorData() {
    return new McpUnsupportedProtocolVersionError.Data("original", List.of("original"));
  }

  private static McpUnsupportedProtocolVersionError.Error
      configuredMcpUnsupportedProtocolVersionErrorError() {
    return new McpUnsupportedProtocolVersionError.Error(
        "configured", configuredMcpUnsupportedProtocolVersionErrorData());
  }

  private static McpUnsupportedProtocolVersionError.Error
      originalMcpUnsupportedProtocolVersionErrorError() {
    return new McpUnsupportedProtocolVersionError.Error(
        "original", originalMcpUnsupportedProtocolVersionErrorData());
  }
}
