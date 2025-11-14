package com.web.dto.catalog.Stop;

import java.io.Serializable;
import java.math.BigDecimal;

public record StopResponse(
    Long id,
    String name,
    Integer order,
    BigDecimal latitude,
    BigDecimal longitude
) implements Serializable {}

