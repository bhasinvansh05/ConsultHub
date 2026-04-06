package com.consultingplatform.consultingservice.service.pricing;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PricingStrategyFactory {
    private final Map<String, PricingStrategy> strategies;

    public PricingStrategyFactory(Map<String, PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public PricingStrategy getStrategy(String strategyType) {
        if (strategyType == null) {
            return strategies.get("FIXED_PRICING");
        }
        
        String key = strategyType.toUpperCase() + "_PRICING";
        return strategies.getOrDefault(key, strategies.get("FIXED_PRICING"));
    }
}
