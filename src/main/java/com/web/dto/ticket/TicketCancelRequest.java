package com.web.dto.ticket;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record TicketCancelRequest(
    @NotNull Long ticketId,
    String cancellationReason
) implements Serializable {}

