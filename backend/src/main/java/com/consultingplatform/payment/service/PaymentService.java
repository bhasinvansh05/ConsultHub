package com.consultingplatform.payment.service;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.repository.BookingRepository;
import com.consultingplatform.payment.domain.Payment;
import com.consultingplatform.payment.domain.PaymentMethod;
import com.consultingplatform.payment.domain.PaymentStatus;
import com.consultingplatform.payment.domain.PaymentStrategy;
import com.consultingplatform.payment.domain.PaymentType;
import com.consultingplatform.notification.service.NotificationService;
import com.consultingplatform.payment.repository.PaymentMethodRepository;
import com.consultingplatform.payment.repository.PaymentRepository;
import com.consultingplatform.payment.web.dto.PaymentMethodDto;
import com.consultingplatform.payment.web.dto.PaymentResponseDto;
import com.consultingplatform.payment.web.dto.ProcessPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BookingRepository bookingRepository;
    private final PaymentValidationService validationService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponseDto processPayment(ProcessPaymentRequest request) throws InterruptedException {
        if (request.getBookingId() == null || request.getClientId() == null || request.getAmount() == null) {
            throw new IllegalArgumentException("bookingId, clientId, and amount are required");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getClientId().equals(request.getClientId())) {
            throw new IllegalArgumentException("Booking does not belong to this client");
        }

        String currentStatus = booking.getStatus();
        if (!"CONFIRMED".equals(currentStatus) && !"PENDING_PAYMENT".equals(currentStatus)) {
            throw new IllegalArgumentException("Booking must be CONFIRMED or PENDING_PAYMENT before payment");
        }

        if ("CONFIRMED".equals(currentStatus)) {
            booking.setStatus("PENDING_PAYMENT");
            bookingRepository.save(booking);
        }

        PaymentStrategy strategy;
        boolean skipValidation;

        if (request.getSavedPaymentMethodId() != null) {
            Optional<PaymentMethod> savedOpt = paymentMethodRepository
                    .findByIdAndClientId(request.getSavedPaymentMethodId(), request.getClientId());

            if (!savedOpt.isPresent()) {
                throw new IllegalArgumentException("Payment method not found or does not belong to this client");
            }

            strategy = validationService.createStrategyFromSaved(savedOpt.get());
            skipValidation = true;

        } else {
            if (request.getPaymentDetails() == null || request.getPaymentDetails().getType() == null) {
                throw new IllegalArgumentException("Either savedPaymentMethodId or paymentDetails with a type must be provided");
            }

            strategy = validationService.createStrategy(request.getPaymentDetails());
            skipValidation = false;
        }

        if (!skipValidation && !strategy.validatePaymentDetails()) {
            throw new IllegalArgumentException("Payment details validation failed for type: " + strategy.getPaymentType());
        }

        Thread.sleep(2000);

        Payment payment = strategy.processPayment(request.getAmount().doubleValue());
        payment.setBookingId(request.getBookingId());
        payment.setClientId(request.getClientId());
        payment.setStrategy(strategy);

        payment = paymentRepository.save(payment);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            booking.setStatus("PAID");
            bookingRepository.save(booking);
            notificationService.sendPaymentSuccessNotification(booking);
        }

        return toResponseDto(payment);
    }

    public PaymentMethod addPaymentMethod(Long clientId, PaymentMethodDto dto) {
        PaymentStrategy strategy = validationService.createStrategy(dto);

        if (!strategy.validatePaymentDetails()) {
            throw new IllegalArgumentException("Invalid payment details for type: " + strategy.getPaymentType());
        }

        PaymentType type = PaymentType.valueOf(dto.getType());
        PaymentMethod method = buildPaymentMethod(clientId, dto, type);
        return paymentMethodRepository.save(method);
    }

    public List<PaymentMethod> getPaymentMethods(Long clientId) {
        return paymentMethodRepository.findByClientId(clientId);
    }

    public PaymentMethod updatePaymentMethod(Long clientId, Long id, PaymentMethodDto dto) {
        Optional<PaymentMethod> existingOpt = paymentMethodRepository.findByIdAndClientId(id, clientId);

        if (!existingOpt.isPresent()) {
            throw new IllegalArgumentException("Payment method not found or does not belong to this client");
        }
        PaymentMethod existing = existingOpt.get();

        PaymentStrategy strategy = validationService.createStrategy(dto);
        if (!strategy.validatePaymentDetails()) {
            throw new IllegalArgumentException("Invalid payment details for type: " + strategy.getPaymentType());
        }

        PaymentType type = PaymentType.valueOf(dto.getType());
        PaymentMethod updated = buildPaymentMethod(clientId, dto, type);
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        return paymentMethodRepository.save(updated);
    }

    @Transactional
    public void removePaymentMethod(Long clientId, Long id) {
        Optional<PaymentMethod> methodOpt = paymentMethodRepository.findByIdAndClientId(id, clientId);

        if (!methodOpt.isPresent()) {
            throw new IllegalArgumentException("Payment method not found or does not belong to this client");
        }

        paymentMethodRepository.deleteByIdAndClientId(id, clientId);
    }

    public List<PaymentResponseDto> getPaymentHistory(Long clientId) {
        List<Payment> payments = paymentRepository.findByClientIdOrderByTimestampDesc(clientId);

        List<PaymentResponseDto> result = new ArrayList<>();
        for (Payment payment : payments) {
            result.add(toResponseDto(payment));
        }
        return result;
    }

    public List<PaymentResponseDto> getPaymentHistoryByStatus(Long clientId, String status) {
        PaymentStatus paymentStatus;
        try {
            String normalized = status.toUpperCase();
            if ("COMPLETED".equals(normalized)) {
                normalized = "SUCCESS";
            }
            paymentStatus = PaymentStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Valid values: PENDING, SUCCESS, FAILED, REFUNDED (COMPLETED is accepted as alias for SUCCESS)");
        }

        List<Payment> payments = paymentRepository.findByClientIdAndStatusOrderByTimestampDesc(clientId, paymentStatus);

        List<PaymentResponseDto> result = new ArrayList<>();
        for (Payment payment : payments) {
            result.add(toResponseDto(payment));
        }
        return result;
    }

    @Transactional
    public void processRefund(Long bookingId, double refundPercentage) {
        if (refundPercentage <= 0) {
            return;
        }

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        for (Payment payment : payments) {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.REFUNDED);
                java.math.BigDecimal refundAmount = payment.getAmount().multiply(java.math.BigDecimal.valueOf(refundPercentage)).setScale(2, java.math.RoundingMode.HALF_UP);
                payment.setRefundAmount(refundAmount);
                payment.setRefundedAt(java.time.LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }
    }

    private PaymentMethod buildPaymentMethod(Long clientId, PaymentMethodDto dto, PaymentType type) {
        PaymentMethod method = new PaymentMethod();
        method.setClientId(clientId);
        method.setType(type);
        method.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault());

        if (type == PaymentType.CREDIT_CARD || type == PaymentType.DEBIT_CARD) {
            String cardNum = dto.getCardNumber().replaceAll(" ", "");
            method.setLast4Digits(cardNum.substring(cardNum.length() - 4));
            method.setExpiryDate(dto.getExpiryDate().trim());
            if (dto.getCardholderName() != null) {
                method.setCardholderName(dto.getCardholderName().trim());
            }

        } else if (type == PaymentType.PAYPAL) {
            method.setPaypalEmail(dto.getPaypalEmail().trim());

        } else if (type == PaymentType.BANK_TRANSFER) {
            String accNum = dto.getAccountNumber().replaceAll(" ", "");
            method.setLast4AccountDigits(accNum.substring(accNum.length() - 4));
            method.setRoutingNumber(dto.getRoutingNumber().replaceAll(" ", ""));
        }

        return method;
    }

    private PaymentResponseDto toResponseDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setTransactionId(payment.getTransactionId());
        dto.setBookingId(payment.getBookingId());
        dto.setClientId(payment.getClientId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().name());
        dto.setPaymentType(payment.getStrategyType());
        dto.setTimestamp(payment.getTimestamp());
        dto.setFailureReason(payment.getFailureReason());
        dto.setRefundAmount(payment.getRefundAmount());
        dto.setRefundedAt(payment.getRefundedAt());
        return dto;
    }
}
