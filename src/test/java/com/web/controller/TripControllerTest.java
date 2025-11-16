package com.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.trip.SeatStatusResponse;
import com.web.dto.trip.TripCreateRequest;
import com.web.dto.trip.TripDetailResponse;
import com.web.dto.trip.TripResponse;
import com.web.config.CustomUserDetailsService;
import com.web.entity.Trip;

import com.web.exception.ResourceNotFoundException;
import com.web.service.trip.TripService;
import com.web.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Verifica que cualquier usuario pueda buscar viajes por ruta y fecha (público)
    @Test
    void searchTrips_shouldReturn200() throws Exception {
        var resp = List.of(new TripResponse(
                1L, 1L, "Route Name", "Origin", "Destination",
                1L, "ABC123", 40,
                LocalDate.now(), LocalDateTime.now(), null,
                Trip.TripStatus.SCHEDULED, 0, 0.0
        ));

        when(tripService.searchTrips(1L, LocalDate.now())).thenReturn(resp);

        mvc.perform(get("/api/v1/trips")
                        .with(anonymous())
                        .param("routeId", "1")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Verifica validación cuando faltan parámetros de búsqueda
    @Test
    void searchTrips_shouldReturn400WhenMissingParams() throws Exception {
        mvc.perform(get("/api/v1/trips")
                        .with(anonymous()))
                .andExpect(status().isBadRequest());
    }

    // Verifica que cualquier usuario pueda consultar un viaje por ID (público)
    @Test
    void getTripById_shouldReturn200() throws Exception {
        var resp = new TripDetailResponse(
                1L, null, null,
                LocalDate.now(), LocalDateTime.now(), null,
                Trip.TripStatus.SCHEDULED, null, 0, 40, 0.0, List.of()
        );

        when(tripService.getTripById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/trips/1")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que retorne 404 cuando el viaje no existe
    @Test
    void getTripById_shouldReturn404WhenNotFound() throws Exception {
        when(tripService.getTripById(99L)).thenThrow(new ResourceNotFoundException("Viaje", 99L));

        mvc.perform(get("/api/v1/trips/99")
                        .with(anonymous()))
                .andExpect(status().isNotFound());
    }

    // Verifica que cualquier usuario pueda consultar disponibilidad de asientos (público)
    @Test
    void getSeatAvailability_shouldReturn200() throws Exception {
        var resp = List.of(new SeatStatusResponse(1, true, "AVAILABLE"));

        when(tripService.getSeatAvailability(1L, 1L, 2L)).thenReturn(resp);

        mvc.perform(get("/api/v1/trips/1/seats")
                        .with(anonymous())
                        .param("fromStopId", "1")
                        .param("toStopId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value(1));
    }

    // Verifica que un ADMIN pueda crear un nuevo viaje
    @Test
    @WithMockUser(roles = "ADMIN")
    void createTrip_shouldReturn201() throws Exception {
        var req = new TripCreateRequest(
                1L, 1L, LocalDate.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(8),
                LocalDateTime.now().plusDays(1).plusHours(12)
        );
        var resp = new TripResponse(
                1L, 1L, "Route Name", "Origin", "Destination",
                1L, "ABC123", 40,
                LocalDate.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(8), null,
                Trip.TripStatus.SCHEDULED, 0, 0.0
        );

        when(tripService.createTrip(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/trips")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que un ADMIN pueda actualizar el estado de un viaje
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTripStatus_shouldReturn200() throws Exception {
        var resp = new TripResponse(
                1L, 1L, "Route Name", "Origin", "Destination",
                1L, "ABC123", 40,
                LocalDate.now(), LocalDateTime.now(), null,
                Trip.TripStatus.BOARDING, 0, 0.0
        );

        when(tripService.updateTripStatus(1L, Trip.TripStatus.BOARDING)).thenReturn(resp);

        mvc.perform(put("/api/v1/trips/1/status")
                        .with(csrf())
                        .param("status", "BOARDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOARDING"));
    }

    // Verifica que un ADMIN pueda cancelar un viaje
    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelTrip_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/trips/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}

