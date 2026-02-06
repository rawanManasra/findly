package com.findly.api.controller;

import com.findly.application.dto.request.RejectBookingRequest;
import com.findly.application.dto.response.BookingResponse;
import com.findly.application.service.BookingService;
import com.findly.domain.enums.BookingStatus;
import com.findly.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUSINESS_OWNER')")
@Tag(name = "Owner - Bookings", description = "Business owner booking management endpoints")
public class OwnerBookingController {

    private final BookingService bookingService;

    // ==================== View Bookings ====================

    @GetMapping
    @Operation(summary = "Get all bookings for my businesses")
    public ResponseEntity<Page<BookingResponse>> getMyBusinessBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID businessId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {

        Page<BookingResponse> bookings = bookingService.getOwnerBookings(
                userDetails.getId(), businessId, status, date, pageable);
        return ResponseEntity.ok(bookings);
    }

    // ==================== Booking Actions ====================

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a pending booking")
    public ResponseEntity<BookingResponse> approveBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {

        BookingResponse booking = bookingService.approveBooking(userDetails.getId(), id);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a pending booking")
    public ResponseEntity<BookingResponse> rejectBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) RejectBookingRequest request) {

        BookingResponse booking = bookingService.rejectBooking(userDetails.getId(), id, request);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark booking as completed")
    public ResponseEntity<BookingResponse> completeBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {

        BookingResponse booking = bookingService.completeBooking(userDetails.getId(), id);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}/no-show")
    @Operation(summary = "Mark customer as no-show")
    public ResponseEntity<BookingResponse> markNoShow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {

        BookingResponse booking = bookingService.markNoShow(userDetails.getId(), id);
        return ResponseEntity.ok(booking);
    }

    // ==================== Statistics (Optional Enhancement) ====================

    @GetMapping("/today")
    @Operation(summary = "Get today's bookings for my businesses")
    public ResponseEntity<Page<BookingResponse>> getTodayBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID businessId,
            Pageable pageable) {

        Page<BookingResponse> bookings = bookingService.getOwnerBookings(
                userDetails.getId(), businessId, null, LocalDate.now(), pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending bookings awaiting approval")
    public ResponseEntity<Page<BookingResponse>> getPendingBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID businessId,
            Pageable pageable) {

        Page<BookingResponse> bookings = bookingService.getOwnerBookings(
                userDetails.getId(), businessId, BookingStatus.PENDING, null, pageable);
        return ResponseEntity.ok(bookings);
    }
}
