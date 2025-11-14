package com.web.controller;

import com.web.dto.admin.ConfigResponse;
import com.web.dto.admin.ConfigUpdateRequest;
import com.web.dto.admin.MetricsResponse;
import com.web.repository.UserRepository;
import com.web.service.admin.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ConfigService configService;
    private final UserRepository userRepository;

    @GetMapping("/config")
    public ResponseEntity<ConfigResponse> getConfig() {
        ConfigResponse response = configService.getConfig();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/config")
    public ResponseEntity<ConfigResponse> updateConfig(@Valid @RequestBody ConfigUpdateRequest request) {
        Long userId = getCurrentUserId();
        ConfigResponse response = configService.updateConfig(request, userId);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        // Obtener userId del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Buscar el usuario por email para obtener su ID
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email))
                .getId();
    }
}
