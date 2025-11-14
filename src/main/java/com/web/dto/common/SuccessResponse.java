package com.web.dto.common;

import java.io.Serializable;

// Respuesta genérica de éxito
public record SuccessResponse(
    String message,
    Object data
) implements Serializable {}

