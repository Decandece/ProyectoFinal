package com.web.dto.admin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public record ConfigUpdateRequest(
    Integer holdDurationMinutes,
    Integer noShowFeePercentage,
    Integer overbookingPercentage,
    Map<String, Integer> discountPercentages,  // "STUDENT": 20, etc
    BigDecimal baggageWeightLimit,
    BigDecimal baggagePricePerKg
) implements Serializable {}

