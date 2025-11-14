package com.web.dto.baggage;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record BaggageCreateRequest(
    @NotNull BigDecimal weightKg,
    BigDecimal excessFee  // Calculado por servicio
) implements Serializable {}

