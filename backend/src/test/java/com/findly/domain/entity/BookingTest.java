package com.findly.domain.entity;

import com.findly.domain.enums.BookingStatus;
import com.findly.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private Booking booking;
    private User customer;
    private Business business;
    private Service service;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("0501234567")
                .role(UserRole.CUSTOMER)
                .build();

        business = Business.builder()
                .name("Test Business")
                .build();

        service = Service.builder()
                .name("Test Service")
                .durationMins(60)
                .build();

        booking = Booking.builder()
                .business(business)
                .service(service)
                .customer(customer)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(BookingStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Guest Booking Tests")
    class GuestBookingTests {

        @Test
        @DisplayName("Should identify guest booking when customer is null")
        void shouldIdentifyGuestBooking() {
            Booking guestBooking = Booking.builder()
                    .business(business)
                    .service(service)
                    .guestName("Guest User")
                    .guestPhone("0509876543")
                    .guestEmail("guest@example.com")
                    .date(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .build();

            assertThat(guestBooking.isGuestBooking()).isTrue();
        }

        @Test
        @DisplayName("Should identify non-guest booking when customer is present")
        void shouldIdentifyNonGuestBooking() {
            assertThat(booking.isGuestBooking()).isFalse();
        }

        @Test
        @DisplayName("Should return guest name when customer is null")
        void shouldReturnGuestName() {
            Booking guestBooking = Booking.builder()
                    .guestName("Guest User")
                    .build();

            assertThat(guestBooking.getCustomerName()).isEqualTo("Guest User");
        }

        @Test
        @DisplayName("Should return customer full name when customer is present")
        void shouldReturnCustomerFullName() {
            assertThat(booking.getCustomerName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should return guest phone when customer is null")
        void shouldReturnGuestPhone() {
            Booking guestBooking = Booking.builder()
                    .guestPhone("0509876543")
                    .build();

            assertThat(guestBooking.getCustomerPhone()).isEqualTo("0509876543");
        }

        @Test
        @DisplayName("Should return customer phone when customer is present")
        void shouldReturnCustomerPhone() {
            assertThat(booking.getCustomerPhone()).isEqualTo("0501234567");
        }

        @Test
        @DisplayName("Should return guest email when customer is null")
        void shouldReturnGuestEmail() {
            Booking guestBooking = Booking.builder()
                    .guestEmail("guest@example.com")
                    .build();

            assertThat(guestBooking.getCustomerEmail()).isEqualTo("guest@example.com");
        }

        @Test
        @DisplayName("Should return customer email when customer is present")
        void shouldReturnCustomerEmail() {
            assertThat(booking.getCustomerEmail()).isEqualTo("john@example.com");
        }
    }

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should approve pending booking")
        void shouldApprovePendingBooking() {
            booking.approve();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
            assertThat(booking.getConfirmedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should reject pending booking")
        void shouldRejectPendingBooking() {
            booking.reject("Customer requested cancellation");

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
            assertThat(booking.getRejectionReason()).isEqualTo("Customer requested cancellation");
        }

        @Test
        @DisplayName("Should cancel pending booking")
        void shouldCancelPendingBooking() {
            booking.cancel();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(booking.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("Should cancel approved booking")
        void shouldCancelApprovedBooking() {
            booking.approve();
            booking.cancel();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should complete approved booking")
        void shouldCompleteApprovedBooking() {
            booking.approve();
            booking.complete();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
            assertThat(booking.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should mark approved booking as no-show")
        void shouldMarkNoShow() {
            booking.approve();
            booking.markNoShow();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.NO_SHOW);
        }

        @Test
        @DisplayName("Should throw when approving non-pending booking")
        void shouldThrowWhenApprovingNonPending() {
            booking.approve();

            assertThatThrownBy(() -> booking.approve())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot be approved");
        }

        @Test
        @DisplayName("Should throw when rejecting non-pending booking")
        void shouldThrowWhenRejectingNonPending() {
            booking.approve();

            assertThatThrownBy(() -> booking.reject("reason"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot be rejected");
        }

        @Test
        @DisplayName("Should throw when cancelling completed booking")
        void shouldThrowWhenCancellingCompleted() {
            booking.approve();
            booking.complete();

            assertThatThrownBy(() -> booking.cancel())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot be cancelled");
        }

        @Test
        @DisplayName("Should throw when completing non-approved booking")
        void shouldThrowWhenCompletingNonApproved() {
            assertThatThrownBy(() -> booking.complete())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only approved bookings can be completed");
        }

        @Test
        @DisplayName("Should throw when marking non-approved booking as no-show")
        void shouldThrowWhenMarkingNonApprovedNoShow() {
            assertThatThrownBy(() -> booking.markNoShow())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only approved bookings can be marked as no-show");
        }
    }

    @Nested
    @DisplayName("Status Check Tests")
    class StatusCheckTests {

        @Test
        @DisplayName("Should allow cancellation for pending status")
        void shouldAllowCancellationForPending() {
            assertThat(booking.canBeCancelled()).isTrue();
        }

        @Test
        @DisplayName("Should allow cancellation for approved status")
        void shouldAllowCancellationForApproved() {
            booking.approve();
            assertThat(booking.canBeCancelled()).isTrue();
        }

        @Test
        @DisplayName("Should not allow cancellation for completed status")
        void shouldNotAllowCancellationForCompleted() {
            booking.approve();
            booking.complete();
            assertThat(booking.canBeCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should allow approval for pending status")
        void shouldAllowApprovalForPending() {
            assertThat(booking.canBeApproved()).isTrue();
        }

        @Test
        @DisplayName("Should not allow approval for approved status")
        void shouldNotAllowApprovalForApproved() {
            booking.approve();
            assertThat(booking.canBeApproved()).isFalse();
        }

        @Test
        @DisplayName("Should allow rejection for pending status")
        void shouldAllowRejectionForPending() {
            assertThat(booking.canBeRejected()).isTrue();
        }

        @Test
        @DisplayName("Should not allow rejection for approved status")
        void shouldNotAllowRejectionForApproved() {
            booking.approve();
            assertThat(booking.canBeRejected()).isFalse();
        }
    }
}
