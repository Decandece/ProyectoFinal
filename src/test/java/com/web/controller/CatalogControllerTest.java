package com.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.catalog.Route.RouteCreateRequest;
import com.web.dto.catalog.Route.RouteDetailResponse;
import com.web.dto.catalog.Route.RouteResponse;
import com.web.dto.catalog.Route.RouteUpdateRequest;
import com.web.dto.catalog.Stop.StopCreateRequest;
import com.web.config.CustomUserDetailsService;
import com.web.exception.ResourceNotFoundException;
import com.web.service.catalog.RouteService;
import com.web.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class CatalogControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private RouteService routeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Verifica que cualquier usuario pueda consultar todas las rutas (público)
    @Test
    void getAllRoutes_shouldReturn200() throws Exception {
        var resp = List.of(new RouteResponse(
                1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                BigDecimal.valueOf(500.0), 360, true
        ));

        when(routeService.getAllRoutes()).thenReturn(resp);

        mvc.perform(get("/api/v1/routes")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Verifica que cualquier usuario pueda consultar una ruta por ID (público)
    @Test
    void getRouteById_shouldReturn200() throws Exception {
        var resp = new RouteDetailResponse(
                1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                BigDecimal.valueOf(500.0), 360, true, List.of()
        );

        when(routeService.getRouteById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/routes/1")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que retorne 404 cuando la ruta no existe
    @Test
    void getRouteById_shouldReturn404WhenNotFound() throws Exception {
        when(routeService.getRouteById(99L)).thenThrow(new ResourceNotFoundException("Ruta", 99L));

        mvc.perform(get("/api/v1/routes/99")
                        .with(anonymous()))
                .andExpect(status().isNotFound());
    }

    // Verifica que cualquier usuario pueda consultar una ruta con sus paradas (público)
    @Test
    void getRouteWithStops_shouldReturn200() throws Exception {
        var resp = new RouteDetailResponse(
                1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                BigDecimal.valueOf(500.0), 360, true, List.of()
        );

        when(routeService.getRouteById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/routes/1/stops")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que un ADMIN pueda crear una nueva ruta
    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoute_shouldReturn201() throws Exception {
        var req = new RouteCreateRequest(
                "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                BigDecimal.valueOf(500.0), 360
        );
        var resp = new RouteResponse(
                1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                BigDecimal.valueOf(500.0), 360, true
        );

        when(routeService.createRoute(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/routes").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que un ADMIN pueda actualizar los datos de una ruta
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoute_shouldReturn200() throws Exception {
        var req = new RouteUpdateRequest(
                "Bogotá - Cali", BigDecimal.valueOf(600.0), 420, null
        );
        var resp = new RouteResponse(
                1L, "R001", "Bogotá - Cali", "Bogotá", "Cali",
                BigDecimal.valueOf(600.0), 420, true
        );

        when(routeService.updateRoute(1L, req)).thenReturn(resp);

        mvc.perform(put("/api/v1/routes/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bogotá - Cali"));
    }

    // Verifica que un ADMIN pueda eliminar una ruta
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoute_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/routes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // Verifica que un ADMIN pueda agregar una parada a una ruta
    @Test
    @WithMockUser(roles = "ADMIN")
    void addStop_shouldReturn201() throws Exception {
        var req = new StopCreateRequest(1L, "Pereira", 2, null, null);
        var resp = new RouteDetailResponse(
                1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                BigDecimal.valueOf(500.0), 360, true, List.of()
        );

        when(routeService.addStop(1L, req)).thenReturn(resp);

        mvc.perform(post("/api/v1/routes/1/stops").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que un ADMIN pueda eliminar una parada de una ruta
    @Test
    @WithMockUser(roles = "ADMIN")
    void removeStop_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/routes/1/stops/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}

