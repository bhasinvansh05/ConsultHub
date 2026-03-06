package com.consultingplatform.consultant.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreateConsultingServiceRequest {

    @NotBlank(message = "serviceType is required")
    private String serviceType;

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotNull(message = "durationMinutes is required")
    @Positive(message = "durationMinutes must be > 0")
    private Integer durationMinutes;

    @NotNull(message = "basePrice is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "basePrice must be >= 0")
    private BigDecimal basePrice;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}
