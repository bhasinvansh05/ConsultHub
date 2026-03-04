package com.consultingplatform.model.payment;

/**
 * Strategy Pattern – contract for all simulated payment processing strategies.
 *
 * Concrete implementations (all in this package):
 *   CreditCardPayment, DebitCardPayment, PayPalPayment, BankTransferPayment
 *
 * Usage flow (PaymentService):
 *   1. PaymentValidationService creates the correct strategy instance
 *   2. strategy.validatePaymentDetails() – validates fields per spec
 *   3. strategy.processPayment(amount)   – simulates gateway, returns partial Payment
 *   4. Service enriches the Payment (bookingId, clientId) and persists it
 */
public interface PaymentStrategy {

    /** Validates all payment-method-specific fields (card number, email, etc.). */
    boolean validatePaymentDetails();

    /**
     * Simulates processing and returns a partially-built Payment containing:
     * transactionId, amount, timestamp, strategyType, status=COMPLETED.
     * The caller sets bookingId and clientId before saving.
     */
    Payment processPayment(double amount);

    /** Returns the payment type name e.g. "CREDIT_CARD", "PAYPAL". */
    String getPaymentType();
}
