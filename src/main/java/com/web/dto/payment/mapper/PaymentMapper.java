package com.web.dto.payment.mapper;

import com.web.dto.payment.PaymentResponse;
import com.web.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    
    // Métodos helper para construir respuestas de pago
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "paymentMethod", source = "ticket.paymentMethod")
    @Mapping(target = "amount", source = "ticket.price")
    @Mapping(target = "paidAt", source = "ticket.purchasedAt")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "transactionReference", source = "transactionReference")
    PaymentResponse toPaymentResponse(Ticket ticket, String status, String transactionReference);
}

