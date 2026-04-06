package com.consultingplatform.consultingservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ConsultingServiceDto {
    
    @NotBlank(message = "Service type is required")
    private String serviceType;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Duration in minutes is required")
    @Positive(message = "Duration must be greater than zero")
    private Integer durationMinutes;
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    private BigDecimal originalPrice;

    /** Optional. On create, defaults to true when omitted. On update, omit to leave unchanged. */
    private Boolean isActive;

    //Getters and Setters
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
