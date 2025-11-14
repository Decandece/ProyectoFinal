package com.web.dto.trip;

import com.web.entity.Trip;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TripUpdateRequest(
    LocalDateTime departureTime,
    LocalDateTime arrivalEta,
    Trip.TripStatus status
) implements Serializable {}

