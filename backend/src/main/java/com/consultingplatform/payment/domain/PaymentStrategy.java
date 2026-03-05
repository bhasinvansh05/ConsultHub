package com.consultingplatform.payment.domain;

public interface PaymentStrategy {

    boolean validatePaymentDetails();

    Payment processPayment(double amount);

    String getPaymentType();
}
