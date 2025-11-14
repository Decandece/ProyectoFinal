package com.web.dto.catalog.Stop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record StopCreateRequest(
    @NotNull Long routeId,
    @NotBlank String name,
    @NotNull Integer order,
    BigDecimal latitude,
    BigDecimal longitude
) implements Serializable {}

