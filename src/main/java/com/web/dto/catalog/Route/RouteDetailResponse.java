package com.web.dto.catalog.Route;

import com.web.dto.catalog.Stop.StopResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record RouteDetailResponse(
        Long id,
        String code,
        String name,
        String origin,
        String destination,
        BigDecimal distanceKm,
        Integer durationMin,
        Boolean isActive,
        List<StopResponse> stops  // Ordenados por order
) implements Serializable {}

