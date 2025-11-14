package com.web.dto.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CashCloseResponse(
    Long id,
    String userName,
    LocalDate date,
    BigDecimal expectedAmount,
    BigDecimal actualAmount,
    BigDecimal difference,
    Integer ticketCount,
    LocalDateTime closedAt
) implements Serializable {}

