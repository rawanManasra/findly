package com.findly.api.controller;

import com.findly.application.dto.request.CreateBookingRequest;
import com.findly.application.dto.response.BookingResponse;
import com.findly.application.dto.response.TimeSlotResponse;
import com.findly.application.service.BookingService;
import com.findly.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Consumer booking endpoints")
public class BookingController {

    private final BookingService bookingService;

    // ==================== Available Slots (Public) ====================

    @GetMapping("/businesses/{businessId}/slots")
    @Operation(summary = "Get available time slots for a business service")
    public ResponseEntity<TimeSlotResponse> getAvailableSlots(
            @PathVariable UUID businessId,
            @RequestParam UUID serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        TimeSlotResponse slots = bookingService.getAvailableSlots(businessId, date, serviceId);
        return ResponseEntity.ok(slots);
    }

    // ==================== Authenticated Customer Bookings ====================

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new booking (authenticated customer)")
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBookingRequest request) {

        BookingResponse booking = bookingService.createBooking(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my bookings")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {

        Page<BookingResponse> bookings = bookingService.getCustomerBookings(userDetails.getId(), pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get booking details")
    public ResponseEntity<BookingResponse> getBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {

        BookingResponse booking = bookingService.getCustomerBooking(userDetails.getId(), id);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<BookingResponse> cancelBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {

        BookingResponse booking = bookingService.cancelBooking(userDetails.getId(), id);
        return ResponseEntity.ok(booking);
    }

    // ==================== Guest Bookings (No Auth Required) ====================

    @PostMapping("/guest")
    @Operation(summary = "Create a guest booking (no account required)")
    public ResponseEntity<BookingResponse> createGuestBooking(
            @Valid @RequestBody CreateBookingRequest request) {

        BookingResponse booking = bookingService.createGuestBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }
}
