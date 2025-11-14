package com.web.dto.admin;

import java.io.Serializable;
import java.util.Map;

public record ParcelMetrics(
    Integer totalParcels,
    Integer delivered,
    Integer failed,
    Double deliverySuccessRate,
    Map<String, Integer> parcelsByRoute
) implements Serializable {}

