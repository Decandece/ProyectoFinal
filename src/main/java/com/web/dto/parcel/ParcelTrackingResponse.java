package com.web.dto.parcel;

import com.web.entity.Parcel;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record ParcelTrackingResponse(
    String code,
    Parcel.ParcelStatus status,
    String currentLocation,  // Nombre de parada actual
    LocalDateTime estimatedDelivery,
    List<ParcelEventResponse> events  // Historial
) implements Serializable {}

