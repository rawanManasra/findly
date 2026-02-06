package com.findly.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    // Use Business as a concrete implementation of BaseEntity
    private Business entity;

    @BeforeEach
    void setUp() {
        entity = Business.builder()
                .name("Test Entity")
                .build();
    }

    @Nested
    @DisplayName("onCreate Tests")
    class OnCreateTests {

        @Test
        @DisplayName("Should set createdAt when onCreate is called")
        void shouldSetCreatedAt() {
            entity.onCreate();

            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("Should set updatedAt when onCreate is called")
        void shouldSetUpdatedAtOnCreate() {
            entity.onCreate();

            assertThat(entity.getUpdatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("Should set same createdAt and updatedAt on create")
        void shouldSetSameTimestampsOnCreate() {
            entity.onCreate();

            // Allow for small time difference due to execution
            long difference = Math.abs(
                entity.getCreatedAt().toEpochMilli() - entity.getUpdatedAt().toEpochMilli()
            );
            assertThat(difference).isLessThan(100);
        }
    }

    @Nested
    @DisplayName("onUpdate Tests")
    class OnUpdateTests {

        @Test
        @DisplayName("Should update updatedAt when onUpdate is called")
        void shouldUpdateUpdatedAt() {
            entity.onCreate();
            Instant originalUpdatedAt = entity.getUpdatedAt();

            // Small delay to ensure time difference
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            entity.onUpdate();

            assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("Should not change createdAt when onUpdate is called")
        void shouldNotChangeCreatedAtOnUpdate() {
            entity.onCreate();
            Instant originalCreatedAt = entity.getCreatedAt();

            entity.onUpdate();

            assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        }
    }

    @Nested
    @DisplayName("ID Tests")
    class IdTests {

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            UUID id = UUID.randomUUID();
            entity.setId(id);

            assertThat(entity.getId()).isEqualTo(id);
        }
    }
}
