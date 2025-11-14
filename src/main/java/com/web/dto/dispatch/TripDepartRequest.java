package com.web.dto.dispatch;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record TripDepartRequest(
    @NotNull Long tripId,
    @NotNull Boolean checklistCompleted,
    String notes
) implements Serializable {}

