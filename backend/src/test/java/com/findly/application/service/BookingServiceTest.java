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
import com.findly.domain.enums.UserRole;
import com.findly.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private WorkingHoursRepository workingHoursRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private UUID businessId;
    private UUID serviceId;
    private UUID customerId;
    private UUID ownerId;
    private UUID bookingId;
    private Business business;
    private Service service;
    private User customer;
    private User owner;
    private WorkingHours workingHours;
    private Booking booking;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        owner = User.builder()
                .email("owner@test.com")
                .firstName("Business")
                .lastName("Owner")
                .role(UserRole.BUSINESS_OWNER)
                .build();
        owner.setId(ownerId);

        customer = User.builder()
                .email("customer@test.com")
                .firstName("Test")
                .lastName("Customer")
                .role(UserRole.CUSTOMER)
                .build();
        customer.setId(customerId);

        business = Business.builder()
                .owner(owner)
                .name("Test Business")
                .status(BusinessStatus.ACTIVE)
                .build();
        business.setId(businessId);

        service = Service.builder()
                .business(business)
                .name("Test Service")
                .durationMins(60)
                .active(true)
                .build();
        service.setId(serviceId);

        workingHours = WorkingHours.builder()
                .business(business)
                .dayOfWeek(1) // Monday
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .closed(false)
                .build();
        workingHours.setId(UUID.randomUUID());

        booking = Booking.builder()
                .business(business)
                .service(service)
                .customer(customer)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(BookingStatus.PENDING)
                .build();
        booking.setId(bookingId);

        bookingResponse = BookingResponse.builder()
                .id(bookingId)
                .businessId(businessId)
                .businessName("Test Business")
                .serviceId(serviceId)
                .serviceName("Test Service")
                .status(BookingStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Get Available Slots Tests")
    class GetAvailableSlotsTests {

        @Test
        @DisplayName("Should return available slots for active business")
        void shouldReturnAvailableSlots() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findByBusinessIdAndDateAndStatusIn(eq(businessId), eq(date), anyList()))
                    .thenReturn(List.of());

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, serviceId);

            assertThat(result).isNotNull();
            assertThat(result.isBusinessOpen()).isTrue();
            assertThat(result.getDate()).isEqualTo(date);
            assertThat(result.getSlots()).isNotEmpty();
        }

        @Test
        @DisplayName("Should return closed status when business is closed")
        void shouldReturnClosedWhenBusinessClosed() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);
            workingHours.setClosed(true);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, serviceId);

            assertThat(result.isBusinessOpen()).isFalse();
            assertThat(result.getSlots()).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when business not found")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getAvailableSlots(businessId, LocalDate.now(), serviceId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when business not active")
        void shouldThrowWhenBusinessNotActive() {
            business.setStatus(BusinessStatus.INACTIVE);
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

            assertThatThrownBy(() -> bookingService.getAvailableSlots(businessId, LocalDate.now(), serviceId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Should exclude slots with existing bookings")
        void shouldExcludeSlotsWithExistingBookings() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            Booking existingBooking = Booking.builder()
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .status(BookingStatus.APPROVED)
                    .build();

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findByBusinessIdAndDateAndStatusIn(eq(businessId), eq(date), anyList()))
                    .thenReturn(List.of(existingBooking));

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, serviceId);

            // Verify that the 10:00 slot is marked as unavailable
            boolean tenAmSlotAvailable = result.getSlots().stream()
                    .filter(s -> s.getStartTime().equals("10:00"))
                    .findFirst()
                    .map(TimeSlotResponse.Slot::isAvailable)
                    .orElse(true);

            assertThat(tenAmSlotAvailable).isFalse();
        }
    }

    @Nested
    @DisplayName("Create Booking Tests")
    class CreateBookingTests {

        private CreateBookingRequest createValidRequest() {
            return CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .build();
        }

        @Test
        @DisplayName("Should create booking for authenticated customer")
        void shouldCreateBookingForAuthenticatedCustomer() {
            CreateBookingRequest request = createValidRequest();
            LocalDate date = request.getDate();
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findConflictingBookings(any(), any(), any(), any(), anyList()))
                    .thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.createBooking(customerId, request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(bookingId);
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            when(userRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(customerId, createValidRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when business not active")
        void shouldThrowWhenBusinessNotActiveForBooking() {
            business.setStatus(BusinessStatus.INACTIVE);
            CreateBookingRequest request = createValidRequest();

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("not accepting bookings");
        }

        @Test
        @DisplayName("Should throw exception when service not active")
        void shouldThrowWhenServiceNotActive() {
            service.setActive(false);
            CreateBookingRequest request = createValidRequest();

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("Should throw exception when slot has conflict")
        void shouldThrowWhenSlotHasConflict() {
            CreateBookingRequest request = createValidRequest();
            LocalDate date = request.getDate();
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findConflictingBookings(any(), any(), any(), any(), anyList()))
                    .thenReturn(List.of(booking));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("no longer available");
        }
    }

    @Nested
    @DisplayName("Guest Booking Tests")
    class GuestBookingTests {

        @Test
        @DisplayName("Should create guest booking with valid info")
        void shouldCreateGuestBooking() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .guestName("John Doe")
                    .guestPhone("0501234567")
                    .guestEmail("john@example.com")
                    .build();

            LocalDate date = request.getDate();
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            Booking guestBooking = Booking.builder()
                    .business(business)
                    .service(service)
                    .guestName("John Doe")
                    .guestPhone("0501234567")
                    .status(BookingStatus.PENDING)
                    .build();
            guestBooking.setId(bookingId);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findConflictingBookings(any(), any(), any(), any(), anyList()))
                    .thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class))).thenReturn(guestBooking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.createGuestBooking(request);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw exception when guest name missing")
        void shouldThrowWhenGuestNameMissing() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .guestPhone("0501234567")
                    .build();

            assertThatThrownBy(() -> bookingService.createGuestBooking(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Guest name is required");
        }

        @Test
        @DisplayName("Should throw exception when guest phone missing")
        void shouldThrowWhenGuestPhoneMissing() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .guestName("John Doe")
                    .build();

            assertThatThrownBy(() -> bookingService.createGuestBooking(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Guest phone is required");
        }
    }

    @Nested
    @DisplayName("Customer Operations Tests")
    class CustomerOperationsTests {

        @Test
        @DisplayName("Should get customer bookings")
        void shouldGetCustomerBookings() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Booking> bookingPage = new PageImpl<>(List.of(booking));

            when(bookingRepository.findByCustomerIdOrderByDateDescStartTimeDesc(customerId, pageable))
                    .thenReturn(bookingPage);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            Page<BookingResponse> result = bookingService.getCustomerBookings(customerId, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get specific customer booking")
        void shouldGetCustomerBooking() {
            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));
            when(bookingMapper.toResponse(booking)).thenReturn(bookingResponse);

            BookingResponse result = bookingService.getCustomerBooking(customerId, bookingId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(bookingId);
        }

        @Test
        @DisplayName("Should throw exception when accessing other's booking")
        void shouldThrowWhenAccessingOthersBooking() {
            UUID otherCustomerId = UUID.randomUUID();
            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.getCustomerBooking(otherCustomerId, bookingId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("don't have access");
        }

        @Test
        @DisplayName("Should cancel booking")
        void shouldCancelBooking() {
            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.cancelBooking(customerId, bookingId);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw when cancelling non-cancellable booking")
        void shouldThrowWhenCancellingNonCancellableBooking() {
            // First approve, then complete to make it non-cancellable
            booking.approve();
            booking.complete();
            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(customerId, bookingId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be cancelled");
        }
    }

    @Nested
    @DisplayName("Owner Operations Tests")
    class OwnerOperationsTests {

        @Test
        @DisplayName("Should approve pending booking")
        void shouldApproveBooking() {
            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.approveBooking(ownerId, bookingId);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw when approving non-pending booking")
        void shouldThrowWhenApprovingNonPendingBooking() {
            booking.approve(); // Already approved
            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.approveBooking(ownerId, bookingId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be approved");
        }

        @Test
        @DisplayName("Should reject booking with reason")
        void shouldRejectBookingWithReason() {
            RejectBookingRequest request = RejectBookingRequest.builder()
                    .reason("Customer requested cancellation")
                    .build();

            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.rejectBooking(ownerId, bookingId, request);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should complete booking")
        void shouldCompleteBooking() {
            booking.approve(); // Must be approved first

            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.completeBooking(ownerId, bookingId);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should mark booking as no-show")
        void shouldMarkNoShow() {
            booking.approve(); // Must be approved first

            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.markNoShow(ownerId, bookingId);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw when booking not found for owner")
        void shouldThrowWhenBookingNotFoundForOwner() {
            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.approveBooking(ownerId, bookingId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should reject booking without reason")
        void shouldRejectBookingWithoutReason() {
            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            BookingResponse result = bookingService.rejectBooking(ownerId, bookingId, null);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw when rejecting already rejected booking")
        void shouldThrowWhenRejectingAlreadyRejectedBooking() {
            booking.reject("Previous rejection");
            when(bookingRepository.findByIdAndOwnerId(bookingId, ownerId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.rejectBooking(ownerId, bookingId, null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be rejected");
        }
    }

    @Nested
    @DisplayName("Get Owner Bookings Tests")
    class GetOwnerBookingsTests {

        @Test
        @DisplayName("Should get bookings by business and status")
        void shouldGetBookingsByBusinessAndStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Booking> bookingPage = new PageImpl<>(List.of(booking));

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(bookingRepository.findByBusinessIdAndStatus(businessId, BookingStatus.PENDING, pageable))
                    .thenReturn(bookingPage);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            Page<BookingResponse> result = bookingService.getOwnerBookings(
                    ownerId, businessId, BookingStatus.PENDING, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(bookingRepository).findByBusinessIdAndStatus(businessId, BookingStatus.PENDING, pageable);
        }

        @Test
        @DisplayName("Should get bookings by business and date")
        void shouldGetBookingsByBusinessAndDate() {
            Pageable pageable = PageRequest.of(0, 10);
            LocalDate date = LocalDate.now();
            Page<Booking> bookingPage = new PageImpl<>(List.of(booking));

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(bookingRepository.findByBusinessIdAndDate(businessId, date, pageable))
                    .thenReturn(bookingPage);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            Page<BookingResponse> result = bookingService.getOwnerBookings(
                    ownerId, businessId, null, date, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(bookingRepository).findByBusinessIdAndDate(businessId, date, pageable);
        }

        @Test
        @DisplayName("Should get all bookings by business only")
        void shouldGetAllBookingsByBusinessOnly() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Booking> bookingPage = new PageImpl<>(List.of(booking));

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(bookingRepository.findByBusinessIdOrderByDateDescStartTimeDesc(businessId, pageable))
                    .thenReturn(bookingPage);
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

            Page<BookingResponse> result = bookingService.getOwnerBookings(
                    ownerId, businessId, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(bookingRepository).findByBusinessIdOrderByDateDescStartTimeDesc(businessId, pageable);
        }

        @Test
        @DisplayName("Should throw when business ID not provided")
        void shouldThrowWhenBusinessIdNotProvided() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> bookingService.getOwnerBookings(ownerId, null, null, null, pageable))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Business ID is required");
        }

        @Test
        @DisplayName("Should throw when business not owned by user")
        void shouldThrowWhenBusinessNotOwnedByUser() {
            Pageable pageable = PageRequest.of(0, 10);
            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getOwnerBookings(ownerId, businessId, null, null, pageable))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("Booking Validation Tests")
    class BookingValidationTests {

        @Test
        @DisplayName("Should throw when booking on closed day")
        void shouldThrowWhenBookingOnClosedDay() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .build();
            workingHours.setClosed(true);
            int dayOfWeek = request.getDate().getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw when booking outside hours")
        void shouldThrowWhenBookingOutsideHours() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("07:00") // Before business opens at 09:00
                    .build();
            int dayOfWeek = request.getDate().getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("outside business hours");
        }

        @Test
        @DisplayName("Should throw when booking during break")
        void shouldThrowWhenBookingDuringBreak() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("12:30") // During lunch break
                    .build();
            int dayOfWeek = request.getDate().getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);
            workingHours.setBreakStart(LocalTime.of(12, 0));
            workingHours.setBreakEnd(LocalTime.of(13, 0));

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("during business break");
        }

        @Test
        @DisplayName("Should throw when no working hours found")
        void shouldThrowWhenNoWorkingHoursFound() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .build();
            int dayOfWeek = request.getDate().getDayOfWeek().getValue() % 7;

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("not open");
        }

        @Test
        @DisplayName("Should throw when booking in the past date")
        void shouldThrowWhenBookingInPastDate() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().minusDays(1))
                    .startTime("10:00")
                    .build();
            int dayOfWeek = request.getDate().getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findConflictingBookings(any(), any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("past date");
        }
    }

    @Nested
    @DisplayName("Available Slots Edge Cases")
    class AvailableSlotsEdgeCasesTests {

        @Test
        @DisplayName("Should return available slots when no working hours found")
        void shouldReturnClosedWhenNoWorkingHours() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.empty());

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, serviceId);

            assertThat(result.isBusinessOpen()).isFalse();
            assertThat(result.getSlots()).isEmpty();
        }

        @Test
        @DisplayName("Should return slots with break times considered")
        void shouldReturnSlotsWithBreakTimes() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);
            workingHours.setBreakStart(LocalTime.of(12, 0));
            workingHours.setBreakEnd(LocalTime.of(13, 0));

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findByBusinessIdAndDateAndStatusIn(eq(businessId), eq(date), anyList()))
                    .thenReturn(List.of());

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, serviceId);

            assertThat(result.isBusinessOpen()).isTrue();
            // Check that 12:00 and 12:30 slots during break are marked unavailable
            boolean breakSlotAvailable = result.getSlots().stream()
                    .filter(s -> s.getStartTime().equals("12:00") || s.getStartTime().equals("12:30"))
                    .anyMatch(TimeSlotResponse.Slot::isAvailable);
            assertThat(breakSlotAvailable).isFalse();
        }

        @Test
        @DisplayName("Should get slots without service ID")
        void shouldGetSlotsWithoutServiceId() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findByBusinessIdAndDateAndStatusIn(eq(businessId), eq(date), anyList()))
                    .thenReturn(List.of());

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, null);

            assertThat(result.isBusinessOpen()).isTrue();
            verify(serviceRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw when service not found")
        void shouldThrowWhenServiceNotFound() {
            LocalDate date = LocalDate.now().plusDays(1);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getAvailableSlots(businessId, date, serviceId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Booking Not Found Tests")
    class BookingNotFoundTests {

        @Test
        @DisplayName("Should throw when booking not found for customer")
        void shouldThrowWhenBookingNotFoundForCustomer() {
            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getCustomerBooking(customerId, bookingId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when booking not found for cancellation")
        void shouldThrowWhenBookingNotFoundForCancel() {
            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancelBooking(customerId, bookingId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when accessing guest booking as wrong customer")
        void shouldThrowWhenAccessingGuestBookingAsCustomer() {
            Booking guestBooking = Booking.builder()
                    .business(business)
                    .service(service)
                    .guestName("Guest")
                    .guestPhone("0501234567")
                    .date(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .status(BookingStatus.PENDING)
                    .build();
            guestBooking.setId(bookingId);

            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(guestBooking));

            assertThatThrownBy(() -> bookingService.getCustomerBooking(customerId, bookingId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("don't have access");
        }

        @Test
        @DisplayName("Should throw when cancelling guest booking as wrong customer")
        void shouldThrowWhenCancellingGuestBookingAsCustomer() {
            Booking guestBooking = Booking.builder()
                    .business(business)
                    .service(service)
                    .guestName("Guest")
                    .guestPhone("0501234567")
                    .date(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .status(BookingStatus.PENDING)
                    .build();
            guestBooking.setId(bookingId);

            when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(guestBooking));

            assertThatThrownBy(() -> bookingService.cancelBooking(customerId, bookingId))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("don't have access");
        }
    }

    @Nested
    @DisplayName("Business and Service Validation Tests")
    class BusinessServiceValidationTests {

        @Test
        @DisplayName("Should throw when business not found for booking")
        void shouldThrowWhenBusinessNotFoundForBooking() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .build();

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when service not found for booking")
        void shouldThrowWhenServiceNotFoundForBooking() {
            CreateBookingRequest request = CreateBookingRequest.builder()
                    .businessId(businessId)
                    .serviceId(serviceId)
                    .date(LocalDate.now().plusDays(1))
                    .startTime("10:00")
                    .build();

            when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Slot Conflict Detection Tests")
    class SlotConflictTests {

        @Test
        @DisplayName("Should detect overlapping booking conflict")
        void shouldDetectOverlappingBookingConflict() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);

            // Existing booking from 10:00 to 11:00
            Booking existingBooking = Booking.builder()
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .status(BookingStatus.APPROVED)
                    .build();

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findByBusinessIdAndDateAndStatusIn(eq(businessId), eq(date), anyList()))
                    .thenReturn(List.of(existingBooking));

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, serviceId);

            // The slot at 10:00 should be unavailable
            boolean tenAmSlotAvailable = result.getSlots().stream()
                    .filter(s -> s.getStartTime().equals("10:00"))
                    .findFirst()
                    .map(TimeSlotResponse.Slot::isAvailable)
                    .orElse(true);

            assertThat(tenAmSlotAvailable).isFalse();
        }

        @Test
        @DisplayName("Should mark slot at end of day boundary correctly")
        void shouldHandleEndOfDayBoundary() {
            LocalDate date = LocalDate.now().plusDays(1);
            int dayOfWeek = date.getDayOfWeek().getValue() % 7;
            workingHours.setDayOfWeek(dayOfWeek);
            workingHours.setStartTime(LocalTime.of(16, 0));
            workingHours.setEndTime(LocalTime.of(18, 0));

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
            when(workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek))
                    .thenReturn(Optional.of(workingHours));
            when(bookingRepository.findByBusinessIdAndDateAndStatusIn(eq(businessId), eq(date), anyList()))
                    .thenReturn(List.of());

            TimeSlotResponse result = bookingService.getAvailableSlots(businessId, date, serviceId);

            // Should have limited slots due to short working window
            assertThat(result.isBusinessOpen()).isTrue();
            assertThat(result.getSlots()).isNotEmpty();
        }
    }
}
