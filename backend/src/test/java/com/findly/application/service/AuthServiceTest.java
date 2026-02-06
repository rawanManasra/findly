package com.findly.application.service;

import com.findly.api.exception.ApiException;
import com.findly.application.dto.request.LoginRequest;
import com.findly.application.dto.request.RefreshTokenRequest;
import com.findly.application.dto.request.RegisterRequest;
import com.findly.application.dto.response.AuthResponse;
import com.findly.application.dto.response.UserResponse;
import com.findly.application.mapper.UserMapper;
import com.findly.domain.entity.RefreshToken;
import com.findly.domain.entity.User;
import com.findly.domain.enums.UserRole;
import com.findly.domain.repository.RefreshTokenRepository;
import com.findly.domain.repository.UserRepository;
import com.findly.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.CUSTOMER)
                .emailVerified(false)
                .phoneVerified(false)
                .build();
        user.setId(userId);

        userResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.CUSTOMER)
                .build();
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUser() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@example.com")
                    .password("password123")
                    .firstName("New")
                    .lastName("User")
                    .role(UserRole.CUSTOMER)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refreshToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(null);

            AuthResponse result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("accessToken");
            assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void shouldThrowWhenEmailExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("existing@example.com")
                    .password("password123")
                    .firstName("Test")
                    .role(UserRole.CUSTOMER)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Email is already registered");
        }

        @Test
        @DisplayName("Should throw when phone already exists")
        void shouldThrowWhenPhoneExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@example.com")
                    .password("password123")
                    .firstName("Test")
                    .phone("0501234567")
                    .role(UserRole.CUSTOMER)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByPhone(anyString())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Phone number is already registered");
        }

        @Test
        @DisplayName("Should register business owner")
        void shouldRegisterBusinessOwner() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("owner@example.com")
                    .password("password123")
                    .firstName("Business")
                    .lastName("Owner")
                    .role(UserRole.BUSINESS_OWNER)
                    .build();

            User ownerUser = User.builder()
                    .email("owner@example.com")
                    .role(UserRole.BUSINESS_OWNER)
                    .build();
            ownerUser.setId(UUID.randomUUID());

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(ownerUser);
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refreshToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            AuthResponse result = authService.register(request);

            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(u -> u.getRole() == UserRole.BUSINESS_OWNER));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            Authentication auth = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refreshToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            AuthResponse result = authService.login(request);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("accessToken");
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid password")
        void shouldThrowForInvalidCredentials() {
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongPassword")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Should handle case-insensitive email")
        void shouldHandleCaseInsensitiveEmail() {
            LoginRequest request = LoginRequest.builder()
                    .email("TEST@EXAMPLE.COM")
                    .password("password123")
                    .build();

            Authentication auth = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refreshToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            AuthResponse result = authService.login(request);

            assertThat(result).isNotNull();
            verify(userRepository).findByEmail("test@example.com");
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("validRefreshToken")
                    .build();

            RefreshToken storedToken = RefreshToken.builder()
                    .user(user)
                    .tokenHash("hashedToken")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            storedToken.setId(UUID.randomUUID());

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("newAccessToken");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("newRefreshToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            AuthResponse result = authService.refreshToken(request);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
            verify(refreshTokenRepository).delete(storedToken);
        }

        @Test
        @DisplayName("Should throw for invalid refresh token")
        void shouldThrowForInvalidRefreshToken() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalidToken")
                    .build();

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("Should throw for expired refresh token")
        void shouldThrowForExpiredRefreshToken() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("expiredToken")
                    .build();

            RefreshToken expiredToken = RefreshToken.builder()
                    .user(user)
                    .tokenHash("hashedToken")
                    .expiresAt(Instant.now().minusSeconds(3600)) // Expired
                    .build();
            expiredToken.setId(UUID.randomUUID());

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("expired");

            verify(refreshTokenRepository).delete(expiredToken);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout user successfully")
        void shouldLogoutSuccessfully() {
            authService.logout(userId);

            verify(refreshTokenRepository).deleteByUserId(userId);
        }
    }

    @Nested
    @DisplayName("Get Current User Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should get current user")
        void shouldGetCurrentUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = authService.getCurrentUser(userId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getCurrentUser(userId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("User not found");
        }
    }
}
