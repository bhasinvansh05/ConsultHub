package com.consultingplatform.agent.web.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Response for POST /api/agent/chat. conversationHistory is the updated Gemini {@code contents} array
 * (including thought signatures) to persist and send on the next request.
 */
public class AgentChatResponse {

    private String reply;
    private String actionTaken;
    private Object actionResult;
    private Long conversationId;
    private JsonNode conversationHistory;

    public AgentChatResponse() {
    }

    public AgentChatResponse(
        String reply,
        String actionTaken,
        Object actionResult,
        Long conversationId,
        JsonNode conversationHistory
    ) {
        this.reply = reply;
        this.actionTaken = actionTaken;
        this.actionResult = actionResult;
        this.conversationId = conversationId;
        this.conversationHistory = conversationHistory;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public Object getActionResult() {
        return actionResult;
    }

    public void setActionResult(Object actionResult) {
        this.actionResult = actionResult;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public JsonNode getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(JsonNode conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}
