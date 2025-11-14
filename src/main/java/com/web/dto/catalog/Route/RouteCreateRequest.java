package com.web.dto.catalog.Route;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record RouteCreateRequest(
    @NotBlank String code,
    @NotBlank String name,
    @NotBlank String origin,
    @NotBlank String destination,
    @NotNull BigDecimal distanceKm,
    @NotNull Integer durationMin
) implements Serializable {}

