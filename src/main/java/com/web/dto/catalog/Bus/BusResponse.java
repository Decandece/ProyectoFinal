package com.web.dto.catalog.Bus;

import com.web.entity.Bus;

import java.io.Serializable;
import java.util.Map;

public record BusResponse(
    Long id,
    String plate,
    Integer capacity,
    Map<String, Object> amenities,  // JSON
    Bus.BusStatus status
) implements Serializable {}

