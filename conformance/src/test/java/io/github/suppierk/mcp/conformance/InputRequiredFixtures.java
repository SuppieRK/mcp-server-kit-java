package io.github.suppierk.mcp.conformance;

import static io.github.suppierk.mcp.protocol.McpCallToolResult.mcpCallToolResult;
import static io.github.suppierk.mcp.protocol.McpClientCapabilities.mcpClientCapabilities;
import static io.github.suppierk.mcp.protocol.McpGetPromptResult.mcpGetPromptResult;
import static io.github.suppierk.mcp.protocol.McpInputRequiredResult.mcpInputRequiredResult;
import static io.github.suppierk.mcp.protocol.McpPrompt.mcpPrompt;

import io.github.suppierk.mcp.protocol.McpCallToolRequestParams;
import io.github.suppierk.mcp.protocol.McpCallToolResult;
import io.github.suppierk.mcp.protocol.McpCallToolResultResponse;
import io.github.suppierk.mcp.protocol.McpCreateMessageResult;
import io.github.suppierk.mcp.protocol.McpElicitResult;
import io.github.suppierk.mcp.protocol.McpInputRequests;
import io.github.suppierk.mcp.protocol.McpInputResponse;
import io.github.suppierk.mcp.protocol.McpInputResponses;
import io.github.suppierk.mcp.protocol.McpListRootsResult;
import io.github.suppierk.mcp.protocol.McpRole;
import io.github.suppierk.mcp.server.McpEmptyContext;
import io.github.suppierk.mcp.server.McpInvalidParamsException;
import io.github.suppierk.mcp.server.McpMissingRequiredClientCapabilityException;
import io.github.suppierk.mcp.server.McpServerKit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Small application-owned input workflows; the kit only carries the protocol values. */
final class InputRequiredFixtures {
  private final String confirmationState = UUID.randomUUID().toString();
  private final String multipleInputsState = UUID.randomUUID().toString();
  private final String firstRoundState = UUID.randomUUID().toString();
  // Application-owned state: opaque tokens identify saved input, outside the kit.
  private final Map<String, String> pendingNames = new ConcurrentHashMap<>();

  private InputRequiredFixtures() {}

  static McpServerKit.Builder<McpEmptyContext> register(McpServerKit.Builder<McpEmptyContext> kit) {
    var workflow = new InputRequiredFixtures();
    return kit.syncTool(
            tool ->
                tool.name("test_input_required_result_elicitation")
                    .description("Requests a name, then greets that name")
                    .handler((context, request, output) -> elicitation(request)))
        .syncTool(
            tool ->
                tool.name("test_input_required_result_sampling")
                    .description("Requests a sampled answer")
                    .handler((context, request, output) -> sampling(request)))
        .syncTool(
            tool ->
                tool.name("test_input_required_result_list_roots")
                    .description("Requests client roots")
                    .handler((context, request, output) -> roots(request)))
        .syncTool(
            tool ->
                tool.name("test_input_required_result_request_state")
                    .description("Validates echoed state")
                    .handler((context, request, output) -> workflow.confirmation(request)))
        .syncTool(
            tool ->
                tool.name("test_input_required_result_tampered_state")
                    .description("Rejects modified state")
                    .handler((context, request, output) -> workflow.confirmation(request)))
        .syncTool(
            tool ->
                tool.name("test_input_required_result_multiple_inputs")
                    .description("Combines three inputs")
                    .handler((context, request, output) -> workflow.multipleInputs(request)))
        .syncTool(
            tool ->
                tool.name("test_input_required_result_multi_round")
                    .description("Collects a name and color")
                    .handler((context, request, output) -> workflow.multiRound(request)))
        .syncTool(
            tool ->
                tool.name("test_input_required_result_capabilities")
                    .description("Requires only sampling")
                    .handler(
                        (context, request, output) -> {
                          if (request.meta().clientCapabilities().sampling().isEmpty()) {
                            throw new McpMissingRequiredClientCapabilityException(
                                "Sampling is required",
                                mcpClientCapabilities().sampling(Map.of()).build());
                          }
                          return sampling(request);
                        }))
        .syncPrompt(
            mcpPrompt()
                .name("test_input_required_result_prompt")
                .description("Requests context")
                .build(),
            (context, request, output) -> {
              String value = submittedString(request.inputResponses(), "user_context", "context");
              if (value == null) {
                return mcpInputRequiredResult()
                    .resultType("input_required")
                    .inputRequests(
                        inputs -> addElicitation(inputs, "user_context", "context", "string"))
                    .build();
              }
              return mcpGetPromptResult()
                  .resultType("complete")
                  .promptMessage(
                      message -> message.role(McpRole.USER).textContent(text -> text.text(value)))
                  .build();
            });
  }

  private static McpCallToolResultResponse.Result sampling(McpCallToolRequestParams request) {
    String answer = sampledText(request.inputResponses(), "capital_question");
    if (answer != null) {
      return complete(answer);
    }
    return mcpInputRequiredResult()
        .resultType("input_required")
        .inputRequests(
            inputs -> addSampling(inputs, "capital_question", "What is the capital of France?"))
        .build();
  }

  private static McpCallToolResultResponse.Result roots(McpCallToolRequestParams request) {
    if (response(request.inputResponses(), "client_roots") instanceof McpListRootsResult roots) {
      return complete(describeRoots(roots));
    }
    return mcpInputRequiredResult()
        .resultType("input_required")
        .inputRequests(
            inputs -> inputs.listRootsRequest("client_roots", roots -> roots.params(Map.of())))
        .build();
  }

  private McpCallToolResultResponse.Result confirmation(McpCallToolRequestParams request) {
    validateState(request, confirmationState);
    if (request.requestState().isPresent()
        && response(request.inputResponses(), "confirm") instanceof McpElicitResult form
        && "accept".equals(form.action())
        && Boolean.TRUE.equals(form.content().orElse(Map.of()).get("ok"))) {
      return complete("state-ok");
    }
    return mcpInputRequiredResult()
        .resultType("input_required")
        .requestState(confirmationState)
        .inputRequests(inputs -> addElicitation(inputs, "confirm", "ok", "boolean"))
        .build();
  }

  private McpCallToolResultResponse.Result multipleInputs(McpCallToolRequestParams request) {
    validateState(request, multipleInputsState);
    String name = submittedString(request.inputResponses(), "user_name", "name");
    String greeting = sampledText(request.inputResponses(), "greeting");
    if (request.requestState().isPresent()
        && name != null
        && greeting != null
        && response(request.inputResponses(), "client_roots") instanceof McpListRootsResult roots) {
      return complete(greeting + " " + name + " " + describeRoots(roots));
    }
    return mcpInputRequiredResult()
        .resultType("input_required")
        .requestState(multipleInputsState)
        .inputRequests(
            inputs -> {
              addElicitation(inputs, "user_name", "name", "string");
              addSampling(inputs, "greeting", "Give a friendly greeting");
              inputs.listRootsRequest("client_roots", roots -> roots.params(Map.of()));
            })
        .build();
  }

  private McpCallToolResultResponse.Result multiRound(McpCallToolRequestParams request) {
    String state = request.requestState().orElse(firstRoundState);
    if (state.equals(firstRoundState)) {
      String name = submittedString(request.inputResponses(), "step1", "name");
      if (name == null || request.requestState().isEmpty()) {
        return mcpInputRequiredResult()
            .resultType("input_required")
            .requestState(firstRoundState)
            .inputRequests(inputs -> addElicitation(inputs, "step1", "name", "string"))
            .build();
      }
      String next = UUID.randomUUID().toString();
      pendingNames.put(next, name);
      return mcpInputRequiredResult()
          .resultType("input_required")
          .requestState(next)
          .inputRequests(inputs -> addElicitation(inputs, "step2", "color", "string"))
          .build();
    }
    String name = pendingNames.get(state);
    if (name == null) {
      throw new McpInvalidParamsException("Unknown or modified request state", Optional.empty());
    }
    String color = submittedString(request.inputResponses(), "step2", "color");
    if (color == null) {
      return mcpInputRequiredResult()
          .resultType("input_required")
          .requestState(state)
          .inputRequests(inputs -> addElicitation(inputs, "step2", "color", "string"))
          .build();
    }
    pendingNames.remove(state);
    return complete(name + " likes " + color);
  }

  private static void validateState(McpCallToolRequestParams request, String expected) {
    if (request.requestState().isPresent() && !request.requestState().get().equals(expected)) {
      throw new McpInvalidParamsException("Unknown or modified request state", Optional.empty());
    }
  }

  private static McpCallToolResult complete(String message) {
    return mcpCallToolResult()
        .resultType("complete")
        .textContent(text -> text.text(message))
        .build();
  }

  private static String describeRoots(McpListRootsResult roots) {
    return roots.roots().stream()
        .map(root -> root.uri().toString())
        .collect(Collectors.joining(", "));
  }

  private static String sampledText(Optional<McpInputResponses> responses, String key) {
    if (response(responses, key) instanceof McpCreateMessageResult sample
        && sample.content() instanceof Map<?, ?> content
        && content.get("text") instanceof String text) {
      return text;
    }
    return null;
  }

  private static McpInputResponse response(Optional<McpInputResponses> responses, String key) {
    return responses.map(McpInputResponses::values).orElse(Map.of()).get(key);
  }

  private static void addSampling(McpInputRequests.Builder inputs, String key, String message) {
    inputs.createMessageRequest(
        key,
        request ->
            request.params(
                params ->
                    params
                        .maxTokens(100L)
                        .samplingMessage(
                            sample ->
                                sample
                                    .role(McpRole.USER)
                                    .content(Map.of("type", "text", "text", message)))));
  }

  private static McpCallToolResultResponse.Result elicitation(McpCallToolRequestParams request) {
    String name = submittedString(request.inputResponses(), "user_name", "name");
    if (name == null) {
      return mcpInputRequiredResult()
          .resultType("input_required")
          .inputRequests(inputs -> addElicitation(inputs, "user_name", "name", "string"))
          .build();
    }
    return mcpCallToolResult()
        .resultType("complete")
        .textContent(text -> text.text("Hello, " + name + "!"))
        .build();
  }

  private static String submittedString(
      Optional<McpInputResponses> responses, String key, String field) {
    McpInputResponse response = responses.map(McpInputResponses::values).orElse(Map.of()).get(key);
    if (response instanceof McpElicitResult form && "accept".equals(form.action())) {
      Object value = form.content().orElse(Map.of()).get(field);
      if (value instanceof String text) {
        return text;
      }
    }
    return null;
  }

  private static void addElicitation(
      McpInputRequests.Builder inputs, String key, String field, String type) {
    inputs.elicitRequest(
        key,
        request ->
            request.elicitRequestFormParams(
                form ->
                    form.message("Provide " + field)
                        .requestedSchema(
                            Map.of(
                                "type",
                                "object",
                                "properties",
                                Map.of(field, Map.of("type", type)),
                                "required",
                                List.of(field)))));
  }
}
