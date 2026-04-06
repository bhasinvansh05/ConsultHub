package com.consultingplatform.consultingservice.service.pricing;

import com.consultingplatform.admin.domain.PricingStrategyConfig;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("DYNAMIC_PRICING")
public class DynamicPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingStrategyConfig config) {
        if (config == null || config.getDynamicMultiplier() <= 0) {
            return basePrice;
        }
        BigDecimal multiplier = BigDecimal.valueOf(config.getDynamicMultiplier());
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
