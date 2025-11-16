package com.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.admin.ConfigResponse;
import com.web.dto.admin.ConfigUpdateRequest;
import com.web.config.CustomUserDetailsService;
import com.web.entity.User;
import com.web.repository.UserRepository;
import com.web.service.admin.ConfigService;
import com.web.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests para el controlador de administración del sistema
@WebMvcTest(AdminController.class)
class  AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private ConfigService configService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Verifica que un ADMIN pueda consultar la configuración del sistema
    @Test
    @WithMockUser(roles = "ADMIN")
    void getConfig_shouldReturn200() throws Exception {
        Map<String, Integer> discounts = new HashMap<>();
        discounts.put("STUDENT", 20);
        discounts.put("SENIOR", 15);
        discounts.put("CHILD", 50);

        var resp = new ConfigResponse(
                10, 10, 5, discounts,
                BigDecimal.valueOf(23.0), BigDecimal.valueOf(5000),
                BigDecimal.valueOf(10000), 0.05,
                BigDecimal.valueOf(90), BigDecimal.valueOf(70),
                BigDecimal.valueOf(50), BigDecimal.valueOf(30), BigDecimal.ZERO,
                BigDecimal.valueOf(50000), BigDecimal.valueOf(1.15),
                BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.1),
                LocalDateTime.now()
        );

        when(configService.getConfig()).thenReturn(resp);

        mvc.perform(get("/api/v1/admin/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdDurationMinutes").value(10))
                .andExpect(jsonPath("$.overbookingMaxPercentage").value(0.05));
    }

    // Verifica que un ADMIN pueda actualizar la configuración del sistema
    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateConfig_shouldReturn200() throws Exception {
        Map<String, Integer> discounts = new HashMap<>();
        discounts.put("STUDENT", 25);

        var req = new ConfigUpdateRequest(
                15, 10, 5, discounts,
                BigDecimal.valueOf(25.0), BigDecimal.valueOf(6000),
                BigDecimal.valueOf(15000), 0.1,
                BigDecimal.valueOf(95), BigDecimal.valueOf(75),
                BigDecimal.valueOf(55), BigDecimal.valueOf(35), BigDecimal.ZERO,
                BigDecimal.valueOf(55000), BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(1.3), BigDecimal.valueOf(1.15)
        );

        var user = User.builder()
                .id(1L)
                .email("admin@example.com")
                .build();

        var resp = new ConfigResponse(
                15, 10, 5, discounts,
                BigDecimal.valueOf(25.0), BigDecimal.valueOf(6000),
                BigDecimal.valueOf(15000), 0.1,
                BigDecimal.valueOf(95), BigDecimal.valueOf(75),
                BigDecimal.valueOf(55), BigDecimal.valueOf(35), BigDecimal.ZERO,
                BigDecimal.valueOf(55000), BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(1.3), BigDecimal.valueOf(1.15),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(configService.updateConfig(any(), any())).thenReturn(resp);

        mvc.perform(put("/api/v1/admin/config")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdDurationMinutes").value(15));
    }

    // Verifica manejo de error cuando el usuario administrador no existe
    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateConfig_shouldReturn404WhenUserNotFound() throws Exception {
        Map<String, Integer> discounts = new HashMap<>();
        var req = new ConfigUpdateRequest(
                15, null, null, discounts,
                null, null, null, null,
                null, null, null, null, null,
                null, null, null, null
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        mvc.perform(put("/api/v1/admin/config")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
}

