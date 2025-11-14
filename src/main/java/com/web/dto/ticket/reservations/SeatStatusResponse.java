package com.web.dto.ticket.reservations;

import java.io.Serializable;

public record SeatStatusResponse(
    Integer seatNumber,
    String status,  // "AVAILABLE", "SOLD", "HELD"
    String seatType  // De Seat entity
) implements Serializable {}

