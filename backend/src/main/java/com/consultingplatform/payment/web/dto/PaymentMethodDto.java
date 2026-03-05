package com.consultingplatform.payment.web.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDto {

    private String type;
    private Boolean isDefault;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardholderName;
    private String paypalEmail;
    private String accountNumber;
    private String routingNumber;
}
