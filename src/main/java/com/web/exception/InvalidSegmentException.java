package com.web.exception;

import org.springframework.http.HttpStatus;

public class InvalidSegmentException extends BusinessException {
    public InvalidSegmentException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_SEGMENT");
    }

    public InvalidSegmentException(String fromStop, String toStop) {
        super(String.format("El tramo de '%s' a '%s' no es válido", fromStop, toStop),
              HttpStatus.BAD_REQUEST, "INVALID_SEGMENT");
    }
}

