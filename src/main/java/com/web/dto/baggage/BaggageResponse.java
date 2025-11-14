package com.web.dto.baggage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BaggageResponse(
    Long id,
    Long ticketId,
    BigDecimal weightKg,
    BigDecimal excessFee,
    String tagCode,
    LocalDateTime createdAt
) implements Serializable {}

