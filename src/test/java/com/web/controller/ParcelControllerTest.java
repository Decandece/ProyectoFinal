package com.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.parcel.ParcelCreateRequest;
import com.web.dto.parcel.ParcelResponse;
import com.web.dto.parcel.ParcelStatusUpdateRequest;
import com.web.entity.Parcel;
import com.web.config.CustomUserDetailsService;

import com.web.exception.ResourceNotFoundException;
import com.web.service.parcel.ParcelService;
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


@WebMvcTest(ParcelController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParcelControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private ParcelService parcelService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Verifica que un CLERK pueda consultar todas las encomiendas
    @Test
    @WithMockUser(roles = "CLERK")
    void getAllParcels_shouldReturn200() throws Exception {
        var resp = List.of(new ParcelResponse(
                1L, "PARCEL001", 1L, "Route Name", null,
                "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                null, null, Parcel.ParcelStatus.IN_TRANSIT,
                "123456", null, null, null
        ));

        when(parcelService.getAllParcels()).thenReturn(resp);

        mvc.perform(get("/api/v1/parcels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Verifica que un CLERK pueda crear una encomienda
    @Test
    @WithMockUser(roles = "CLERK")
    void createParcel_shouldReturn201() throws Exception {
        var req = new ParcelCreateRequest(
                1L, "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10.0), "Description"
        );
        var resp = new ParcelResponse(
                1L, "PARCEL001", 1L, "Route Name", null,
                "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                null, null, Parcel.ParcelStatus.IN_TRANSIT,
                "123456", null, null, null
        );

        when(parcelService.createParcel(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/parcels").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Verifica que cualquier usuario pueda rastrear una encomienda por código (público)
    @Test
    void trackParcel_shouldReturn200() throws Exception {
        var resp = new ParcelResponse(
                1L, "PARCEL001", 1L, "Route Name", null,
                "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                null, null, Parcel.ParcelStatus.IN_TRANSIT,
                "123456", null, null, null
        );

        when(parcelService.trackParcel("PARCEL001")).thenReturn(resp);

        mvc.perform(get("/api/v1/parcels/PARCEL001/track")
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARCEL001"));
    }

    // Verifica que retorne 404 cuando la encomienda no existe
    @Test
    void trackParcel_shouldReturn404WhenNotFound() throws Exception {
        when(parcelService.trackParcel("INVALID")).thenThrow(new ResourceNotFoundException("Encomienda con código: INVALID"));

        mvc.perform(get("/api/v1/parcels/INVALID/track")
                        .with(anonymous()))
                .andExpect(status().isNotFound());
    }

    // Verifica que un DRIVER pueda entregar una encomienda con OTP y foto
    @Test
    @WithMockUser(roles = "DRIVER")
    void deliverParcel_shouldReturn200() throws Exception {
        var trackResp = new ParcelResponse(
                1L, "PARCEL001", 1L, "Route Name", null,
                "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                null, null, Parcel.ParcelStatus.IN_TRANSIT,
                "123456", null, null, null
        );
        var deliverResp = new ParcelResponse(
                1L, "PARCEL001", 1L, "Route Name", null,
                "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                null, null, Parcel.ParcelStatus.DELIVERED,
                "123456", "photo.jpg", null, null
        );
        var req = new ParcelStatusUpdateRequest("PARCEL001", Parcel.ParcelStatus.DELIVERED, "123456", "photo.jpg");

        when(parcelService.trackParcel("PARCEL001")).thenReturn(trackResp);
        when(parcelService.deliverWithOtp(1L, "123456", "photo.jpg")).thenReturn(deliverResp);

        mvc.perform(post("/api/v1/parcels/PARCEL001/deliver").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    // Verifica validación cuando faltan OTP o foto en la entrega
    @Test
    @WithMockUser(roles = "DRIVER")
    void deliverParcel_shouldReturn400WhenMissingOtp() throws Exception {
        var req = new ParcelStatusUpdateRequest("PARCEL001", Parcel.ParcelStatus.DELIVERED, null, "photo.jpg");

        mvc.perform(post("/api/v1/parcels/PARCEL001/deliver").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // Verifica que un CLERK pueda actualizar el estado de una encomienda
    @Test
    @WithMockUser(roles = "CLERK")
    void updateParcelStatus_shouldReturn200() throws Exception {
        var trackResp = new ParcelResponse(
                1L, "PARCEL001", 1L, "Route Name", null,
                "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                null, null, Parcel.ParcelStatus.IN_TRANSIT,
                "123456", null, null, null
        );
        var updateResp = new ParcelResponse(
                1L, "PARCEL001", 1L, "Route Name", null,
                "Sender", "123456789", "Receiver", "987654321",
                1L, "Origin", 2L, "Destination",
                null, null, Parcel.ParcelStatus.DELIVERED,
                "123456", null, null, null
        );
        var req = new ParcelStatusUpdateRequest("PARCEL001", Parcel.ParcelStatus.DELIVERED, null, null);

        when(parcelService.trackParcel("PARCEL001")).thenReturn(trackResp);
        when(parcelService.updateStatus(1L, Parcel.ParcelStatus.DELIVERED)).thenReturn(updateResp);

        mvc.perform(put("/api/v1/parcels/PARCEL001/status").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }
}

