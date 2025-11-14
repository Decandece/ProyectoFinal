package com.web.dto.admin;

import java.io.Serializable;

public record OperationalMetrics(
    Double onTimeDepartureRate,
    Double onTimeArrivalRate,
    Double noShowRate,
    Integer totalCancellations,
    Integer totalIncidents
) implements Serializable {}

