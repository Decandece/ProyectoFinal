package com.web.dto.admin;

import java.io.Serializable;
import java.util.Map;

// Analisis de paquetes
public record ParcelMetrics(
                Integer totalParcels, // Total de paquetes
                Integer delivered,
                Integer failed,
                Double deliverySuccessRate,
                Map<String, Integer> parcelsByRoute // paquetes por ruta
) implements Serializable {
}
