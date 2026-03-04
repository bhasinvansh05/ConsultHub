package com.consultingplatform.model.payment;

public interface PaymentStrategy {

    boolean validatePaymentDetails();

    Payment processPayment(double amount);

    String getPaymentType();
}
