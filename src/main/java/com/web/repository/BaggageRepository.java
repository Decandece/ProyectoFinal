package com.web.repository;

import com.web.entity.Baggage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BaggageRepository extends JpaRepository<Baggage, Long> {

    // Buscar equipaje por ticket
    Optional<Baggage> findByTicketId(Long ticketId);

    // Buscar equipaje por código de etiqueta
    Optional<Baggage> findByTagCode(String tagCode);

    // Buscar equipajes por viaje
    @Query("""
        SELECT b FROM Baggage b
        WHERE b.ticket.trip.id = :tripId
        ORDER BY b.ticket.seatNumber
    """)
    List<Baggage> findByTripId(@Param("tripId") Long tripId);

    // Calcular total de tarifas de exceso para un viaje
    @Query("""
        SELECT SUM(b.excessFee)
        FROM Baggage b
        WHERE b.ticket.trip.id = :tripId
    """)
    BigDecimal calculateTotalExcessFees(@Param("tripId") Long tripId);

    // Calcular ingresos por exceso de equipaje por rango de fechas
    @Query("""
        SELECT SUM(b.excessFee)
        FROM Baggage b
        WHERE b.ticket.trip.tripDate BETWEEN :startDate AND :endDate
        AND b.excessFee > 0
    """)
    BigDecimal calculateExcessBaggageRevenue(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}