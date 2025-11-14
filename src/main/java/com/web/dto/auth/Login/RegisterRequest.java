package com.web.dto.auth.Login;

import com.web.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record RegisterRequest(
    @NotBlank String name,
    @Email String email,
    @NotBlank String phone,
    @NotBlank String password,
    User.Role role  // Default PASSENGER en servicio
) implements Serializable {}

