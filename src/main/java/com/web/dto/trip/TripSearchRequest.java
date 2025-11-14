package com.web.dto.trip;

import com.web.entity.Trip;

import java.io.Serializable;
import java.time.LocalDate;

public record TripSearchRequest(
    Long routeId,
    LocalDate date,
    Trip.TripStatus status  // opcional
) implements Serializable {}

