package com.web.dto.auth.User;

import com.web.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String name,
    String email,
    String phone,
    User.Role role,
    User.Status status,
    LocalDateTime createdAt
) implements Serializable {}

