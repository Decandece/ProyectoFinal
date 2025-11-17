package com.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.auth.Login.LoginRequest;
import com.web.dto.auth.Login.LoginResponse;
import com.web.dto.auth.Login.RegisterRequest;
import com.web.dto.auth.User.UserResponse;
import com.web.config.CustomUserDetailsService;
import com.web.entity.User;
import com.web.exception.InvalidCredentialsException;
import com.web.service.auth.AuthService;
import com.web.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Verifica que un usuario anónimo pueda registrarse correctamente
    @Test
    @WithAnonymousUser
    void register_shouldReturn201AndLocation() throws Exception {
        var req = new RegisterRequest("John Doe", "john@example.com", "123456789", "password123", null);
        var resp = new UserResponse(10L, "John Doe", "john@example.com", "123456789", User.Role.PASSENGER, User.Status.ACTIVE, null);

        when(authService.register(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    // Verifica validación de datos en el registro
    @Test
    @WithAnonymousUser
    void register_shouldReturn400WhenInvalid() throws Exception{
        var req = new RegisterRequest("", "invalid-email", "", "pass", null);

        mvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // Verifica que un usuario pueda iniciar sesión y recibir un token JWT
    @Test
    @WithAnonymousUser
    void login_shouldReturn200WithToken() throws Exception {
        var req = new LoginRequest("john@example.com", "password123");
        var userResp = new UserResponse(1L, "John Doe", "john@example.com", "123456789", User.Role.PASSENGER, User.Status.ACTIVE, null);
        var resp = new LoginResponse("token123", "Bearer", userResp);

        when(authService.login(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.id").value(1));
    }

    // Verifica que credenciales inválidas retornen 401
    @Test
    @WithAnonymousUser
    void login_shouldReturn401WhenInvalidCredentials() throws Exception {
        var req = new LoginRequest("john@example.com", "wrongpassword");

        when(authService.login(any())).thenThrow(new InvalidCredentialsException("Credenciales inválidas"));

        mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}

