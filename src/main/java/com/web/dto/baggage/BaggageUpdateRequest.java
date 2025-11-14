package com.web.dto.baggage;

import java.io.Serializable;
import java.math.BigDecimal;

public record BaggageUpdateRequest(
    BigDecimal weightKg,
    BigDecimal excessFee
) implements Serializable {}

