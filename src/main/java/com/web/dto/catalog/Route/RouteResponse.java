package com.web.dto.catalog.Route;

import java.io.Serializable;
import java.math.BigDecimal;

public record RouteResponse(
    Long id,
    String code,
    String name,
    String origin,
    String destination,
    BigDecimal distanceKm,
    Integer durationMin,
    Boolean isActive
) implements Serializable {}

