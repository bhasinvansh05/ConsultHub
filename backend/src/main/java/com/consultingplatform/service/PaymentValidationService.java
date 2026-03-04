package com.consultingplatform.service;

import com.consultingplatform.dto.PaymentMethodDto;
import com.consultingplatform.model.enums.PaymentType;
import com.consultingplatform.model.payment.BankTransferPayment;
import com.consultingplatform.model.payment.CreditCardPayment;
import com.consultingplatform.model.payment.DebitCardPayment;
import com.consultingplatform.model.payment.PayPalPayment;
import com.consultingplatform.model.payment.PaymentMethod;
import com.consultingplatform.model.payment.PaymentStrategy;
import org.springframework.stereotype.Service;

@Service
public class PaymentValidationService {

    public PaymentStrategy createStrategy(PaymentMethodDto dto) {
        if (dto == null || dto.getType() == null) {
            throw new IllegalArgumentException("Payment type is required");
        }

        PaymentType type = PaymentType.valueOf(dto.getType());

        if (type == PaymentType.CREDIT_CARD) {
            return new CreditCardPayment(dto.getCardNumber(), dto.getExpiryDate(), dto.getCvv());

        } else if (type == PaymentType.DEBIT_CARD) {
            return new DebitCardPayment(dto.getCardNumber(), dto.getExpiryDate(), dto.getCvv());

        } else if (type == PaymentType.PAYPAL) {
            return new PayPalPayment(dto.getPaypalEmail());

        } else if (type == PaymentType.BANK_TRANSFER) {
            return new BankTransferPayment(dto.getAccountNumber(), dto.getRoutingNumber());

        } else {
            throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }

    public PaymentStrategy createStrategyFromSaved(PaymentMethod saved) {
        if (saved.getType() == PaymentType.CREDIT_CARD) {
            return new CreditCardPayment("**** **** **** " + saved.getLast4Digits(), saved.getExpiryDate(), null);

        } else if (saved.getType() == PaymentType.DEBIT_CARD) {
            return new DebitCardPayment("**** **** **** " + saved.getLast4Digits(), saved.getExpiryDate(), null);

        } else if (saved.getType() == PaymentType.PAYPAL) {
            return new PayPalPayment(saved.getPaypalEmail());

        } else if (saved.getType() == PaymentType.BANK_TRANSFER) {
            return new BankTransferPayment("****" + saved.getLast4AccountDigits(), saved.getRoutingNumber());

        } else {
            throw new IllegalArgumentException("Unknown payment type: " + saved.getType());
        }
    }
}
