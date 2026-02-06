package com.findly.application.service;

import com.findly.api.exception.ApiException;
import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.request.CreateBookingRequest;
import com.findly.application.dto.request.RejectBookingRequest;
import com.findly.application.dto.response.BookingResponse;
import com.findly.application.dto.response.TimeSlotResponse;
import com.findly.application.mapper.BookingMapper;
import com.findly.domain.entity.*;
import com.findly.domain.enums.BookingStatus;
import com.findly.domain.enums.BusinessStatus;
import com.findly.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int SLOT_INTERVAL_MINUTES = 30; // Generate slots every 30 minutes

    // ==================== Slot Availability ====================

    /**
     * Get available time slots for a business on a specific date.
     */
    @Transactional(readOnly = true)
    public TimeSlotResponse getAvailableSlots(UUID businessId, LocalDate date, UUID serviceId) {
        log.debug("Getting available slots for business {} on {} for service {}", businessId, date, serviceId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", businessId));

        if (business.getStatus() != BusinessStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_NOT_ACTIVE", "Business is not active");
        }

        // Get service duration
        com.findly.domain.entity.Service service = null;
        int durationMins = SLOT_INTERVAL_MINUTES;
        if (serviceId != null) {
            service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));
            durationMins = service.getDurationMins();
        }

        // Get working hours for the day
        int dayOfWeek = date.getDayOfWeek().getValue() % 7; // Convert to 0=Sunday format
        WorkingHours workingHours = workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek)
                .orElse(null);

        // Check if business is open
        if (workingHours == null || workingHours.isClosed() ||
            workingHours.getStartTime() == null || workingHours.getEndTime() == null) {
            return TimeSlotResponse.builder()
                    .date(date)
                    .businessOpen(false)
                    .slots(List.of())
                    .build();
        }

        // Get existing bookings for the day
        List<Booking> existingBookings = bookingRepository.findByBusinessIdAndDateAndStatusIn(
                businessId, date, List.of(BookingStatus.PENDING, BookingStatus.APPROVED));

        // Generate time slots
        List<TimeSlotResponse.Slot> slots = generateTimeSlots(
                workingHours.getStartTime(),
                workingHours.getEndTime(),
                workingHours.getBreakStart(),
                workingHours.getBreakEnd(),
                durationMins,
                existingBookings,
                date);

        return TimeSlotResponse.builder()
                .date(date)
                .businessOpen(true)
                .openTime(workingHours.getStartTime().format(TIME_FORMATTER))
                .closeTime(workingHours.getEndTime().format(TIME_FORMATTER))
                .slots(slots)
                .build();
    }

    private List<TimeSlotResponse.Slot> generateTimeSlots(
            LocalTime openTime,
            LocalTime closeTime,
            LocalTime breakStart,
            LocalTime breakEnd,
            int durationMins,
            List<Booking> existingBookings,
            LocalDate date) {

        List<TimeSlotResponse.Slot> slots = new ArrayList<>();
        LocalTime currentTime = openTime;
        LocalTime now = LocalTime.now();
        boolean isToday = date.equals(LocalDate.now());

        while (currentTime.plusMinutes(durationMins).isBefore(closeTime) ||
               currentTime.plusMinutes(durationMins).equals(closeTime)) {

            LocalTime slotEnd = currentTime.plusMinutes(durationMins);

            // Check if slot is during break
            boolean isDuringBreak = false;
            if (breakStart != null && breakEnd != null) {
                isDuringBreak = !(slotEnd.isBefore(breakStart) || slotEnd.equals(breakStart) ||
                                  currentTime.isAfter(breakEnd) || currentTime.equals(breakEnd));
            }

            // Check if slot is in the past (for today)
            boolean isInPast = isToday && currentTime.isBefore(now);

            // Check if slot conflicts with existing bookings
            boolean hasConflict = hasBookingConflict(currentTime, slotEnd, existingBookings);

            boolean isAvailable = !isDuringBreak && !isInPast && !hasConflict;

            slots.add(TimeSlotResponse.Slot.builder()
                    .startTime(currentTime.format(TIME_FORMATTER))
                    .endTime(slotEnd.format(TIME_FORMATTER))
                    .available(isAvailable)
                    .build());

            currentTime = currentTime.plusMinutes(SLOT_INTERVAL_MINUTES);
        }

        return slots;
    }

    private boolean hasBookingConflict(LocalTime slotStart, LocalTime slotEnd, List<Booking> existingBookings) {
        for (Booking booking : existingBookings) {
            // Check if there's an overlap
            if (slotStart.isBefore(booking.getEndTime()) && slotEnd.isAfter(booking.getStartTime())) {
                return true;
            }
        }
        return false;
    }

    // ==================== Create Booking ====================

    /**
     * Create a new booking (for registered users).
     */
    @Transactional
    public BookingResponse createBooking(UUID customerId, CreateBookingRequest request) {
        log.info("Creating booking for customer {} at business {}", customerId, request.getBusinessId());

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        return createBookingInternal(request, customer, null, null, null);
    }

    /**
     * Create a guest booking (no authentication required).
     */
    @Transactional
    public BookingResponse createGuestBooking(CreateBookingRequest request) {
        log.info("Creating guest booking at business {}", request.getBusinessId());

        // Validate guest info
        if (request.getGuestName() == null || request.getGuestName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GUEST_NAME_REQUIRED", "Guest name is required");
        }
        if (request.getGuestPhone() == null || request.getGuestPhone().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GUEST_PHONE_REQUIRED", "Guest phone is required");
        }

        return createBookingInternal(request, null, request.getGuestName(),
                request.getGuestPhone(), request.getGuestEmail());
    }

    private BookingResponse createBookingInternal(
            CreateBookingRequest request,
            User customer,
            String guestName,
            String guestPhone,
            String guestEmail) {

        // Validate business
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", request.getBusinessId()));

        if (business.getStatus() != BusinessStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_NOT_ACTIVE", "Business is not accepting bookings");
        }

        // Validate service
        com.findly.domain.entity.Service service = serviceRepository.findByIdAndBusinessId(
                request.getServiceId(), request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        if (!service.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SERVICE_NOT_ACTIVE", "Service is not available");
        }

        // Parse and validate time
        LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        LocalTime endTime = startTime.plusMinutes(service.getDurationMins());

        // Validate the slot is available
        validateSlotAvailability(business.getId(), request.getDate(), startTime, endTime);

        // Create booking
        Booking booking = Booking.builder()
                .business(business)
                .service(service)
                .customer(customer)
                .guestName(guestName)
                .guestPhone(guestPhone)
                .guestEmail(guestEmail)
                .date(request.getDate())
                .startTime(startTime)
                .endTime(endTime)
                .status(BookingStatus.PENDING)
                .notes(request.getNotes())
                .build();

        booking = bookingRepository.save(booking);

        log.info("Booking created: {}", booking.getId());

        // TODO: Send notification to business owner

        return bookingMapper.toResponse(booking);
    }

    private void validateSlotAvailability(UUID businessId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Check working hours
        int dayOfWeek = date.getDayOfWeek().getValue() % 7;
        WorkingHours workingHours = workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_CLOSED",
                        "Business is not open on this day"));

        if (workingHours.isClosed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_CLOSED", "Business is closed on this day");
        }

        if (startTime.isBefore(workingHours.getStartTime()) || endTime.isAfter(workingHours.getEndTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OUTSIDE_HOURS",
                    "Requested time is outside business hours");
        }

        // Check for break time
        if (workingHours.hasBreak()) {
            if (!(endTime.isBefore(workingHours.getBreakStart()) || endTime.equals(workingHours.getBreakStart()) ||
                  startTime.isAfter(workingHours.getBreakEnd()) || startTime.equals(workingHours.getBreakEnd()))) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "DURING_BREAK",
                        "Requested time is during business break");
            }
        }

        // Check for conflicts
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                businessId, date, startTime, endTime,
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED));

        if (!conflicts.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "SLOT_UNAVAILABLE",
                    "This time slot is no longer available");
        }

        // Check if date is in the past
        if (date.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATE_IN_PAST", "Cannot book for a past date");
        }

        // Check if time is in the past (for today)
        if (date.equals(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TIME_IN_PAST", "Cannot book for a past time");
        }
    }

    // ==================== Customer Operations ====================

    /**
     * Get bookings for a customer.
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getCustomerBookings(UUID customerId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByCustomerIdOrderByDateDescStartTimeDesc(customerId, pageable);
        return bookings.map(bookingMapper::toResponse);
    }

    /**
     * Get a specific booking for a customer.
     */
    @Transactional(readOnly = true)
    public BookingResponse getCustomerBooking(UUID customerId, UUID bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getCustomer() == null || !booking.getCustomer().getId().equals(customerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You don't have access to this booking");
        }

        return bookingMapper.toResponse(booking);
    }

    /**
     * Cancel a booking (by customer).
     */
    @Transactional
    public BookingResponse cancelBooking(UUID customerId, UUID bookingId) {
        log.info("Customer {} cancelling booking {}", customerId, bookingId);

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getCustomer() == null || !booking.getCustomer().getId().equals(customerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You don't have access to this booking");
        }

        if (!booking.canBeCancelled()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_CANCEL",
                    "This booking cannot be cancelled in its current status");
        }

        booking.cancel();
        booking = bookingRepository.save(booking);

        log.info("Booking cancelled: {}", bookingId);

        // TODO: Send notification to business owner

        return bookingMapper.toResponse(booking);
    }

    // ==================== Owner Operations ====================

    /**
     * Get bookings for owner's businesses.
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getOwnerBookings(UUID ownerId, UUID businessId, BookingStatus status,
                                                   LocalDate date, Pageable pageable) {
        // Verify ownership
        if (businessId != null) {
            verifyBusinessOwnership(businessId, ownerId);
        }

        Page<Booking> bookings;

        if (businessId != null && status != null) {
            bookings = bookingRepository.findByBusinessIdAndStatus(businessId, status, pageable);
        } else if (businessId != null && date != null) {
            bookings = bookingRepository.findByBusinessIdAndDate(businessId, date, pageable);
        } else if (businessId != null) {
            bookings = bookingRepository.findByBusinessIdOrderByDateDescStartTimeDesc(businessId, pageable);
        } else {
            // Get all bookings for all owner's businesses
            // For simplicity, we'll require a business ID for now
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_ID_REQUIRED",
                    "Business ID is required to view bookings");
        }

        return bookings.map(bookingMapper::toResponse);
    }

    /**
     * Approve a booking (by owner).
     */
    @Transactional
    public BookingResponse approveBooking(UUID ownerId, UUID bookingId) {
        log.info("Owner {} approving booking {}", ownerId, bookingId);

        Booking booking = getBookingForOwner(bookingId, ownerId);

        if (!booking.canBeApproved()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_APPROVE",
                    "This booking cannot be approved in its current status");
        }

        booking.approve();
        booking = bookingRepository.save(booking);

        log.info("Booking approved: {}", bookingId);

        // TODO: Send notification to customer

        return bookingMapper.toResponse(booking);
    }

    /**
     * Reject a booking (by owner).
     */
    @Transactional
    public BookingResponse rejectBooking(UUID ownerId, UUID bookingId, RejectBookingRequest request) {
        log.info("Owner {} rejecting booking {}", ownerId, bookingId);

        Booking booking = getBookingForOwner(bookingId, ownerId);

        if (!booking.canBeRejected()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_REJECT",
                    "This booking cannot be rejected in its current status");
        }

        booking.reject(request != null ? request.getReason() : null);
        booking = bookingRepository.save(booking);

        log.info("Booking rejected: {}", bookingId);

        // TODO: Send notification to customer

        return bookingMapper.toResponse(booking);
    }

    /**
     * Mark booking as completed (by owner).
     */
    @Transactional
    public BookingResponse completeBooking(UUID ownerId, UUID bookingId) {
        log.info("Owner {} completing booking {}", ownerId, bookingId);

        Booking booking = getBookingForOwner(bookingId, ownerId);

        booking.complete();
        booking = bookingRepository.save(booking);

        log.info("Booking completed: {}", bookingId);

        return bookingMapper.toResponse(booking);
    }

    /**
     * Mark booking as no-show (by owner).
     */
    @Transactional
    public BookingResponse markNoShow(UUID ownerId, UUID bookingId) {
        log.info("Owner {} marking booking {} as no-show", ownerId, bookingId);

        Booking booking = getBookingForOwner(bookingId, ownerId);

        booking.markNoShow();
        booking = bookingRepository.save(booking);

        log.info("Booking marked as no-show: {}", bookingId);

        return bookingMapper.toResponse(booking);
    }

    // ==================== Helper Methods ====================

    private Booking getBookingForOwner(UUID bookingId, UUID ownerId) {
        return bookingRepository.findByIdAndOwnerId(bookingId, ownerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                        "Booking not found or you don't have access"));
    }

    private void verifyBusinessOwnership(UUID businessId, UUID ownerId) {
        businessRepository.findByIdAndOwnerId(businessId, ownerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                        "Business not found or you don't have access"));
    }
}
