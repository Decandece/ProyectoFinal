package com.web.dto.auth.User;

import java.io.Serializable;

public record UserUpdateRequest(
    String name,
    String phone,
    String password  // opcional
) implements Serializable {}

