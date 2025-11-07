package com.web.dto.ticket;

import com.web.entity.Ticket;

import java.io.Serializable;
import java.math.BigDecimal;

public record TicketCancelResponse(
    Long ticketId,
    Ticket.TicketStatus status,
    BigDecimal refundAmount,
    Integer refundPercentage,  // Según política
    String message
) implements Serializable {}

