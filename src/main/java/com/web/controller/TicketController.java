package com.web.controller;

import com.web.dto.ticket.TicketCancelRequest;
import com.web.dto.ticket.TicketCancelResponse;
import com.web.dto.ticket.TicketCreateRequest;
import com.web.dto.ticket.TicketResponse;
import com.web.dto.ticket.reservations.SeatHoldRequest;
import com.web.dto.ticket.reservations.SeatHoldResponse;
import com.web.exception.BusinessException;
import com.web.repository.UserRepository;
import com.web.service.ticket.SeatHoldService;
import com.web.service.ticket.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SeatHoldService seatHoldService;
    private final UserRepository userRepository;

    // CASO DE USO 2: Hold de 10 minutos
    @PostMapping("/trips/{tripId}/seats/{seatNumber}/hold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeatHoldResponse> holdSeat(
            @PathVariable Long tripId,
            @PathVariable Integer seatNumber,
            @RequestBody @Valid SeatHoldRequest request) {

        SeatHoldResponse response = seatHoldService.createHold(tripId, seatNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // CASO DE USO 1 y 2: Compra con validación de tramo
    @PostMapping("/trips/{tripId}/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> purchaseTicket(
            @PathVariable Long tripId,
            @RequestBody @Valid TicketCreateRequest request) {

        // Validar que el tripId de la URL coincida con el del body (si viene)
        if (request.tripId() != null && !request.tripId().equals(tripId)) {
            throw new BusinessException(
                    "El tripId de la URL no coincide con el del body",
                    HttpStatus.BAD_REQUEST,
                    "TRIP_ID_MISMATCH");
        }

        // Crear un nuevo request usando SIEMPRE el tripId de la URL
        TicketCreateRequest validatedRequest = new TicketCreateRequest(
                tripId,
                request.passengerId(),
                request.seatNumber(),
                request.fromStopId(),
                request.fromStopName(),
                request.fromStopOrder(),
                request.toStopId(),
                request.toStopName(),
                request.toStopOrder(),
                request.price(),
                request.paymentMethod(),
                request.baggage());

        TicketResponse response = ticketService.purchaseTicket(validatedRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Política de reembolso
    @PostMapping("/tickets/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketCancelResponse> cancelTicket(
            @PathVariable Long id,
            @RequestBody(required = false) TicketCancelRequest request) {

        TicketCancelResponse response = ticketService.cancelTicket(id);
        return ResponseEntity.ok(response);
    }

    // Consulta de tickets por usuario
    @GetMapping("/tickets/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        TicketResponse response = ticketService.getTicketById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tickets/my-tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketResponse>> getMyTickets() {
        // Obtener userId del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Buscar el usuario por email para obtener su ID
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email))
                .getId();

        List<TicketResponse> response = ticketService.getUserTickets(userId);
        return ResponseEntity.ok(response);
    }
}
