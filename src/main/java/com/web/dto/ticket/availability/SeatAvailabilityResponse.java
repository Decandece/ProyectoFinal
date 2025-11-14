package com.web.dto.ticket.availability;

import com.web.dto.ticket.reservations.SeatStatusResponse;

import java.io.Serializable;
import java.util.List;

public record SeatAvailabilityResponse(
    Long tripId,
    Integer busCapacity,
    List<SeatStatusResponse> seats
) implements Serializable {}

