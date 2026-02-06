package com.findly.domain.entity;

import com.findly.domain.enums.NotificationChannel;
import com.findly.domain.enums.NotificationStatus;
import com.findly.domain.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .type(NotificationType.BOOKING_APPROVED)
                .channel(NotificationChannel.SMS)
                .recipient("0501234567")
                .message("Your booking has been confirmed")
                .status(NotificationStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("markSent Tests")
    class MarkSentTests {

        @Test
        @DisplayName("Should mark notification as sent")
        void shouldMarkAsSent() {
            notification.markSent();

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notification.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update sentAt timestamp when marked as sent")
        void shouldUpdateSentAtTimestamp() {
            notification.markSent();

            assertThat(notification.getSentAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("markFailed Tests")
    class MarkFailedTests {

        @Test
        @DisplayName("Should mark notification as failed")
        void shouldMarkAsFailed() {
            notification.markFailed("Network error");

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        }

        @Test
        @DisplayName("Should store error message when failed")
        void shouldStoreErrorMessage() {
            String errorMessage = "Connection timeout";
            notification.markFailed(errorMessage);

            assertThat(notification.getErrorMessage()).isEqualTo(errorMessage);
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have PENDING status by default")
        void shouldHavePendingStatusByDefault() {
            Notification newNotification = Notification.builder()
                    .type(NotificationType.BOOKING_REMINDER)
                    .channel(NotificationChannel.EMAIL)
                    .recipient("test@example.com")
                    .message("Test message")
                    .build();

            assertThat(newNotification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("Should have createdAt set by default")
        void shouldHaveCreatedAtByDefault() {
            Notification newNotification = Notification.builder()
                    .type(NotificationType.BOOKING_REMINDER)
                    .channel(NotificationChannel.EMAIL)
                    .recipient("test@example.com")
                    .message("Test message")
                    .build();

            assertThat(newNotification.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get all notification types")
        void shouldSetAndGetNotificationTypes() {
            notification.setType(NotificationType.BOOKING_CANCELLED);
            assertThat(notification.getType()).isEqualTo(NotificationType.BOOKING_CANCELLED);

            notification.setType(NotificationType.BOOKING_REMINDER);
            assertThat(notification.getType()).isEqualTo(NotificationType.BOOKING_REMINDER);
        }

        @Test
        @DisplayName("Should set and get all notification channels")
        void shouldSetAndGetNotificationChannels() {
            notification.setChannel(NotificationChannel.EMAIL);
            assertThat(notification.getChannel()).isEqualTo(NotificationChannel.EMAIL);

            notification.setChannel(NotificationChannel.PUSH);
            assertThat(notification.getChannel()).isEqualTo(NotificationChannel.PUSH);
        }

        @Test
        @DisplayName("Should set and get user")
        void shouldSetAndGetUser() {
            User user = User.builder()
                    .email("test@example.com")
                    .firstName("Test")
                    .build();
            notification.setUser(user);
            assertThat(notification.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("Should set and get booking")
        void shouldSetAndGetBooking() {
            Booking booking = Booking.builder()
                    .notes("Test booking")
                    .build();
            notification.setBooking(booking);
            assertThat(notification.getBooking()).isEqualTo(booking);
        }
    }
}
