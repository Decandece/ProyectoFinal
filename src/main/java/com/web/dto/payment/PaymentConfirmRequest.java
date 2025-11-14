package com.web.dto.payment;

import com.web.entity.Ticket;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentConfirmRequest(
    @NotNull Long ticketId,
    @NotNull Ticket.PaymentMethod paymentMethod,
    String transactionReference,  // Para QR/TRANSFER
    BigDecimal amount,
    String proofImageUrl  // Opcional
) implements Serializable {}

