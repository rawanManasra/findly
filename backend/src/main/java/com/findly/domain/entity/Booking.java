package com.findly.domain.entity;

import com.findly.domain.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_business_date", columnList = "business_id, date"),
        @Index(name = "idx_bookings_customer", columnList = "customer_id"),
        @Index(name = "idx_bookings_status", columnList = "status"),
        @Index(name = "idx_bookings_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer; // Nullable for guest bookings

    // Guest info (used when customer is null)
    @Column(name = "guest_name", length = 200)
    private String guestName;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    @Column(name = "guest_email", length = 255)
    private String guestEmail;

    // Appointment details
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Tracking timestamps
    @Column(name = "booked_at", nullable = false)
    @Builder.Default
    private Instant bookedAt = Instant.now();

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    // Helper methods
    public boolean isGuestBooking() {
        return customer == null;
    }

    public String getCustomerName() {
        if (customer != null) {
            return customer.getFullName();
        }
        return guestName;
    }

    public String getCustomerPhone() {
        if (customer != null) {
            return customer.getPhone();
        }
        return guestPhone;
    }

    public String getCustomerEmail() {
        if (customer != null) {
            return customer.getEmail();
        }
        return guestEmail;
    }

    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || status == BookingStatus.APPROVED;
    }

    public boolean canBeApproved() {
        return status == BookingStatus.PENDING;
    }

    public boolean canBeRejected() {
        return status == BookingStatus.PENDING;
    }

    public void approve() {
        if (!canBeApproved()) {
            throw new IllegalStateException("Booking cannot be approved in current status: " + status);
        }
        this.status = BookingStatus.APPROVED;
        this.confirmedAt = Instant.now();
    }

    public void reject(String reason) {
        if (!canBeRejected()) {
            throw new IllegalStateException("Booking cannot be rejected in current status: " + status);
        }
        this.status = BookingStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Booking cannot be cancelled in current status: " + status);
        }
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public void complete() {
        if (status != BookingStatus.APPROVED) {
            throw new IllegalStateException("Only approved bookings can be completed");
        }
        this.status = BookingStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markNoShow() {
        if (status != BookingStatus.APPROVED) {
            throw new IllegalStateException("Only approved bookings can be marked as no-show");
        }
        this.status = BookingStatus.NO_SHOW;
    }
}
