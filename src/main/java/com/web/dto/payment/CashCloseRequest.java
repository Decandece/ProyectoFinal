package com.web.dto.payment;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

// POST /api/cash/close
public record CashCloseRequest(
    @NotNull Long userId,  // CLERK o DRIVER
    @NotNull LocalDate date,
    @NotNull BigDecimal expectedAmount,
    @NotNull BigDecimal actualAmount,
    String notes
) implements Serializable {}

