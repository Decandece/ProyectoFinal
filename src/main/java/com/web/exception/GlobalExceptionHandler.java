package com.web.exception;

import com.web.dto.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja excepciones cuando no se encuentra un recurso (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja excepciones cuando un asiento no está disponible (409)
    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSeatNotAvailable(SeatNotAvailableException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja excepciones cuando un tramo de viaje no es válido (400)
    @ExceptionHandler(InvalidSegmentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSegment(InvalidSegmentException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja excepciones cuando se excede el límite de overbooking (403)
    @ExceptionHandler(OverbookingNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleOverbookingNotAllowed(OverbookingNotAllowedException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja excepciones cuando una transición de estado no es permitida (422)
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransition(InvalidStateTransitionException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja excepciones cuando se intenta registrar un email duplicado (409)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja excepciones cuando las credenciales de autenticación son inválidas (401)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja cualquier excepción de negocio genérica (catch-all para BusinessException)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Maneja errores de validación de datos (cuando @Valid falla) (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación en los datos enviados",
                LocalDateTime.now(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Maneja cualquier excepción no capturada (error genérico del servidor) (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Error interno del servidor: " + ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

