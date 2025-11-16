package com.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.catalog.Bus.BusCreateRequest;
import com.web.dto.catalog.Bus.BusResponse;
import com.web.dto.catalog.Bus.BusUpdateRequest;
import com.web.entity.Bus;
import com.web.config.CustomUserDetailsService;
import com.web.exception.ResourceNotFoundException;
import com.web.service.catalog.BusService;
import com.web.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(BusController.class)
class BusControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private BusService busService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Verifica que un ADMIN pueda crear un bus
    @Test
    @WithMockUser(roles = "ADMIN")
    void createBus_shouldReturn201() throws Exception {
        var req = new BusCreateRequest("ABC123", 40, null);
        var resp = new BusResponse(1L, "ABC123", 40, null, Bus.BusStatus.ACTIVE);

        when(busService.createBus(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/buses").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }

    // Verifica que un DISPATCHER pueda consultar todos los buses
    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getAllBuses_shouldReturn200() throws Exception {
        var resp = List.of(new BusResponse(1L, "ABC123", 40, null, Bus.BusStatus.ACTIVE));

        when(busService.getAllBuses()).thenReturn(resp);

        mvc.perform(get("/api/v1/buses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Verifica que un DISPATCHER pueda consultar un bus por ID
    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getBusById_shouldReturn200() throws Exception {
        var resp = new BusResponse(1L, "ABC123", 40, null, Bus.BusStatus.ACTIVE);

        when(busService.getBusById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/buses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que retorne 404 cuando el bus no existe
    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getBusById_shouldReturn404WhenNotFound() throws Exception {
        when(busService.getBusById(99L)).thenThrow(new ResourceNotFoundException("Bus", 99L));

        mvc.perform(get("/api/v1/buses/99"))
                .andExpect(status().isNotFound());
    }

    // Verifica que un DISPATCHER pueda buscar un bus por placa
    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getBusByPlate_shouldReturn200() throws Exception {
        var resp = new BusResponse(1L, "ABC123", 40, null, Bus.BusStatus.ACTIVE);

        when(busService.getBusByPlate("ABC123")).thenReturn(resp);

        mvc.perform(get("/api/v1/buses/plate/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }

    // Verifica que un DISPATCHER pueda consultar buses disponibles por fecha
    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getAvailableBuses_shouldReturn200() throws Exception {
        var resp = List.of(new BusResponse(1L, "ABC123", 40, null, Bus.BusStatus.ACTIVE));

        when(busService.getAvailableBuses(any(LocalDate.class))).thenReturn(resp);

        mvc.perform(get("/api/v1/buses/available")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Verifica que un ADMIN pueda actualizar los datos de un bus
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBus_shouldReturn200() throws Exception {
        var req = new BusUpdateRequest(45, null, Bus.BusStatus.ACTIVE);
        var resp = new BusResponse(1L, "ABC123", 45, null, Bus.BusStatus.ACTIVE);

        when(busService.updateBus(1L, req)).thenReturn(resp);

        mvc.perform(put("/api/v1/buses/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(45));
    }

    // Verifica que un ADMIN pueda eliminar un bus
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBus_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/buses/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}

