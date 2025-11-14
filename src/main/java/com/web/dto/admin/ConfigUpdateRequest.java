package com.web.dto.admin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public record ConfigUpdateRequest(
    Integer holdDurationMinutes,
    Integer noShowFeePercentage,
    Integer overbookingPercentage,
    Map<String, Integer> discountPercentages,  // "STUDENT": 20, etc
    BigDecimal baggageWeightLimit,
    BigDecimal baggagePricePerKg,
    
    // Configuraciones adicionales
    BigDecimal noShowFee,  // Para actualizar "no.show.fee"
    Double overbookingMaxPercentage,  // Para actualizar "overbooking.max.percentage"
    
    // Políticas de Reembolso
    BigDecimal refundPercentage48Hours,
    BigDecimal refundPercentage24Hours,
    BigDecimal refundPercentage12Hours,
    BigDecimal refundPercentage6Hours,
    BigDecimal refundPercentageLess6Hours,
    
    // Precios de Tickets
    BigDecimal ticketBasePrice,
    BigDecimal ticketPriceMultiplierPeakHours,
    BigDecimal ticketPriceMultiplierHighDemand,
    BigDecimal ticketPriceMultiplierMediumDemand
) implements Serializable {}

