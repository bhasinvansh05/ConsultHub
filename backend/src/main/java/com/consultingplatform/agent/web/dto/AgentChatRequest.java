package com.consultingplatform.agent.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

/**
 * Request for POST /api/agent/chat. conversationHistory follows Gemini {@code contents} shape
 * (including thought signatures on model parts when present).
 *
 * <p>Identity and role for tools are taken from the JWT; {@code userId} and {@code role} are
 * optional and ignored (kept for older clients / documentation).
 */
public class AgentChatRequest {

    @NotBlank(message = "message is required")
    private String message;

    /** @deprecated Ignored; role is derived from the authenticated principal. */
    @Deprecated
    private String role;

    /** @deprecated Ignored; user id is derived from the authenticated principal. */
    @Deprecated
    private String userId;

    /**
     * Conversation id for server-side chat memory. If omitted, a new conversation is started.
     */
    private Long conversationId;

    /**
     * Prior turns in Gemini {@code contents} format; may be null or empty for a new conversation.
     */
    private JsonNode conversationHistory;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public JsonNode getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(JsonNode conversationHistory) {
        this.conversationHistory = conversationHistory;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
