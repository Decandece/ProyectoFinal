package com.web.exception;

import org.springframework.http.HttpStatus;

public class SeatNotAvailableException extends BusinessException {
    public SeatNotAvailableException(String message) {
        super(message, HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE");
    }

    public SeatNotAvailableException(Integer seatNumber, Long tripId) {
        super(String.format("El asiento %d no está disponible para el viaje %d", seatNumber, tripId),
              HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE");
    }
}

