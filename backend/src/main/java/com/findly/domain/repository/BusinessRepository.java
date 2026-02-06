package com.findly.domain.repository;

import com.findly.domain.entity.Business;
import com.findly.domain.enums.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    List<Business> findByOwnerId(UUID ownerId);

    Page<Business> findByOwnerIdAndStatus(UUID ownerId, BusinessStatus status, Pageable pageable);

    Optional<Business> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<Business> findByCategoryIdAndStatus(UUID categoryId, BusinessStatus status, Pageable pageable);

    /**
     * Find businesses within a radius (in meters) of a given point.
     * Uses PostGIS ST_DWithin for efficient spatial queries.
     *
     * @param longitude User's longitude
     * @param latitude User's latitude
     * @param radiusMeters Search radius in meters
     * @param status Business status filter
     * @param pageable Pagination info
     * @return Page of businesses with distance
     */
    @Query(value = """
        SELECT b.*,
               ST_Distance(b.location, CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)) as distance
        FROM businesses b
        WHERE ST_DWithin(
            b.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusMeters
        )
        AND b.status = CAST(:#{#status.name()} AS business_status)
        AND b.deleted_at IS NULL
        ORDER BY distance
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM businesses b
        WHERE ST_DWithin(
            b.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusMeters
        )
        AND b.status = CAST(:#{#status.name()} AS business_status)
        AND b.deleted_at IS NULL
        """,
        nativeQuery = true)
    Page<Object[]> findNearbyBusinesses(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("status") BusinessStatus status,
            Pageable pageable);

    /**
     * Find businesses within a radius with category filter.
     */
    @Query(value = """
        SELECT b.*,
               ST_Distance(b.location, CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)) as distance
        FROM businesses b
        WHERE ST_DWithin(
            b.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusMeters
        )
        AND b.status = CAST(:#{#status.name()} AS business_status)
        AND b.category_id = :categoryId
        AND b.deleted_at IS NULL
        ORDER BY distance
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM businesses b
        WHERE ST_DWithin(
            b.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusMeters
        )
        AND b.status = CAST(:#{#status.name()} AS business_status)
        AND b.category_id = :categoryId
        AND b.deleted_at IS NULL
        """,
        nativeQuery = true)
    Page<Object[]> findNearbyBusinessesByCategory(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("categoryId") UUID categoryId,
            @Param("status") BusinessStatus status,
            Pageable pageable);

    /**
     * Search businesses by name within a radius.
     */
    @Query(value = """
        SELECT b.*,
               ST_Distance(b.location, CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)) as distance
        FROM businesses b
        WHERE ST_DWithin(
            b.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusMeters
        )
        AND b.status = CAST(:#{#status.name()} AS business_status)
        AND (LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND b.deleted_at IS NULL
        ORDER BY distance
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM businesses b
        WHERE ST_DWithin(
            b.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusMeters
        )
        AND b.status = CAST(:#{#status.name()} AS business_status)
        AND (LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND b.deleted_at IS NULL
        """,
        nativeQuery = true)
    Page<Object[]> searchNearbyBusinesses(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("searchTerm") String searchTerm,
            @Param("status") BusinessStatus status,
            Pageable pageable);

    long countByStatus(BusinessStatus status);
}
