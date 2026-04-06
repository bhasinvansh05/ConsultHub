package com.consultingplatform.admin.domain;

public class PricingStrategyConfig {
    private String strategyType; // "FIXED", "DYNAMIC", "DISCOUNTED"
    private double dynamicMultiplier; // e.g., 1.2 for 20% surge
    private double discountPercentage; // e.g., 0.15 for 15% off

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public double getDynamicMultiplier() {
        return dynamicMultiplier;
    }

    public void setDynamicMultiplier(double dynamicMultiplier) {
        this.dynamicMultiplier = dynamicMultiplier;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
