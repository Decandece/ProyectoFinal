package com.web.dto.ticket.availability;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record SeatAvailabilityRequest(
    @NotNull Long tripId,
    @NotNull Long fromStopId,
    @NotNull Long toStopId
) implements Serializable {}

