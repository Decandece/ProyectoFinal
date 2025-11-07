package com.web.dto.dispatch.Assignment;

import java.io.Serializable;

public record AssignmentUpdateRequest(
    Long driverId,
    Boolean checklistOk
) implements Serializable {}

