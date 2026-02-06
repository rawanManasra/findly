package com.findly.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SoftDeletableEntityTest {

    // Use User as a concrete implementation of SoftDeletableEntity
    private User entity;

    @BeforeEach
    void setUp() {
        entity = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .build();
    }

    @Nested
    @DisplayName("isDeleted Tests")
    class IsDeletedTests {

        @Test
        @DisplayName("Should return false when deletedAt is null")
        void shouldReturnFalseWhenNotDeleted() {
            entity.setDeletedAt(null);
            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should return true when deletedAt is set")
        void shouldReturnTrueWhenDeleted() {
            entity.setDeletedAt(Instant.now());
            assertThat(entity.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("softDelete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should set deletedAt when soft deleted")
        void shouldSetDeletedAt() {
            entity.softDelete();

            assertThat(entity.getDeletedAt()).isNotNull();
            assertThat(entity.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Should set deletedAt to current time")
        void shouldSetDeletedAtToCurrentTime() {
            Instant before = Instant.now();
            entity.softDelete();
            Instant after = Instant.now();

            assertThat(entity.getDeletedAt()).isAfterOrEqualTo(before);
            assertThat(entity.getDeletedAt()).isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("restore Tests")
    class RestoreTests {

        @Test
        @DisplayName("Should clear deletedAt when restored")
        void shouldClearDeletedAt() {
            entity.softDelete();
            assertThat(entity.isDeleted()).isTrue();

            entity.restore();

            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should work when already not deleted")
        void shouldWorkWhenNotDeleted() {
            entity.setDeletedAt(null);

            entity.restore();

            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle soft delete and restore cycle")
        void shouldHandleSoftDeleteAndRestoreCycle() {
            // Initially not deleted
            assertThat(entity.isDeleted()).isFalse();

            // Soft delete
            entity.softDelete();
            assertThat(entity.isDeleted()).isTrue();

            // Restore
            entity.restore();
            assertThat(entity.isDeleted()).isFalse();

            // Soft delete again
            entity.softDelete();
            assertThat(entity.isDeleted()).isTrue();
        }
    }
}
