package com.web.service.auth;

import com.web.dto.auth.Login.LoginRequest;
import com.web.dto.auth.Login.LoginResponse;
import com.web.dto.auth.Login.RegisterRequest;
import com.web.dto.auth.User.UserResponse;

public interface AuthService {
    
    UserResponse register(RegisterRequest request);
    
    LoginResponse login(LoginRequest request);
    
    boolean validateToken(String token);
}

