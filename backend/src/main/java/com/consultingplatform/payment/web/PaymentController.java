package com.consultingplatform.payment.web;

import com.consultingplatform.payment.domain.PaymentMethod;
import com.consultingplatform.payment.service.PaymentService;
import com.consultingplatform.payment.web.dto.PaymentMethodDto;
import com.consultingplatform.payment.web.dto.PaymentResponseDto;
import com.consultingplatform.payment.web.dto.ProcessPaymentRequest;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) throws InterruptedException {
        PaymentResponseDto response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/methods/{clientId}")
    public ResponseEntity<PaymentMethod> addPaymentMethod(
            @PathVariable Long clientId,
            @Valid @RequestBody PaymentMethodDto dto) {
        PaymentMethod created = paymentService.addPaymentMethod(clientId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/methods/{clientId}")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(paymentService.getPaymentMethods(clientId));
    }

    @PutMapping("/methods/{clientId}/{id}")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(
            @PathVariable Long clientId,
            @PathVariable Long id,
            @Valid @RequestBody PaymentMethodDto dto) {
        PaymentMethod updated = paymentService.updatePaymentMethod(clientId, id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/methods/{clientId}/{id}")
    public ResponseEntity<Void> removePaymentMethod(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        paymentService.removePaymentMethod(clientId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history/{clientId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistory(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(clientId));
    }

    @GetMapping("/history/{clientId}/status/{status}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistoryByStatus(
            @PathVariable Long clientId,
            @PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentHistoryByStatus(clientId, status));
    }
}
