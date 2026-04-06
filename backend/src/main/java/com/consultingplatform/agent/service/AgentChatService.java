package com.consultingplatform.agent.service;

import com.consultingplatform.agent.domain.ChatConversation;
import com.consultingplatform.agent.domain.ChatMessage;
import com.consultingplatform.agent.repository.ChatConversationRepository;
import com.consultingplatform.agent.repository.ChatMessageRepository;
import com.consultingplatform.agent.web.dto.AgentChatRequest;
import com.consultingplatform.agent.web.dto.AgentChatResponse;
import com.consultingplatform.security.CustomUserDetails;
import com.consultingplatform.user.domain.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gemini agent loop with function calling. Uses the same REST models as the Node
 * {@code @google/generative-ai} package: {@code gemini-3-flash-preview} with fallback
 * {@code gemini-3.1-flash-lite-preview}. Thought signatures on model parts are preserved by
 * copying {@code candidates[0].content} verbatim into {@code contents}.
 */
@Service
public class AgentChatService {

    private static final int MAX_ITERATIONS = 10;

    private static final String SYSTEM_PROMPT = """
        You are a helpful AI assistant for a professional Service Booking & Consulting Platform.
        You help clients book consulting sessions, manage payments, and navigate the platform.
        You help consultants manage their schedules and bookings.
        You help admins oversee platform operations.

        Rules:
        - You have access to platform tools. Use them when the user wants to perform an action or needs real-time data.
        - Never expose or ask for raw passwords, full credit card numbers, or sensitive personal data.
        - Only use tools that match the user's role. A client cannot use consultant or admin tools. A consultant cannot use admin tools.
        - When an action is taken, summarize clearly what was done and confirm success or explain failure in plain language.
        - For general questions about how the platform works, answer conversationally without calling tools.
        - Be concise, professional, and friendly.
        - If you are unsure what the user wants, ask one clarifying question before acting.
        - Never query the database directly. Only use the provided tools.
        """;

    private final GeminiHttpClient geminiHttpClient;
    private final AgentToolExecutor agentToolExecutor;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.model.primary:gemini-3-flash-preview}")
    private String primaryModel;

    @Value("${gemini.model.fallback:gemini-3.1-flash-lite-preview}")
    private String fallbackModel;

    public AgentChatService(
        GeminiHttpClient geminiHttpClient,
        AgentToolExecutor agentToolExecutor,
        ChatConversationRepository chatConversationRepository,
        ChatMessageRepository chatMessageRepository,
        ObjectMapper objectMapper
    ) {
        this.geminiHttpClient = geminiHttpClient;
        this.agentToolExecutor = agentToolExecutor;
        this.chatConversationRepository = chatConversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AgentChatResponse chat(
        AgentChatRequest request,
        Authentication authentication,
        String authorizationHeader
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        if (!(authentication.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Role principalRole = resolveRole(authentication, cud, request.getRole());
        if (principalRole == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User role is unknown");
        }
        Long id = resolveUserId(cud, request.getUserId());
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token missing user id");
        }
        long userId = id;
        String role =
            switch (principalRole) {
                case CLIENT -> "client";
                case CONSULTANT -> "consultant";
                case ADMIN -> "admin";
            };
        ChatConversation conversation = loadOrCreateConversation(request.getConversationId(), userId, request.getMessage());
        Long conversationId = conversation.getId();

        ArrayNode contents = objectMapper.createArrayNode();
        appendStoredHistory(contents, conversationId);
        appendUserText(contents, request.getMessage());

        ArrayNode functionDeclarations = GeminiToolCatalog.functionDeclarationsForRole(role, objectMapper);
        ArrayNode toolsArray = objectMapper.createArrayNode();
        ObjectNode toolObj = objectMapper.createObjectNode();
        toolObj.set("functionDeclarations", functionDeclarations);
        toolsArray.add(toolObj);

        ObjectNode toolConfig = objectMapper.createObjectNode();
        ObjectNode fcc = objectMapper.createObjectNode();
        fcc.put("mode", "AUTO");
        toolConfig.set("functionCallingConfig", fcc);

        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode siParts = objectMapper.createArrayNode();
        ObjectNode siPart = objectMapper.createObjectNode();
        siPart.put("text", SYSTEM_PROMPT);
        siParts.add(siPart);
        systemInstruction.set("parts", siParts);

        String lastAction = null;
        JsonNode lastResult = null;
        Set<String> seenToolCalls = new HashSet<>();

        int iteration = 0;
        while (iteration < MAX_ITERATIONS) {
            ObjectNode generateBody = objectMapper.createObjectNode();
            generateBody.set("systemInstruction", systemInstruction);
            generateBody.set("contents", contents);
            generateBody.set("tools", toolsArray);
            generateBody.set("toolConfig", toolConfig);

            JsonNode response;
            try {
                response = callGeminiWithFallback(generateBody);
            } catch (Exception e) {
                return friendlyError(
                    "The assistant is temporarily unavailable. Please try again later.",
                    conversationId,
                    contents
                );
            }

            if (response.has("error")) {
                String msg = response.path("error").path("message").asText("Request failed");
                return friendlyError(msg, conversationId, contents);
            }

            JsonNode promptFeedback = response.path("promptFeedback");
            if (promptFeedback.has("blockReason") && !promptFeedback.get("blockReason").isNull()) {
                return new AgentChatResponse(
                    "I cannot help with that request.",
                    lastAction,
                    lastResult,
                    conversationId,
                    contents
                );
            }

            JsonNode candidates = response.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return new AgentChatResponse(
                    "I did not get a valid response from the model. Please try again.",
                    lastAction,
                    lastResult,
                    conversationId,
                    contents
                );
            }

            JsonNode candidate = candidates.get(0);
            JsonNode modelContent = candidate.get("content");
            if (modelContent == null || modelContent.isNull()) {
                return new AgentChatResponse(
                    "The model returned an empty response.",
                    lastAction,
                    lastResult,
                    conversationId,
                    contents
                );
            }

            contents.add(modelContent.deepCopy());

            JsonNode parts = modelContent.path("parts");
            List<FunctionCall> calls = extractFunctionCalls(parts);
            String plainText = extractText(parts);

            if (calls.isEmpty()) {
                String reply = plainText != null && !plainText.isBlank()
                    ? plainText
                    : "Done.";
                persistTurn(conversationId, request.getMessage(), reply);
                return new AgentChatResponse(reply, lastAction, lastResult, conversationId, contents);
            }

            ArrayNode responseParts = objectMapper.createArrayNode();
            for (FunctionCall fc : calls) {
                if (!GeminiToolCatalog.isAllowedForRole(fc.name(), role)) {
                    ObjectNode err = objectMapper.createObjectNode();
                    err.put("error", "This action is not allowed for your role.");
                    responseParts.add(buildFunctionResponsePart(fc.name(), wrapToolResult(err)));
                    continue;
                }
                try {
                    String signature = toolCallSignature(fc);
                    if (!seenToolCalls.add(signature)) {
                        String reply = "I already fetched that data. Please ask a more specific follow-up.";
                        persistTurn(conversationId, request.getMessage(), reply);
                        return new AgentChatResponse(reply, lastAction, lastResult, conversationId, contents);
                    }
                    JsonNode result = agentToolExecutor.execute(fc.name(), fc.args(), authorizationHeader, userId);
                    lastAction = fc.name();
                    lastResult = result;
                    responseParts.add(buildFunctionResponsePart(fc.name(), wrapToolResult(result)));
                } catch (Exception ex) {
                    ObjectNode err = objectMapper.createObjectNode();
                    err.put("error", "Tool execution failed");
                    responseParts.add(buildFunctionResponsePart(fc.name(), wrapToolResult(err)));
                }
            }

            ObjectNode userFnTurn = objectMapper.createObjectNode();
            userFnTurn.put("role", "user");
            userFnTurn.set("parts", responseParts);
            contents.add(userFnTurn);

            iteration++;
        }

        return new AgentChatResponse(
            "I reached the maximum number of tool steps for this request. Please try a simpler question or break it into parts.",
            lastAction,
            lastResult,
            conversationId,
            contents
        );
    }

    private AgentChatResponse friendlyError(String userMessage, Long conversationId, ArrayNode contents) {
        return new AgentChatResponse(userMessage, null, null, conversationId, contents);
    }

    private JsonNode callGeminiWithFallback(ObjectNode generateBody) {
        try {
            return geminiHttpClient.generateContent(generateBody, primaryModel);
        } catch (GeminiHttpClient.GeminiApiException e) {
            if (e.getStatus() == 404) {
                // Cost-effective alternative if primary model id is unavailable in the API
                return geminiHttpClient.generateContent(generateBody, fallbackModel);
            }
            throw e;
        }
    }

    private Role resolveRole(Authentication authentication, CustomUserDetails cud, String requestedRole) {
        if (cud.getRole() != null) {
            return cud.getRole();
        }
        for (GrantedAuthority a : authentication.getAuthorities()) {
            if (a == null || a.getAuthority() == null) {
                continue;
            }
            String v = a.getAuthority().replace("ROLE_", "");
            try {
                return Role.valueOf(v);
            } catch (Exception ignored) {
            }
        }
        if (requestedRole != null && !requestedRole.isBlank()) {
            try {
                return Role.valueOf(requestedRole.trim().toUpperCase());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Long resolveUserId(CustomUserDetails cud, String requestedUserId) {
        if (cud.getId() != null) {
            return cud.getId();
        }
        if (requestedUserId != null && !requestedUserId.isBlank()) {
            try {
                return Long.parseLong(requestedUserId.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private ChatConversation loadOrCreateConversation(Long requestedConversationId, long userId, String firstMessage) {
        if (requestedConversationId != null) {
            return chatConversationRepository.findByIdAndUserId(requestedConversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Conversation not found for user"));
        }
        ChatConversation c = new ChatConversation();
        c.setUserId(userId);
        c.setTitle(truncate(firstMessage, 120));
        return chatConversationRepository.save(c);
    }

    private void appendStoredHistory(ArrayNode contents, Long conversationId) {
        List<ChatMessage> history = chatMessageRepository.findByConversationIdOrderByIdAsc(conversationId);
        for (ChatMessage m : history) {
            String geminiRole = "ASSISTANT".equalsIgnoreCase(m.getRole()) ? "model" : "user";
            ObjectNode turn = objectMapper.createObjectNode();
            turn.put("role", geminiRole);
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode p = objectMapper.createObjectNode();
            p.put("text", m.getContent());
            parts.add(p);
            turn.set("parts", parts);
            contents.add(turn);
        }
    }

    private void persistTurn(Long conversationId, String userMessage, String assistantReply) {
        ChatMessage user = new ChatMessage();
        user.setConversationId(conversationId);
        user.setRole("USER");
        user.setContent(userMessage);
        user.setProvider("gemini");
        user.setModel(primaryModel);
        chatMessageRepository.save(user);

        ChatMessage assistant = new ChatMessage();
        assistant.setConversationId(conversationId);
        assistant.setRole("ASSISTANT");
        assistant.setContent(assistantReply);
        assistant.setProvider("gemini");
        assistant.setModel(primaryModel);
        chatMessageRepository.save(assistant);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private void appendUserText(ArrayNode contents, String message) {
        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("role", "user");
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode p = objectMapper.createObjectNode();
        p.put("text", message);
        parts.add(p);
        turn.set("parts", parts);
        contents.add(turn);
    }

    private String extractText(JsonNode parts) {
        if (!parts.isArray()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode p : parts) {
            if (p.has("text")) {
                sb.append(p.get("text").asText());
            }
        }
        return sb.toString();
    }

    private List<FunctionCall> extractFunctionCalls(JsonNode parts) {
        List<FunctionCall> out = new ArrayList<>();
        if (!parts.isArray()) {
            return out;
        }
        for (JsonNode p : parts) {
            if (p.has("functionCall")) {
                JsonNode fc = p.get("functionCall");
                String name = fc.path("name").asText("");
                JsonNode args = fc.get("args");
                if (args == null || args.isNull()) {
                    args = objectMapper.createObjectNode();
                } else if (args.isTextual()) {
                    try {
                        args = objectMapper.readTree(args.asText());
                    } catch (Exception e) {
                        args = objectMapper.createObjectNode();
                    }
                }
                out.add(new FunctionCall(name, args));
            }
        }
        return out;
    }

    private ObjectNode buildFunctionResponsePart(String name, ObjectNode responseStruct) {
        ObjectNode part = objectMapper.createObjectNode();
        ObjectNode fr = objectMapper.createObjectNode();
        fr.put("name", name);
        fr.set("response", responseStruct);
        part.set("functionResponse", fr);
        return part;
    }

    private ObjectNode wrapToolResult(JsonNode payload) {
        ObjectNode o = objectMapper.createObjectNode();
        o.set("result", payload);
        return o;
    }

    private String toolCallSignature(FunctionCall fc) {
        return fc.name() + "|" + (fc.args() == null ? "{}" : fc.args().toString());
    }

    private record FunctionCall(String name, JsonNode args) {
    }
}
