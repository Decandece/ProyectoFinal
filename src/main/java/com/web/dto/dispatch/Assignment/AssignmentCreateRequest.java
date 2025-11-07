package com.web.dto.dispatch.Assignment;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record AssignmentCreateRequest(
    @NotNull Long tripId,
    @NotNull Long driverId,
    @NotNull Long dispatcherId
) implements Serializable {}

