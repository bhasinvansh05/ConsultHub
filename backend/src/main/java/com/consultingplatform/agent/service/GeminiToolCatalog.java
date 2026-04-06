package com.consultingplatform.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gemini function declarations aligned with existing REST endpoints (paths and DTO fields).
 */
public final class GeminiToolCatalog {

    private static final Set<String> CLIENT = Set.of(
        "agent_chat",
        "auth_logout",
        "list_services",
        "get_service_by_id",
        "list_service_availability",
        "list_consultants",
        "request_booking",
        "get_booking_by_id",
        "cancel_booking",
        "list_client_bookings",
        "get_payment_methods",
        "add_payment_method",
        "update_payment_method",
        "remove_payment_method",
        "process_payment",
        "payment_history",
        "payment_history_by_status",
        "health_test"
    );

    private static final Set<String> CONSULTANT = Set.of(
        "agent_chat",
        "auth_logout",
        "list_services",
        "get_service_by_id",
        "list_service_availability",
        "list_consultant_bookings",
        "get_booking_by_id",
        "accept_booking",
        "reject_booking",
        "complete_booking",
        "add_availability_slot",
        "list_availability_slots",
        "delete_availability_slot",
        "health_test"
    );

    private static final Set<String> ADMIN = Set.of(
        "agent_chat",
        "auth_logout",
        "list_services",
        "get_service_by_id",
        "list_service_availability",
        "list_consultants",
        "get_booking_by_id",
        "create_consulting_service",
        "approve_consultant",
        "list_pending_consultants",
        "update_policy",
        "system_status",
        "create_user",
        "list_users",
        "get_user",
        "update_user",
        "delete_user",
        "auth_login",
        "auth_register",
        "health_test"
    );

    private GeminiToolCatalog() {
    }

    public static ArrayNode functionDeclarationsForRole(String role, ObjectMapper mapper) {
        Set<String> allowed = switch (role) {
            case "client" -> CLIENT;
            case "consultant" -> CONSULTANT;
            case "admin" -> ADMIN;
            default -> Set.of();
        };
        ArrayNode all = allDeclarations(mapper);
        ArrayNode out = mapper.createArrayNode();
        for (int i = 0; i < all.size(); i++) {
            ObjectNode decl = (ObjectNode) all.get(i);
            if (allowed.contains(decl.get("name").asText())) {
                out.add(decl);
            }
        }
        return out;
    }

    public static boolean isAllowedForRole(String toolName, String role) {
        Set<String> allowed = switch (role) {
            case "client" -> CLIENT;
            case "consultant" -> CONSULTANT;
            case "admin" -> ADMIN;
            default -> Set.of();
        };
        return allowed.contains(toolName);
    }

    public static List<String> toolNamesForRole(String role) {
        return switch (role) {
            case "client" -> CLIENT.stream().sorted().collect(Collectors.toList());
            case "consultant" -> CONSULTANT.stream().sorted().collect(Collectors.toList());
            case "admin" -> ADMIN.stream().sorted().collect(Collectors.toList());
            default -> List.of();
        };
    }

    private static ObjectNode decl(ObjectMapper m, String name, String description, ObjectNode properties) {
        ObjectNode params = m.createObjectNode();
        params.put("type", "object");
        params.set("properties", properties);
        ObjectNode o = m.createObjectNode();
        o.put("name", name);
        o.put("description", description);
        o.set("parameters", params);
        return o;
    }

    private static ObjectNode propString(ObjectMapper m, String description) {
        ObjectNode o = m.createObjectNode();
        o.put("type", "string");
        o.put("description", description);
        return o;
    }

    private static ObjectNode propNumber(ObjectMapper m, String description) {
        ObjectNode o = m.createObjectNode();
        o.put("type", "number");
        o.put("description", description);
        return o;
    }

    private static ArrayNode allDeclarations(ObjectMapper m) {
        ArrayNode a = m.createArrayNode();

        ObjectNode p1 = m.createObjectNode();
        p1.set("serviceType", propString(m, "Optional filter; matches GET /api/services?serviceType="));
        a.add(decl(m, "list_services",
            "List active consulting services (GET /api/services).",
            p1));

        ObjectNode p1b = m.createObjectNode();
        p1b.set("service_id", propNumber(m, "Service id for GET /api/services/{id}."));
        a.add(decl(m, "get_service_by_id",
            "Get service details by id (GET /api/services/{id}).",
            p1b));

        ObjectNode p1c = m.createObjectNode();
        p1c.set("service_id", propNumber(m, "Service id for GET /api/services/{serviceId}/availability."));
        a.add(decl(m, "list_service_availability",
            "List availability slots for a service (GET /api/services/{serviceId}/availability).",
            p1c));

        a.add(decl(m, "list_consultants",
            "List registered consultants (GET /api/consultants).",
            m.createObjectNode()));

        ObjectNode p2 = m.createObjectNode();
        p2.set("slotId", propNumber(m, "Availability slot id to book (POST /bookings body slotId)."));
        a.add(decl(m, "request_booking",
            "Request a booking for the authenticated client (POST /bookings with clientId and slotId).",
            p2));

        ObjectNode p3 = m.createObjectNode();
        p3.set("booking_id", propNumber(m, "Booking id (PUT /bookings/{id}/cancel)."));
        a.add(decl(m, "cancel_booking",
            "Cancel a booking for the authenticated user (PUT /bookings/{id}/cancel).",
            p3));

        ObjectNode p3b = m.createObjectNode();
        p3b.set("booking_id", propNumber(m, "Booking id for GET /bookings/{id}."));
        a.add(decl(m, "get_booking_by_id",
            "Get booking details (GET /bookings/{id}).",
            p3b));

        a.add(decl(m, "list_client_bookings",
            "List the authenticated client's bookings (GET /bookings/client/{clientId}).",
            m.createObjectNode()));

        a.add(decl(m, "get_payment_methods",
            "List saved payment methods for the authenticated client (GET /api/payments/methods/{clientId}).",
            m.createObjectNode()));

        ObjectNode p4b = m.createObjectNode();
        p4b.set("type", propString(m, "Payment type (e.g., CARD, PAYPAL, BANK_TRANSFER)."));
        p4b.set("is_default", propString(m, "Optional boolean-like value to mark method default."));
        p4b.set("card_number", propString(m, "Optional card number digits."));
        p4b.set("expiry_date", propString(m, "Optional card expiry."));
        p4b.set("cvv", propString(m, "Optional CVV."));
        p4b.set("cardholder_name", propString(m, "Optional cardholder name."));
        p4b.set("paypal_email", propString(m, "Optional PayPal email."));
        p4b.set("account_number", propString(m, "Optional bank account number."));
        p4b.set("routing_number", propString(m, "Optional routing number."));
        a.add(decl(m, "add_payment_method",
            "Add a saved payment method for the authenticated client (POST /api/payments/methods/{clientId}).",
            p4b));

        ObjectNode p4c = m.createObjectNode();
        p4c.set("payment_method_id", propNumber(m, "Saved payment method id for PUT /api/payments/methods/{clientId}/{id}."));
        p4c.set("type", propString(m, "Payment type."));
        p4c.set("is_default", propString(m, "Optional boolean-like value to mark method default."));
        p4c.set("card_number", propString(m, "Optional card number digits."));
        p4c.set("expiry_date", propString(m, "Optional card expiry."));
        p4c.set("cvv", propString(m, "Optional CVV."));
        p4c.set("cardholder_name", propString(m, "Optional cardholder name."));
        p4c.set("paypal_email", propString(m, "Optional PayPal email."));
        p4c.set("account_number", propString(m, "Optional bank account number."));
        p4c.set("routing_number", propString(m, "Optional routing number."));
        a.add(decl(m, "update_payment_method",
            "Update a saved payment method for the authenticated client (PUT /api/payments/methods/{clientId}/{id}).",
            p4c));

        ObjectNode p4d = m.createObjectNode();
        p4d.set("payment_method_id", propNumber(m, "Saved payment method id for DELETE /api/payments/methods/{clientId}/{id}."));
        a.add(decl(m, "remove_payment_method",
            "Remove a saved payment method for the authenticated client (DELETE /api/payments/methods/{clientId}/{id}).",
            p4d));

        ObjectNode p4 = m.createObjectNode();
        p4.set("booking_id", propNumber(m, "Booking to pay (ProcessPaymentRequest.bookingId)."));
        p4.set("amount", propNumber(m, "Payment amount (ProcessPaymentRequest.amount)."));
        ObjectNode saved = propNumber(m, "Optional saved payment method id (ProcessPaymentRequest.savedPaymentMethodId); prefer this over raw card data.");
        p4.set("saved_payment_method_id", saved);
        ObjectNode pd = m.createObjectNode();
        pd.put("type", "object");
        pd.put("description", "Optional PaymentMethodDto; never send full card numbers—use saved_payment_method_id when possible.");
        pd.set("properties", m.createObjectNode());
        p4.set("payment_details", pd);
        a.add(decl(m, "process_payment",
            "Process payment for a booking (POST /api/payments/process). Prefer saved_payment_method_id.",
            p4));

        a.add(decl(m, "payment_history",
            "List payment history for the authenticated client (GET /api/payments/history/{clientId}).",
            m.createObjectNode()));

        ObjectNode p4e = m.createObjectNode();
        p4e.set("status", propString(m, "Payment status for GET /api/payments/history/{clientId}/status/{status}."));
        a.add(decl(m, "payment_history_by_status",
            "List payment history by status for the authenticated client (GET /api/payments/history/{clientId}/status/{status}).",
            p4e));

        ObjectNode p5 = m.createObjectNode();
        ObjectNode st = propString(m, "Optional booking status filter e.g. REQUESTED (GET /api/consultant/{id}/bookings?status=).");
        p5.set("status", st);
        a.add(decl(m, "list_consultant_bookings",
            "List bookings for the authenticated consultant (GET /api/consultant/{consultantId}/bookings).",
            p5));

        ObjectNode p6 = m.createObjectNode();
        p6.set("booking_id", propNumber(m, "Booking id to accept."));
        a.add(decl(m, "accept_booking",
            "Accept a booking (PUT /api/consultant/{consultantId}/bookings/{bookingId}/accept).",
            p6));

        ObjectNode p7 = m.createObjectNode();
        p7.set("booking_id", propNumber(m, "Booking id to reject."));
        p7.set("reason", propString(m, "Optional rejection reason (BookingDecisionRequest.reason)."));
        a.add(decl(m, "reject_booking",
            "Reject a booking (PUT /api/consultant/{consultantId}/bookings/{bookingId}/reject).",
            p7));

        ObjectNode p8 = m.createObjectNode();
        p8.set("booking_id", propNumber(m, "Booking id to complete."));
        a.add(decl(m, "complete_booking",
            "Mark a booking completed (PUT /api/consultant/{consultantId}/bookings/{bookingId}/complete).",
            p8));

        ObjectNode p9 = m.createObjectNode();
        p9.set("service_id", propNumber(m, "Consulting service id (CreateAvailabilitySlotRequest.serviceId)."));
        p9.set("start_at", propString(m, "Slot start ISO-8601 offset datetime (CreateAvailabilitySlotRequest.startAt)."));
        p9.set("end_at", propString(m, "Slot end ISO-8601 offset datetime (CreateAvailabilitySlotRequest.endAt)."));
        a.add(decl(m, "add_availability_slot",
            "Add an availability slot (POST /api/consultant/{consultantId}/availability).",
            p9));

        a.add(decl(m, "list_availability_slots",
            "List availability slots (GET /api/consultant/{consultantId}/availability).",
            m.createObjectNode()));

        ObjectNode p10 = m.createObjectNode();
        p10.set("slot_id", propNumber(m, "Slot id to delete."));
        a.add(decl(m, "delete_availability_slot",
            "Delete an availability slot (DELETE /api/consultant/{consultantId}/availability/{slotId}).",
            p10));

        ObjectNode p11 = m.createObjectNode();
        p11.set("consultant_id", propNumber(m, "Consultant user id to approve or reject."));
        p11.set("decision", propString(m, "APPROVE or REJECT (ConsultantApprovalDecision)."));
        p11.set("reason", propString(m, "Optional reason (ConsultantApprovalRequestDto.reason)."));
        a.add(decl(m, "approve_consultant",
            "Approve or reject a pending consultant registration (POST /api/admin/consultants/{consultantId}/approval).",
            p11));

        a.add(decl(m, "list_pending_consultants",
            "List pending consultant registrations (GET /api/admin/consultants/pending).",
            m.createObjectNode()));

        ObjectNode p12 = m.createObjectNode();
        p12.set("policy_key", propString(m, "Policy key path segment (PUT /api/admin/policies/{policyKey})."));
        p12.set("policy_value", propString(m, "New policy text (PolicyUpsertRequestDto.policyValue)."));
        a.add(decl(m, "update_policy",
            "Create or update a system policy (PUT /api/admin/policies/{policyKey}).",
            p12));

        a.add(decl(m, "system_status",
            "View platform status stub (GET /api/admin/system/status).",
            m.createObjectNode()));

        ObjectNode p13 = m.createObjectNode();
        p13.set("service_type", propString(m, "Service type."));
        p13.set("title", propString(m, "Service title."));
        p13.set("description", propString(m, "Optional service description."));
        p13.set("duration_minutes", propNumber(m, "Service duration in minutes."));
        p13.set("base_price", propNumber(m, "Service base price."));
        a.add(decl(m, "create_consulting_service",
            "Create a consulting service (POST /api/admin/services).",
            p13));

        ObjectNode p14 = m.createObjectNode();
        p14.set("role", propString(m, "User role: CLIENT, CONSULTANT, or ADMIN."));
        p14.set("firstName", propString(m, "First name."));
        p14.set("lastName", propString(m, "Last name."));
        p14.set("email", propString(m, "Email."));
        p14.set("password", propString(m, "Password."));
        p14.set("phoneNumber", propString(m, "Optional phone number."));
        a.add(decl(m, "create_user",
            "Create a user (POST /api/users).",
            p14));

        a.add(decl(m, "list_users",
            "List all users (GET /api/users).",
            m.createObjectNode()));

        ObjectNode p15 = m.createObjectNode();
        p15.set("user_id", propNumber(m, "User id for GET /api/users/{id}."));
        a.add(decl(m, "get_user",
            "Get a user by id (GET /api/users/{id}).",
            p15));

        ObjectNode p16 = m.createObjectNode();
        p16.set("user_id", propNumber(m, "User id for PUT /api/users/{id}."));
        p16.set("firstName", propString(m, "Optional first name."));
        p16.set("lastName", propString(m, "Optional last name."));
        p16.set("email", propString(m, "Optional email."));
        p16.set("password", propString(m, "Optional password."));
        p16.set("phoneNumber", propString(m, "Optional phone number."));
        p16.set("role", propString(m, "Optional role."));
        a.add(decl(m, "update_user",
            "Update a user by id (PUT /api/users/{id}).",
            p16));

        ObjectNode p17 = m.createObjectNode();
        p17.set("user_id", propNumber(m, "User id for DELETE /api/users/{id}."));
        a.add(decl(m, "delete_user",
            "Delete a user by id (DELETE /api/users/{id}).",
            p17));

        ObjectNode p18 = m.createObjectNode();
        p18.set("username_or_email", propString(m, "Username or email for login."));
        p18.set("password", propString(m, "Password for login."));
        a.add(decl(m, "auth_login",
            "Login and get a JWT token (POST /api/auth/login).",
            p18));

        ObjectNode p18b = m.createObjectNode();
        p18b.set("message", propString(m, "Chat message for POST /api/agent/chat."));
        p18b.set("conversation_id", propNumber(m, "Optional conversation id for continuing chat."));
        a.add(decl(m, "agent_chat",
            "Call the agent chat endpoint (POST /api/agent/chat).",
            p18b));

        a.add(decl(m, "auth_logout",
            "Logout current session (POST /api/auth/logout).",
            m.createObjectNode()));

        ObjectNode p19 = m.createObjectNode();
        p19.set("first_name", propString(m, "First name."));
        p19.set("last_name", propString(m, "Last name."));
        p19.set("email", propString(m, "Email."));
        p19.set("password", propString(m, "Password."));
        p19.set("phone_number", propString(m, "Optional phone number."));
        a.add(decl(m, "auth_register",
            "Register a new account (POST /api/auth/register).",
            p19));

        a.add(decl(m, "health_test",
            "Health test endpoint (GET /test).",
            m.createObjectNode()));

        return a;
    }
}
