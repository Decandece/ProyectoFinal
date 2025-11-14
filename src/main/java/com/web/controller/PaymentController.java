package com.web.controller;

import com.web.dto.payment.CashCloseRequest;
import com.web.dto.payment.CashCloseResponse;
import com.web.dto.payment.PaymentConfirmRequest;
import com.web.repository.UserRepository;
import com.web.dto.ticket.TicketResponse;
import com.web.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;
    
    @PostMapping("/payments/confirm")
    @PreAuthorize("hasRole('CLERK')")
    public ResponseEntity<TicketResponse> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request) {
        TicketResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cash/close")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER')")
    public ResponseEntity<CashCloseResponse> closeCash(@Valid @RequestBody CashCloseRequest request) {
        // Obtener userId del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Buscar el usuario por email para obtener su ID
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email))
                .getId();

        CashCloseResponse response = paymentService.closeCash(request, userId);
        return ResponseEntity.ok(response);
    }
}

