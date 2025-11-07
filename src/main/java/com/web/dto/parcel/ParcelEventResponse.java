package com.web.dto.parcel;

import com.web.entity.Parcel;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ParcelEventResponse(
    Parcel.ParcelStatus status,
    String location,
    LocalDateTime timestamp,
    String notes
) implements Serializable {}

