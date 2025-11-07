package com.web.dto.dispatch;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record BoardingControlRequest(
    @NotNull Long tripId,
    @NotNull String action  // "OPEN" o "CLOSE"
) implements Serializable {}

