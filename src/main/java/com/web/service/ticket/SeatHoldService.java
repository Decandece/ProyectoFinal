package com.web.service.ticket;

import com.web.dto.ticket.reservations.SeatHoldCreateRequest;
import com.web.dto.ticket.reservations.SeatHoldRequest;
import com.web.dto.ticket.reservations.SeatHoldResponse;

import java.util.List;
import java.util.Optional;

public interface SeatHoldService {
    
    SeatHoldResponse createHold(SeatHoldCreateRequest request, Long userId);
    
    // Método sobrecargado para controller con tripId y seatNumber separados
    SeatHoldResponse createHold(Long tripId, Integer seatNumber, SeatHoldRequest request);
    
    boolean hasActiveHold(Long tripId, Integer seatNumber);
    
    void releaseHold(Long holdId);
    
    Optional<SeatHoldResponse> findUserActiveHold(Long tripId, Integer seatNumber, Long userId);
    
    List<SeatHoldResponse> getActiveHoldsByTrip(Long tripId);
    
    List<SeatHoldResponse> getUserActiveHolds(Long userId);
}

