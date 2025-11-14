package com.web.service.ticket;

import com.web.dto.baggage.BaggageCreateRequest;
import com.web.dto.ticket.TicketCancelResponse;
import com.web.dto.ticket.TicketCreateRequest;
import com.web.dto.ticket.TicketResponse;
import com.web.dto.ticket.mapper.TicketMapper;
import com.web.entity.*;
import com.web.exception.InvalidSegmentException;
import com.web.exception.ResourceNotFoundException;
import com.web.exception.SeatNotAvailableException;
import com.web.repository.*;
import com.web.service.admin.ConfigService;
import com.web.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;
    private final FareRuleRepository fareRuleRepository;
    private final BaggageRepository baggageRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final TicketMapper ticketMapper;
    private final SeatHoldService seatHoldService;
    private final QrCodeGenerator qrCodeGenerator;
    private final ConfigService configService;

    @Override
    @Transactional
    public TicketResponse purchaseTicket(TicketCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new ResourceNotFoundException("Viaje", request.tripId()));

        if (trip.getStatus() != Trip.TripStatus.SCHEDULED) {
            throw new InvalidSegmentException("El viaje no está disponible para compra (estado: " + trip.getStatus() + ")");
        }

        User passenger = userRepository.findById(request.passengerId())
                .orElseThrow(() -> new ResourceNotFoundException("Pasajero", request.passengerId()));

        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Parada de origen", request.fromStopId()));
        Stop toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Parada de destino", request.toStopId()));

        validateSegment(trip, fromStop, toStop);

        Optional<SeatHold> activeHold = seatHoldRepository.findActiveHold(
                request.tripId(),
                request.seatNumber(),
                now
        );

        if (activeHold.isPresent() && !activeHold.get().getUser().getId().equals(passenger.getId())) {
            throw new SeatNotAvailableException(
                    "El asiento " + request.seatNumber() + " tiene un hold activo de otro usuario");
        }

        Boolean isSeatAvailable = ticketRepository.isSeatAvailableForSegment(
                request.tripId(),
                request.seatNumber(),
                request.fromStopId().intValue(),
                request.toStopId().intValue()
        );

        if (!isSeatAvailable) {
            throw new SeatNotAvailableException(
                    "El asiento " + request.seatNumber() + " no está disponible para el tramo seleccionado");
        }

        BigDecimal finalPrice = calculateFinalPrice(trip, fromStop, toStop, request);

        Ticket ticket = ticketMapper.toEntity(request);
        // Establecer las relaciones manualmente
        ticket.setTrip(trip);
        ticket.setPassenger(passenger);
        ticket.setFromStop(fromStop);
        ticket.setToStop(toStop);
        ticket.setPrice(finalPrice);
        ticket.setQrCode(qrCodeGenerator.generateTicketQr());
        ticket = ticketRepository.save(ticket);

        if (request.baggage() != null) {
            BaggageCreateRequest baggageReq = request.baggage();
            
            Baggage baggage = Baggage.builder()
                    .ticket(ticket)
                    .weightKg(baggageReq.weightKg())
                    .tagCode(qrCodeGenerator.generateBaggageTag())
                    .build();

            Double baggageWeightLimit = configService.getBaggageWeightLimit();
            double weightKgDouble = baggageReq.weightKg().doubleValue();
            if (weightKgDouble > baggageWeightLimit) {
                double excess = weightKgDouble - baggageWeightLimit;
                BigDecimal excessFeePerKg = configService.getExcessFeePerKg();
                BigDecimal excessFee = excessFeePerKg.multiply(BigDecimal.valueOf(excess));
                baggage.setExcessFee(excessFee.setScale(2, RoundingMode.HALF_UP));

            } else {
                baggage.setExcessFee(BigDecimal.ZERO);
            }

            baggageRepository.save(baggage);

        }

        if (activeHold.isPresent() && activeHold.get().getUser().getId().equals(passenger.getId())) {
            seatHoldService.releaseHold(activeHold.get().getId());

        }

        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketCancelResponse cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != Ticket.TicketStatus.SOLD) {
            throw new InvalidSegmentException("El ticket ya está cancelado o es no-show");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getTrip().getDepartureTime();
        
        Duration timeUntilDeparture = Duration.between(now, departureTime);
        long hoursUntilDeparture = timeUntilDeparture.toHours();

        BigDecimal refundPercentage = calculateRefundPercentage(hoursUntilDeparture);
        BigDecimal refundAmount = ticket.getPrice()
                .multiply(refundPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        ticket.setStatus(Ticket.TicketStatus.CANCELLED);
        ticketRepository.save(ticket);



        return new TicketCancelResponse(
                ticketId,
                ticket.getStatus(),
                refundAmount,
                refundPercentage.intValue(),
                "Ticket cancelado exitosamente"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getUserTickets(Long userId) {
        List<Ticket> tickets = ticketRepository.findByPassengerId(userId);
        return ticketMapper.toResponseList(tickets);
    }

    @Scheduled(cron = "0 */5 * * * *") //Cada 5 minutos HOLD
    @Transactional
    public void processNoShows() {
        LocalDateTime fiveMinutesFromNow = LocalDateTime.now().plusMinutes(5);
        

        List<Ticket> allTickets = ticketRepository.findAll();
        int noShowCount = 0;
        
        for (Ticket ticket : allTickets) {
            if (ticket.getStatus() == Ticket.TicketStatus.SOLD &&
                ticket.getTrip().getDepartureTime().isBefore(fiveMinutesFromNow) &&
                ticket.getTrip().getDepartureTime().isAfter(LocalDateTime.now())) {
                ticket.setStatus(Ticket.TicketStatus.NO_SHOW);
                ticketRepository.save(ticket);
                noShowCount++;

            }
        }
        

    }

    private void validateSegment(Trip trip, Stop fromStop, Stop toStop) {
        if (!fromStop.getRoute().getId().equals(trip.getRoute().getId()) ||
            !toStop.getRoute().getId().equals(trip.getRoute().getId())) {
            throw new InvalidSegmentException("Las paradas no pertenecen a la ruta del viaje");
        }

        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new InvalidSegmentException("La parada de origen debe ser anterior a la de destino");
        }
    }

    private BigDecimal calculateFinalPrice(Trip trip, Stop fromStop, Stop toStop, TicketCreateRequest request) {
        // Precio base: primero busca en FareRule, si no existe usa ConfigService
        BigDecimal basePrice = fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(
                trip.getRoute().getId(),
                fromStop.getId(),
                toStop.getId()
        ).map(fareRule -> fareRule.getBasePrice())
         .orElse(configService.getTicketBasePrice());

        Long soldTickets = ticketRepository.countSoldTicketsInRange(
                trip.getTripDate(),
                trip.getTripDate()
        );
        int capacity = trip.getBus().getCapacity();
        double occupancyRate = (double) soldTickets / capacity;

        BigDecimal dynamicMultiplier = BigDecimal.ONE;
        if (occupancyRate > 0.8) {
            dynamicMultiplier = configService.getTicketPriceMultiplierHighDemand();
        } else if (occupancyRate > 0.6) {
            dynamicMultiplier = configService.getTicketPriceMultiplierMediumDemand();
        }

        LocalDateTime departureTime = trip.getDepartureTime();
        int hour = departureTime.getHour();
        if ((hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 20)) {
            dynamicMultiplier = dynamicMultiplier.multiply(
                configService.getTicketPriceMultiplierPeakHours()
            );
        }

        BigDecimal finalPrice = basePrice.multiply(dynamicMultiplier)
                .setScale(2, RoundingMode.HALF_UP);


        return finalPrice;
    }

    private BigDecimal calculateRefundPercentage(long hoursUntilDeparture) {
        if (hoursUntilDeparture >= 48) {
            return configService.getRefundPercentage48Hours();
        } else if (hoursUntilDeparture >= 24) {
            return configService.getRefundPercentage24Hours();
        } else if (hoursUntilDeparture >= 12) {
            return configService.getRefundPercentage12Hours();
        } else if (hoursUntilDeparture >= 6) {
            return configService.getRefundPercentage6Hours();
        } else {
            return configService.getRefundPercentageLess6Hours();
        }
    }
}

