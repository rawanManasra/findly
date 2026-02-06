package com.findly.application.dto.response;

import com.findly.domain.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private UUID id;

    // Business info
    private UUID businessId;
    private String businessName;
    private String businessPhone;
    private String businessAddress;

    // Service info
    private UUID serviceId;
    private String serviceName;
    private Integer serviceDurationMins;
    private String servicePrice;

    // Customer info
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private boolean guestBooking;

    // Appointment details
    private LocalDate date;
    private String startTime;
    private String endTime;
    private BookingStatus status;
    private String notes;
    private String rejectionReason;

    // Timestamps
    private Instant bookedAt;
    private Instant confirmedAt;
    private Instant cancelledAt;
    private Instant completedAt;
    private Instant createdAt;
}
