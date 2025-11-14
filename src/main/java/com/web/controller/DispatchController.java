package com.web.controller;

import com.web.dto.dispatch.Assignment.AssignmentCreateRequest;
import com.web.dto.dispatch.Assignment.AssignmentResponse;
import com.web.dto.trip.TripResponse;
import com.web.exception.BusinessException;
import com.web.service.dispatch.AssignmentService;
import com.web.service.dispatch.BoardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips/{tripId}")
@RequiredArgsConstructor
public class DispatchController {

    private final AssignmentService assignmentService;
    private final BoardingService boardingService;

    @PostMapping("/assign")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<AssignmentResponse> assignTrip(
            @PathVariable Long tripId,
            @RequestBody @Valid AssignmentCreateRequest request) {

        // Validar que el tripId de la URL coincida con el del body (si viene)
        if (request.tripId() != null && !request.tripId().equals(tripId)) {
            throw new BusinessException(
                    "El tripId de la URL no coincide con el del body",
                    HttpStatus.BAD_REQUEST,
                    "TRIP_ID_MISMATCH");
        }


        AssignmentCreateRequest validatedRequest = new AssignmentCreateRequest(
                tripId, // Usar siempre el tripId de la URL
                request.driverId(),
                request.dispatcherId());

        AssignmentResponse response = assignmentService.assignTrip(validatedRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/boarding/{action}")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> controlBoarding(
            @PathVariable Long tripId,
            @PathVariable String action) {

        if (!action.equals("open") && !action.equals("close")) {
            throw new BusinessException("Action debe ser 'open' o 'close'",
                    HttpStatus.BAD_REQUEST, "INVALID_ACTION");
        }

        TripResponse response = action.equals("open")
                ? boardingService.openBoarding(tripId)
                : boardingService.closeBoarding(tripId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/depart")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<TripResponse> departTrip(
            @PathVariable Long tripId) {

        TripResponse response = boardingService.departTrip(tripId);
        return ResponseEntity.ok(response);
    }
}
