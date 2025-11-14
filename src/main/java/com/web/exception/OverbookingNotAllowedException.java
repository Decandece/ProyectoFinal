package com.web.exception;

import org.springframework.http.HttpStatus;

public class OverbookingNotAllowedException extends BusinessException {
    public OverbookingNotAllowedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "OVERBOOKING_NOT_ALLOWED");
    }

    public OverbookingNotAllowedException() {
        super("El viaje ha alcanzado el límite de overbooking permitido",
              HttpStatus.FORBIDDEN, "OVERBOOKING_NOT_ALLOWED");
    }
}

