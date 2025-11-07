package com.web.dto.payment;

import com.web.entity.Ticket;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    Long ticketId,
    Ticket.PaymentMethod paymentMethod,
    BigDecimal amount,
    String status,  // "CONFIRMED", "PENDING", "FAILED"
    String transactionReference,
    LocalDateTime paidAt
) implements Serializable {}

