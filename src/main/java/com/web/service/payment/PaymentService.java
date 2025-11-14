package com.web.service.payment;

import com.web.dto.payment.CashCloseRequest;
import com.web.dto.payment.CashCloseResponse;
import com.web.dto.payment.PaymentConfirmRequest;
import com.web.dto.ticket.TicketResponse;

public interface PaymentService {
    
    TicketResponse confirmPayment(PaymentConfirmRequest request);
    
    CashCloseResponse closeCash(CashCloseRequest request, Long userId);
}

