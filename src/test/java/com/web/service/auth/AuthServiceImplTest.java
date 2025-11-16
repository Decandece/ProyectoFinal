package com.web.service.auth;

import com.web.dto.auth.Login.LoginRequest;
import com.web.dto.auth.Login.LoginResponse;
import com.web.dto.auth.Login.RegisterRequest;
import com.web.dto.auth.User.UserResponse;
import com.web.dto.auth.User.mapper.UserMapper;
import com.web.entity.User;
import com.web.exception.EmailAlreadyExistsException;
import com.web.exception.InvalidCredentialsException;
import com.web.repository.UserRepository;
import com.web.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(User.Role.PASSENGER)
                .status(User.Status.ACTIVE)
                .passwordHash("$2a$10$encoded")
                .build();

        userResponse = new UserResponse(
                1L, "John Doe", "john@example.com", null,
                User.Role.PASSENGER, User.Status.ACTIVE, null
        );
    }

    @Test
    void shouldRegister_WithValidRequest_ReturnUserResponse() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "John Doe", "john@example.com", "123456789", "password123", null
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When
        UserResponse result = authService.register(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("john@example.com");
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRegister_WithExistingEmail_ThrowException() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "John Doe", "john@example.com", "123456789", "password123", null
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void shouldLogin_WithValidCredentials_ReturnLoginResponse() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$encoded")).thenReturn(true);
        when(jwtTokenProvider.generateToken("john@example.com", "PASSENGER")).thenReturn("token123");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("token123");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$encoded");
    }

    @Test
    void shouldLogin_WithInvalidCredentials_ThrowException() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encoded")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void shouldValidateToken_WithValidToken_ReturnTrue() {
        // Given
        when(jwtTokenProvider.validateToken("validToken")).thenReturn(true);

        // When
        boolean result = authService.validateToken("validToken");

        // Then
        assertThat(result).isTrue();
        verify(jwtTokenProvider).validateToken("validToken");
    }
}

