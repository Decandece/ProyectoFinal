package com.web.dto.admin;

import java.io.Serializable;

public record OccupancyMetrics(
    Double averageOccupancy,
    Double p50Occupancy,
    Double p95Occupancy,
    Integer totalTrips,
    Integer totalSeatsSold
) implements Serializable {}

