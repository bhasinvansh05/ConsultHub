package com.consultingplatform.agent.service;

import com.consultingplatform.admin.web.dto.ConsultantApprovalDecision;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Invokes existing REST endpoints on this application (loopback). The caller must pass the
 * same Authorization header as the original client so Spring Security and {@code @PreAuthorize}
 * behave identically to a normal API call.
 */
@Service
public class AgentToolExecutor {

    private final RestClient internalRestApiRestClient;
    private final ObjectMapper objectMapper;

    public AgentToolExecutor(RestClient internalRestApiRestClient, ObjectMapper objectMapper) {
        this.internalRestApiRestClient = internalRestApiRestClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode execute(String toolName, JsonNode args, String authorizationHeader, long authenticatedUserId) {
        JsonNode a = args == null || args.isNull() ? objectMapper.createObjectNode() : args;
        try {
            return switch (toolName) {
                case "agent_chat" -> postJson("/api/agent/chat", agentChatBody(a), authorizationHeader);
                case "auth_login" -> postJson("/api/auth/login", loginBody(a), authorizationHeader);
                case "auth_logout" -> postJson("/api/auth/logout", "{}", authorizationHeader);
                case "auth_register" -> postJson("/api/auth/register", registerBody(a), authorizationHeader);
                case "list_services" -> listServices(a, authorizationHeader);
                case "get_service_by_id" -> getJson(
                    "/api/services/" + longArg(a, "service_id", "serviceId"),
                    authorizationHeader
                );
                case "list_service_availability" -> getJson(
                    "/api/services/" + longArg(a, "service_id", "serviceId") + "/availability",
                    authorizationHeader
                );
                case "list_consultants" -> getJson("/api/consultants", authorizationHeader);
                case "create_user" -> postJson("/api/users", userBody(a), authorizationHeader);
                case "list_users" -> getJson("/api/users", authorizationHeader);
                case "get_user" -> getJson("/api/users/" + longArg(a, "user_id", "userId"), authorizationHeader);
                case "update_user" -> putJson(
                    "/api/users/" + longArg(a, "user_id", "userId"),
                    userUpdateBody(a),
                    authorizationHeader
                );
                case "delete_user" -> delete("/api/users/" + longArg(a, "user_id", "userId"), authorizationHeader);
                case "request_booking" -> postJson("/bookings", bookingRequestBody(a, authenticatedUserId), authorizationHeader);
                case "get_booking_by_id" -> getJson(
                    "/bookings/" + longArg(a, "booking_id", "bookingId"),
                    authorizationHeader
                );
                case "cancel_booking" -> putJson(
                    "/bookings/" + longArg(a, "booking_id", "bookingId") + "/cancel",
                    "{}",
                    authorizationHeader
                );
                case "list_client_bookings" -> getJson("/bookings/client/" + authenticatedUserId, authorizationHeader);
                case "get_payment_methods" -> getJson("/api/payments/methods/" + authenticatedUserId, authorizationHeader);
                case "add_payment_method" -> postJson(
                    "/api/payments/methods/" + authenticatedUserId,
                    paymentMethodBody(a),
                    authorizationHeader
                );
                case "update_payment_method" -> putJson(
                    "/api/payments/methods/" + authenticatedUserId + "/" + longArg(a, "payment_method_id", "paymentMethodId"),
                    paymentMethodBody(a),
                    authorizationHeader
                );
                case "remove_payment_method" -> delete(
                    "/api/payments/methods/" + authenticatedUserId + "/" + longArg(a, "payment_method_id", "paymentMethodId"),
                    authorizationHeader
                );
                case "process_payment" -> postJson("/api/payments/process", processPaymentBody(a, authenticatedUserId), authorizationHeader);
                case "payment_history" -> getJson("/api/payments/history/" + authenticatedUserId, authorizationHeader);
                case "payment_history_by_status" -> getJson(
                    "/api/payments/history/" + authenticatedUserId + "/status/" + textArg(a, "status", "status"),
                    authorizationHeader
                );
                case "list_consultant_bookings" -> {
                    UriComponentsBuilder b = UriComponentsBuilder.fromPath(
                        "/api/consultant/" + authenticatedUserId + "/bookings");
                    String st = optionalText(a, "status");
                    if (st != null) {
                        b.queryParam("status", st);
                    }
                    yield getJson(b.toUriString(), authorizationHeader);
                }
                case "accept_booking" -> putJson(
                    "/api/consultant/" + authenticatedUserId + "/bookings/" + longArg(a, "booking_id", "bookingId") + "/accept",
                    "{}",
                    authorizationHeader
                );
                case "reject_booking" -> putJson(
                    "/api/consultant/" + authenticatedUserId + "/bookings/" + longArg(a, "booking_id", "bookingId") + "/reject",
                    rejectBody(a),
                    authorizationHeader
                );
                case "complete_booking" -> putJson(
                    "/api/consultant/" + authenticatedUserId + "/bookings/" + longArg(a, "booking_id", "bookingId") + "/complete",
                    "{}",
                    authorizationHeader
                );
                case "add_availability_slot" -> postJson(
                    "/api/consultant/" + authenticatedUserId + "/availability",
                    availabilityBody(a),
                    authorizationHeader
                );
                case "list_availability_slots" -> getJson("/api/consultant/" + authenticatedUserId + "/availability", authorizationHeader);
                case "delete_availability_slot" -> delete(
                    "/api/consultant/" + authenticatedUserId + "/availability/" + longArg(a, "slot_id", "slotId"),
                    authorizationHeader
                );
                case "approve_consultant" -> postJson(
                    "/api/admin/consultants/" + longArg(a, "consultant_id", "consultantId") + "/approval",
                    approvalBody(a, authenticatedUserId),
                    authorizationHeader
                );
                case "list_pending_consultants" -> getJson("/api/admin/consultants/pending", authorizationHeader);
                case "create_consulting_service" -> postJson(
                    "/api/admin/services",
                    createServiceBody(a),
                    authorizationHeader
                );
                case "update_policy" -> putJson(
                    "/api/admin/policies/" + textArg(a, "policy_key", "policyKey"),
                    policyBody(a, authenticatedUserId),
                    authorizationHeader
                );
                case "system_status" -> getJson("/api/admin/system/status", authorizationHeader);
                case "health_test" -> getJson("/test", authorizationHeader);
                default -> errorJson("Unknown tool: " + toolName);
            };
        } catch (IllegalArgumentException ex) {
            return errorJson(ex.getMessage());
        } catch (Exception ex) {
            return errorJson("Tool execution failed: " + ex.getMessage());
        }
    }

    private JsonNode listServices(JsonNode args, String auth) throws Exception {
        UriComponentsBuilder b = UriComponentsBuilder.fromPath("/api/services");
        String st = optionalText(args, "serviceType");
        if (st == null) {
            st = optionalText(args, "service_type");
        }
        if (st != null && !st.isBlank()) {
            b.queryParam("serviceType", st);
        }
        return getJson(b.build().toUriString(), auth);
    }

    private String bookingRequestBody(JsonNode args, long clientId) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("clientId", clientId);
        o.put("slotId", longArg(args, "slotId", "slot_id"));
        return objectMapper.writeValueAsString(o);
    }

    private String loginBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("usernameOrEmail", textArg(args, "username_or_email", "usernameOrEmail"));
        o.put("password", textArg(args, "password", "password"));
        return objectMapper.writeValueAsString(o);
    }

    private String agentChatBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("message", textArg(args, "message", "message"));
        JsonNode conversationId = args.get("conversation_id");
        if (conversationId == null || conversationId.isNull()) {
            conversationId = args.get("conversationId");
        }
        if (conversationId != null && !conversationId.isNull()) {
            o.put("conversationId", conversationId.asLong());
        }
        return objectMapper.writeValueAsString(o);
    }

    private String registerBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("firstName", textArg(args, "first_name", "firstName"));
        o.put("lastName", textArg(args, "last_name", "lastName"));
        o.put("email", textArg(args, "email", "email"));
        o.put("password", textArg(args, "password", "password"));
        String phone = optionalText(args, "phone_number");
        if (phone == null) {
            phone = optionalText(args, "phoneNumber");
        }
        if (phone != null) {
            o.put("phoneNumber", phone);
        }
        return objectMapper.writeValueAsString(o);
    }

    private String processPaymentBody(JsonNode args, long clientId) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("bookingId", longArg(args, "booking_id", "bookingId"));
        o.put("clientId", clientId);
        JsonNode amountNode = args.get("amount");
        if (amountNode == null || amountNode.isNull()) {
            throw new IllegalArgumentException("amount is required for process_payment");
        }
        BigDecimal amt = new BigDecimal(amountNode.asText());
        o.put("amount", amt);
        if (args.has("saved_payment_method_id") && !args.get("saved_payment_method_id").isNull()) {
            o.put("savedPaymentMethodId", longArg(args, "saved_payment_method_id", "savedPaymentMethodId"));
        } else if (args.has("savedPaymentMethodId") && !args.get("savedPaymentMethodId").isNull()) {
            o.put("savedPaymentMethodId", longArg(args, "saved_payment_method_id", "savedPaymentMethodId"));
        }
        JsonNode pd = args.get("payment_details");
        if (pd == null) {
            pd = args.get("paymentDetails");
        }
        if (pd != null && !pd.isNull()) {
            o.set("paymentDetails", pd);
        }
        return objectMapper.writeValueAsString(o);
    }

    private String availabilityBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("serviceId", longArg(args, "service_id", "serviceId"));
        String start = textArg(args, "start_at", "startAt");
        String end = textArg(args, "end_at", "endAt");
        o.put("startAt", start);
        o.put("endAt", end);
        return objectMapper.writeValueAsString(o);
    }

    private String createServiceBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("serviceType", textArg(args, "service_type", "serviceType"));
        o.put("title", textArg(args, "title", "title"));
        String description = optionalText(args, "description");
        if (description != null) {
            o.put("description", description);
        }
        o.put("durationMinutes", longArg(args, "duration_minutes", "durationMinutes"));
        JsonNode basePrice = args.get("base_price");
        if (basePrice == null || basePrice.isNull()) {
            basePrice = args.get("basePrice");
        }
        if (basePrice == null || basePrice.isNull()) {
            throw new IllegalArgumentException("Missing required parameter: base_price / basePrice");
        }
        o.put("basePrice", new BigDecimal(basePrice.asText()));
        return objectMapper.writeValueAsString(o);
    }

    private String paymentMethodBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("type", textArg(args, "type", "type"));
        Boolean isDefault = optionalBoolean(args, "is_default", "isDefault");
        if (isDefault != null) {
            o.put("isDefault", isDefault);
        }
        putOptionalText(o, "cardNumber", firstPresentText(args, "card_number", "cardNumber"));
        putOptionalText(o, "expiryDate", firstPresentText(args, "expiry_date", "expiryDate"));
        putOptionalText(o, "cvv", optionalText(args, "cvv"));
        putOptionalText(o, "cardholderName", firstPresentText(args, "cardholder_name", "cardholderName"));
        putOptionalText(o, "paypalEmail", firstPresentText(args, "paypal_email", "paypalEmail"));
        putOptionalText(o, "accountNumber", firstPresentText(args, "account_number", "accountNumber"));
        putOptionalText(o, "routingNumber", firstPresentText(args, "routing_number", "routingNumber"));
        return objectMapper.writeValueAsString(o);
    }

    private String userBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("role", textArg(args, "role", "role"));
        o.put("firstName", textArg(args, "firstName", "firstName"));
        o.put("lastName", textArg(args, "lastName", "lastName"));
        o.put("email", textArg(args, "email", "email"));
        o.put("password", textArg(args, "password", "password"));
        String phone = firstPresentText(args, "phone_number", "phoneNumber");
        if (phone != null) {
            o.put("phoneNumber", phone);
        }
        return objectMapper.writeValueAsString(o);
    }

    private String userUpdateBody(JsonNode args) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        putOptionalText(o, "firstName", firstPresentText(args, "first_name", "firstName"));
        putOptionalText(o, "lastName", firstPresentText(args, "last_name", "lastName"));
        putOptionalText(o, "email", optionalText(args, "email"));
        putOptionalText(o, "password", optionalText(args, "password"));
        putOptionalText(o, "phoneNumber", firstPresentText(args, "phone_number", "phoneNumber"));
        putOptionalText(o, "role", optionalText(args, "role"));
        return objectMapper.writeValueAsString(o);
    }

    private String rejectBody(JsonNode args) throws Exception {
        String reason = optionalText(args, "reason");
        if (reason == null || reason.isBlank()) {
            return "{}";
        }
        ObjectNode o = objectMapper.createObjectNode();
        o.put("reason", reason);
        return objectMapper.writeValueAsString(o);
    }

    private String approvalBody(JsonNode args, long adminUserId) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("adminId", String.valueOf(adminUserId));
        if (!args.has("decision") || args.get("decision").isNull()) {
            throw new IllegalArgumentException("decision is required (APPROVE or REJECT)");
        }
        String d = args.get("decision").asText();
        o.put("decision", ConsultantApprovalDecision.valueOf(d.trim().toUpperCase()).name());
        String reason = optionalText(args, "reason");
        if (reason != null) {
            o.put("reason", reason);
        }
        return objectMapper.writeValueAsString(o);
    }

    private String policyBody(JsonNode args, long adminUserId) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("adminId", String.valueOf(adminUserId));
        o.put("policyValue", textArg(args, "policy_value", "policyValue"));
        return objectMapper.writeValueAsString(o);
    }

    private JsonNode getJson(String uri, String authorizationHeader) throws Exception {
        try {
            String body = internalRestApiRestClient.get()
                .uri(uri)
                .headers(h -> {
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        h.set("Authorization", authorizationHeader);
                    }
                })
                .retrieve()
                .body(String.class);
            return body == null || body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
        } catch (RestClientResponseException e) {
            return httpError(e);
        }
    }

    private JsonNode postJson(String uri, String jsonBody, String authorizationHeader) throws Exception {
        try {
            String body = internalRestApiRestClient.post()
                .uri(uri)
                .headers(h -> {
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        h.set("Authorization", authorizationHeader);
                    }
                    h.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(jsonBody)
                .retrieve()
                .body(String.class);
            return body == null || body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
        } catch (RestClientResponseException e) {
            return httpError(e);
        }
    }

    private JsonNode putJson(String uri, String jsonBody, String authorizationHeader) throws Exception {
        try {
            String body = internalRestApiRestClient.put()
                .uri(uri)
                .headers(h -> {
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        h.set("Authorization", authorizationHeader);
                    }
                    h.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(jsonBody)
                .retrieve()
                .body(String.class);
            return body == null || body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
        } catch (RestClientResponseException e) {
            return httpError(e);
        }
    }

    private JsonNode delete(String uri, String authorizationHeader) throws Exception {
        try {
            internalRestApiRestClient.delete()
                .uri(uri)
                .headers(h -> {
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        h.set("Authorization", authorizationHeader);
                    }
                })
                .retrieve()
                .toBodilessEntity();
            ObjectNode ok = objectMapper.createObjectNode();
            ok.put("deleted", true);
            return ok;
        } catch (RestClientResponseException e) {
            return httpError(e);
        }
    }

    private JsonNode httpError(RestClientResponseException e) throws Exception {
        ObjectNode err = objectMapper.createObjectNode();
        err.put("httpStatus", e.getStatusCode().value());
        String b = e.getResponseBodyAsString();
        if (b != null && !b.isBlank()) {
            try {
                err.set("body", objectMapper.readTree(b));
            } catch (Exception ignored) {
                err.put("body", b);
            }
        }
        return err;
    }

    private JsonNode errorJson(String message) {
        ObjectNode err = objectMapper.createObjectNode();
        err.put("error", message);
        return err;
    }

    private long longArg(JsonNode args, String snake, String camel) {
        if (args.has(snake) && !args.get(snake).isNull()) {
            return args.get(snake).asLong();
        }
        if (args.has(camel) && !args.get(camel).isNull()) {
            return args.get(camel).asLong();
        }
        throw new IllegalArgumentException("Missing required parameter: " + snake + " / " + camel);
    }

    private String textArg(JsonNode args, String snake, String camel) {
        if (args.has(snake) && !args.get(snake).isNull()) {
            return args.get(snake).asText();
        }
        if (args.has(camel) && !args.get(camel).isNull()) {
            return args.get(camel).asText();
        }
        throw new IllegalArgumentException("Missing required parameter: " + snake + " / " + camel);
    }

    private String optionalText(JsonNode args, String name) {
        if (!args.has(name) || args.get(name).isNull()) {
            return null;
        }
        String t = args.get(name).asText();
        return t == null || t.isBlank() ? null : t;
    }

    private String firstPresentText(JsonNode args, String snake, String camel) {
        String v = optionalText(args, snake);
        if (v != null) {
            return v;
        }
        return optionalText(args, camel);
    }

    private Boolean optionalBoolean(JsonNode args, String snake, String camel) {
        JsonNode n = args.get(snake);
        if (n == null || n.isNull()) {
            n = args.get(camel);
        }
        if (n == null || n.isNull()) {
            return null;
        }
        if (n.isBoolean()) {
            return n.asBoolean();
        }
        String t = n.asText();
        if (t == null || t.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(t);
    }

    private void putOptionalText(ObjectNode target, String key, String value) {
        if (value != null) {
            target.put(key, value);
        }
    }
}
