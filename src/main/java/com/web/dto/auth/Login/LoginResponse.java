package com.web.dto.auth.Login;

import com.web.dto.auth.User.UserResponse;

import java.io.Serializable;

public record LoginResponse(
    String token,
    String tokenType,  // "Bearer"
    UserResponse user
) implements Serializable {}

