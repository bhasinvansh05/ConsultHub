package com.consultingplatform.payment.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDto {

    @NotBlank(message = "payment type is required")
    private String type;

    private Boolean isDefault;

    @Size(min = 12, max = 19, message = "cardNumber must be between 12 and 19 digits")
    @Pattern(regexp = "\\d+", message = "cardNumber must contain only digits")
    private String cardNumber;

    private String expiryDate;

    @Size(min = 3, max = 4, message = "cvv must be 3 or 4 digits")
    @Pattern(regexp = "\\d+", message = "cvv must contain only digits")
    private String cvv;

    private String cardholderName;

    @Email(message = "paypalEmail must be a valid email address")
    private String paypalEmail;

    private String accountNumber;
    private String routingNumber;
}
