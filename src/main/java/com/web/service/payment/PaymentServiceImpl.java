package com.web.service.payment;

import com.web.dto.payment.CashCloseRequest;
import com.web.dto.payment.CashCloseResponse;
import com.web.dto.payment.PaymentConfirmRequest;
import com.web.dto.ticket.TicketResponse;
import com.web.dto.ticket.mapper.TicketMapper;
import com.web.entity.Ticket;
import com.web.entity.User;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.TicketRepository;
import com.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public TicketResponse confirmPayment(PaymentConfirmRequest request) {
        Ticket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", request.ticketId()));

        if (ticket.getStatus() != Ticket.TicketStatus.SOLD) {
            throw new IllegalStateException("El ticket no está en estado SOLD");
        }

        ticket.setPaymentMethod(request.paymentMethod());
        Ticket updatedTicket = ticketRepository.save(ticket);



        return ticketMapper.toResponse(updatedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public CashCloseResponse closeCash(CashCloseRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        // Buscar tickets de efectivo del día
        List<Ticket> cashTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getPaymentMethod() == Ticket.PaymentMethod.CASH)
                .filter(t -> t.getTrip().getTripDate().equals(request.date()))
                .toList();

        BigDecimal totalCash = cashTickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int ticketCount = cashTickets.size();
        BigDecimal difference = request.actualAmount().subtract(request.expectedAmount());


        return new CashCloseResponse(
                userId, // Usar el userId como ID del cierre
                user.getName(),
                request.date(),
                request.expectedAmount(),
                request.actualAmount(),
                difference,
                ticketCount,
                LocalDateTime.now()
        );
    }
}
