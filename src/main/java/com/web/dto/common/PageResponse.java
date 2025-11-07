package com.web.dto.common;

import java.io.Serializable;
import java.util.List;

// Para paginación
public record PageResponse<T>(
    List<T> content,
    Integer pageNumber,
    Integer pageSize,
    Long totalElements,
    Integer totalPages,
    Boolean last
) implements Serializable {}

