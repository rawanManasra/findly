package com.findly.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        refreshToken = RefreshToken.builder()
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Nested
    @DisplayName("isExpired Tests")
    class IsExpiredTests {

        @Test
        @DisplayName("Should return false when token is not expired")
        void shouldReturnFalseWhenNotExpired() {
            refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));
            assertThat(refreshToken.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should return true when token is expired")
        void shouldReturnTrueWhenExpired() {
            refreshToken.setExpiresAt(Instant.now().minusSeconds(3600));
            assertThat(refreshToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Should return true when token expires exactly now")
        void shouldReturnTrueWhenExpiringNow() {
            refreshToken.setExpiresAt(Instant.now().minusSeconds(1));
            assertThat(refreshToken.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have createdAt set by default")
        void shouldHaveCreatedAtByDefault() {
            RefreshToken newToken = RefreshToken.builder()
                    .tokenHash("hash")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(newToken.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get device info")
        void shouldSetAndGetDeviceInfo() {
            refreshToken.setDeviceInfo("Mozilla/5.0");
            assertThat(refreshToken.getDeviceInfo()).isEqualTo("Mozilla/5.0");
        }

        @Test
        @DisplayName("Should set and get user")
        void shouldSetAndGetUser() {
            User user = User.builder()
                    .email("test@example.com")
                    .firstName("Test")
                    .build();
            refreshToken.setUser(user);
            assertThat(refreshToken.getUser()).isEqualTo(user);
        }
    }
}
