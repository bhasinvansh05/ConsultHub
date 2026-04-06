package com.consultingplatform.consultingservice.service.pricing;

import com.consultingplatform.admin.domain.PricingStrategyConfig;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("FIXED_PRICING")
public class FixedPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingStrategyConfig config) {
        return basePrice;
    }
}
