package com.consultingplatform.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Calls the Google Generative Language API (same REST surface used by the Node
 * {@code @google/generative-ai} package). Model strings match the JS SDK:
 * primary {@code gemini-3-flash-preview}; cost-effective fallback
 * {@code gemini-3.1-flash-lite-preview} if the primary model returns 404.
 */
@Component
public class GeminiHttpClient {

    private static final String GEMINI_BASE =
        "https://generativelanguage.googleapis.com/v1beta/models/";

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public GeminiHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode generateContent(JsonNode requestBody, String modelId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }
        String url = GEMINI_BASE + modelId + ":generateContent?key="
            + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        try {
            String raw = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(serialize(requestBody))
                .retrieve()
                .body(String.class);
            return objectMapper.readTree(raw);
        } catch (RestClientResponseException e) {
            throw new GeminiApiException(e.getStatusCode().value(), safeBody(e), e);
        } catch (Exception e) {
            throw new GeminiApiException(0, e.getMessage(), e);
        }
    }

    private static String safeBody(RestClientResponseException e) {
        try {
            return e.getResponseBodyAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String serialize(JsonNode node) throws Exception {
        return objectMapper.writeValueAsString(node);
    }

    /**
     * Non-HTTP failure (e.g. parsing) or HTTP error from Gemini.
     */
    public static class GeminiApiException extends RuntimeException {
        private final int status;

        public GeminiApiException(int status, String detail, Throwable cause) {
            super(detail, cause);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
