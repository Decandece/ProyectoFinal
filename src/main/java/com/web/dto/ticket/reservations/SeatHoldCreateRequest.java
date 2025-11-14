package com.web.dto.ticket.reservations;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record SeatHoldCreateRequest(
    @NotNull Long tripId,
    @NotNull Integer seatNumber,
    @NotNull Long userId
) implements Serializable {}

