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
import com.findly.domain.repository.RefreshTokenRepository;
import com.findly.domain.repository.UserRepository;
import com.findly.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email is already registered");
        }

        // Check if phone already exists (if provided)
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new ApiException(HttpStatus.CONFLICT, "PHONE_EXISTS", "Phone number is already registered");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .phone(request.getPhone())
                .role(request.getRole())
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getId());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            log.info("User logged in successfully: {}", user.getId());

            return generateAuthResponse(user);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw e;
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refreshing token");

        String tokenHash = hashToken(request.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Refresh token has expired");
        }

        User user = refreshToken.getUser();

        // Delete old refresh token
        refreshTokenRepository.delete(refreshToken);

        log.debug("Token refreshed for user: {}", user.getId());

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(UUID userId) {
        log.info("Logging out user: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));

        return userMapper.toResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                userMapper.toResponse(user)
        );
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(token))
                .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
