package com.consultingplatform.consultingservice.service.pricing;

import com.consultingplatform.admin.domain.PricingStrategyConfig;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("DISCOUNTED_PRICING")
public class DiscountedPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingStrategyConfig config) {
        if (config == null || config.getDiscountPercentage() <= 0 || config.getDiscountPercentage() >= 1) {
            return basePrice;
        }
        BigDecimal factor = BigDecimal.valueOf(1.0 - config.getDiscountPercentage());
        return basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
