package com.web.dto.parcel;

import com.web.entity.Parcel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ParcelStatusUpdateRequest(
    @NotBlank String code,
    @NotNull Parcel.ParcelStatus status,
    String otp,  // Para DELIVERED
    String proofPhotoUrl  // Para DELIVERED
) implements Serializable {}

