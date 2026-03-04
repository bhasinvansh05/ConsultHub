package com.consultingplatform.controller;

import com.consultingplatform.dto.PaymentMethodDto;
import com.consultingplatform.dto.PaymentResponseDto;
import com.consultingplatform.dto.ProcessPaymentRequest;
import com.consultingplatform.model.payment.PaymentMethod;
import com.consultingplatform.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for UC5 – Process Payment,
 *                    UC6 – Manage Payment Methods,
 *                    UC7 – View Payment History.
 *
 * Base path: /api/payments
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // =======================================================
    // UC5 – Process Payment
    // =======================================================

    /**
     * POST /api/payments/process
     *
     * Initiates payment for a confirmed booking.
     * Accepts either a savedPaymentMethodId or inline paymentDetails.
     * Simulates a 2-3 second processing delay before returning.
     *
     * Request body: ProcessPaymentRequest
     * Response:     PaymentResponseDto (status COMPLETED on success)
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @RequestBody ProcessPaymentRequest request) throws InterruptedException {
        PaymentResponseDto response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    // =======================================================
    // UC6 – Manage Payment Methods
    // =======================================================

    /**
     * POST /api/payments/methods/{clientId}
     *
     * Adds a new saved payment method for the client.
     * Validates payment details and stores only masked/safe data.
     */
    @PostMapping("/methods/{clientId}")
    public ResponseEntity<PaymentMethod> addPaymentMethod(
            @PathVariable Long clientId,
            @RequestBody PaymentMethodDto dto) {
        PaymentMethod created = paymentService.addPaymentMethod(clientId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/payments/methods/{clientId}
     *
     * Returns all saved payment methods for the client.
     */
    @GetMapping("/methods/{clientId}")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(paymentService.getPaymentMethods(clientId));
    }

    /**
     * PUT /api/payments/methods/{clientId}/{id}
     *
     * Updates an existing saved payment method owned by the client.
     * Re-validates all fields before persisting.
     */
    @PutMapping("/methods/{clientId}/{id}")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(
            @PathVariable Long clientId,
            @PathVariable Long id,
            @RequestBody PaymentMethodDto dto) {
        PaymentMethod updated = paymentService.updatePaymentMethod(clientId, id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/payments/methods/{clientId}/{id}
     *
     * Removes a saved payment method owned by the client.
     */
    @DeleteMapping("/methods/{clientId}/{id}")
    public ResponseEntity<Void> removePaymentMethod(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        paymentService.removePaymentMethod(clientId, id);
        return ResponseEntity.noContent().build();
    }

    // =======================================================
    // UC7 – View Payment History
    // =======================================================

    /**
     * GET /api/payments/history/{clientId}
     *
     * Returns the full payment history for the client (all statuses),
     * sorted newest first.
     */
    @GetMapping("/history/{clientId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistory(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(clientId));
    }

    /**
     * GET /api/payments/history/{clientId}/status/{status}
     *
     * Returns payment history filtered by status.
     * status must be one of: PENDING | COMPLETED | FAILED | REFUNDED  (case-insensitive)
     */
    @GetMapping("/history/{clientId}/status/{status}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistoryByStatus(
            @PathVariable Long clientId,
            @PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentHistoryByStatus(clientId, status));
    }
}
