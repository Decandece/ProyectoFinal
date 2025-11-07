package com.web.dto.trip;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripCreateRequest(
    @NotNull Long routeId,
    @NotNull Long busId,
    @NotNull LocalDate tripDate,
    @NotNull LocalDateTime departureTime,
    @NotNull LocalDateTime arrivalEta
) implements Serializable {}

