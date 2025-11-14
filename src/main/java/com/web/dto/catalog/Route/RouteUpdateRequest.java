package com.web.dto.catalog.Route;

import java.io.Serializable;
import java.math.BigDecimal;

public record RouteUpdateRequest(
    String name,
    BigDecimal distanceKm,
    Integer durationMin,
    Boolean isActive
) implements Serializable {}

