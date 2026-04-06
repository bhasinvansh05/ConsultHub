package com.consultingplatform.agent.web;

import com.consultingplatform.agent.service.AgentChatService;
import com.consultingplatform.agent.web.dto.AgentChatRequest;
import com.consultingplatform.agent.web.dto.AgentChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    private final AgentChatService agentChatService;
    private final ObjectMapper objectMapper;

    public AgentChatController(AgentChatService agentChatService, ObjectMapper objectMapper) {
        this.agentChatService = agentChatService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(
        @Valid @RequestBody AgentChatRequest request,
        Authentication authentication,
        HttpServletRequest httpRequest
    ) {
        try {
            String authorization = httpRequest.getHeader("Authorization");
            AgentChatResponse body = agentChatService.chat(request, authentication, authorization);
            return ResponseEntity.ok(body);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            ArrayNode empty = objectMapper.createArrayNode();
            if (request.getConversationHistory() != null && request.getConversationHistory().isArray()) {
                for (JsonNode n : request.getConversationHistory()) {
                    empty.add(n);
                }
            }
            AgentChatResponse err = new AgentChatResponse(
                "Something went wrong while processing your message. Please try again later.",
                null,
                null,
                request.getConversationId(),
                empty
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
