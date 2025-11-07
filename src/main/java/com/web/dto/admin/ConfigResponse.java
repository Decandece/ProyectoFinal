package com.web.dto.admin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record ConfigResponse(
    Integer holdDurationMinutes,
    Integer noShowFeePercentage,
    Integer overbookingPercentage,
    Map<String, Integer> discountPercentages,
    BigDecimal baggageWeightLimit,
    BigDecimal baggagePricePerKg,
    LocalDateTime lastUpdated
) implements Serializable {}

