package com.consultingplatform.consultingservice.service.pricing;

import com.consultingplatform.admin.domain.PricingStrategyConfig;
import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(BigDecimal basePrice, PricingStrategyConfig config);
}
