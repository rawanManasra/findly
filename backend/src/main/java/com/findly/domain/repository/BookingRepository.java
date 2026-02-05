package com.findly.domain.repository;

import com.findly.domain.entity.Booking;
import com.findly.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Customer bookings
    Page<Booking> findByCustomerIdOrderByDateDescStartTimeDesc(UUID customerId, Pageable pageable);

    List<Booking> findByCustomerIdAndStatusIn(UUID customerId, List<BookingStatus> statuses);

    // Business bookings
    Page<Booking> findByBusinessIdOrderByDateDescStartTimeDesc(UUID businessId, Pageable pageable);

    Page<Booking> findByBusinessIdAndStatus(UUID businessId, BookingStatus status, Pageable pageable);

    Page<Booking> findByBusinessIdAndDate(UUID businessId, LocalDate date, Pageable pageable);

    List<Booking> findByBusinessIdAndDateAndStatusIn(UUID businessId, LocalDate date, List<BookingStatus> statuses);

    // Check for conflicts
    @Query("""
        SELECT b FROM Booking b
        WHERE b.business.id = :businessId
        AND b.date = :date
        AND b.status IN :statuses
        AND ((b.startTime < :endTime AND b.endTime > :startTime))
        """)
    List<Booking> findConflictingBookings(
            @Param("businessId") UUID businessId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") List<BookingStatus> statuses);

    // Count by status for dashboard
    long countByBusinessIdAndStatus(UUID businessId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.business.id = :businessId AND b.date = :date AND b.status IN :statuses")
    long countByBusinessIdAndDateAndStatusIn(
            @Param("businessId") UUID businessId,
            @Param("date") LocalDate date,
            @Param("statuses") List<BookingStatus> statuses);

    // Get booking with all relationships
    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.business
        LEFT JOIN FETCH b.service
        LEFT JOIN FETCH b.customer
        WHERE b.id = :id
        """)
    Optional<Booking> findByIdWithDetails(@Param("id") UUID id);

    // Owner access check
    @Query("""
        SELECT b FROM Booking b
        WHERE b.id = :bookingId
        AND b.business.owner.id = :ownerId
        """)
    Optional<Booking> findByIdAndOwnerId(@Param("bookingId") UUID bookingId, @Param("ownerId") UUID ownerId);
}
