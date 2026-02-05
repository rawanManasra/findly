package com.findly.application.service;

import com.findly.api.exception.ApiException;
import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.request.CreateBusinessRequest;
import com.findly.application.dto.request.UpdateBusinessRequest;
import com.findly.application.dto.request.UpdateLocationRequest;
import com.findly.application.dto.response.BusinessDetailResponse;
import com.findly.application.dto.response.BusinessResponse;
import com.findly.application.mapper.BusinessMapper;
import com.findly.domain.entity.Business;
import com.findly.domain.entity.Category;
import com.findly.domain.entity.User;
import com.findly.domain.enums.BusinessStatus;
import com.findly.domain.repository.BusinessRepository;
import com.findly.domain.repository.CategoryRepository;
import com.findly.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessMapper businessMapper;

    private static final double DEFAULT_RADIUS_METERS = 5000.0; // 5km

    /**
     * Search for nearby businesses within a radius.
     */
    @Transactional(readOnly = true)
    public Page<BusinessResponse> searchNearby(
            double latitude,
            double longitude,
            Double radiusMeters,
            String searchTerm,
            UUID categoryId,
            Pageable pageable) {

        double radius = radiusMeters != null ? radiusMeters : DEFAULT_RADIUS_METERS;

        log.debug("Searching businesses near ({}, {}) within {} meters", latitude, longitude, radius);

        Page<Object[]> results;

        if (searchTerm != null && !searchTerm.isBlank()) {
            results = businessRepository.searchNearbyBusinesses(
                    longitude, latitude, radius, searchTerm.trim(), BusinessStatus.ACTIVE, pageable);
        } else if (categoryId != null) {
            results = businessRepository.findNearbyBusinessesByCategory(
                    longitude, latitude, radius, categoryId, BusinessStatus.ACTIVE, pageable);
        } else {
            results = businessRepository.findNearbyBusinesses(
                    longitude, latitude, radius, BusinessStatus.ACTIVE, pageable);
        }

        List<BusinessResponse> businesses = results.getContent().stream()
                .map(this::mapToBusinessResponseWithDistance)
                .toList();

        return new PageImpl<>(businesses, pageable, results.getTotalElements());
    }

    /**
     * Get business details by ID (public).
     */
    @Transactional(readOnly = true)
    public BusinessDetailResponse getBusinessById(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", businessId));

        if (business.getStatus() != BusinessStatus.ACTIVE) {
            throw new ResourceNotFoundException("Business", "id", businessId);
        }

        return businessMapper.toDetailResponse(business);
    }

    /**
     * Create a new business (owner).
     */
    @Transactional
    public BusinessDetailResponse createBusiness(UUID ownerId, CreateBusinessRequest request) {
        log.info("Creating business for owner: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Business business = Business.builder()
                .owner(owner)
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(category)
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .imageUrl(request.getImageUrl())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry() != null ? request.getCountry() : "Israel")
                .status(BusinessStatus.PENDING_APPROVAL)
                .build();

        // Set location
        business.updateLocation(request.getLatitude(), request.getLongitude());

        business = businessRepository.save(business);

        log.info("Business created: {}", business.getId());

        return businessMapper.toOwnerDetailResponse(business);
    }

    /**
     * Update a business (owner).
     */
    @Transactional
    public BusinessDetailResponse updateBusiness(UUID ownerId, UUID businessId, UpdateBusinessRequest request) {
        log.info("Updating business: {} by owner: {}", businessId, ownerId);

        Business business = getBusinessForOwner(businessId, ownerId);

        if (request.getName() != null) {
            business.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            business.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            business.setCategory(category);
        }
        if (request.getPhone() != null) {
            business.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            business.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null) {
            business.setWebsite(request.getWebsite());
        }
        if (request.getImageUrl() != null) {
            business.setImageUrl(request.getImageUrl());
        }
        if (request.getAddressLine1() != null) {
            business.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            business.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            business.setCity(request.getCity());
        }
        if (request.getState() != null) {
            business.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            business.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            business.setCountry(request.getCountry());
        }

        business = businessRepository.save(business);

        log.info("Business updated: {}", business.getId());

        return businessMapper.toOwnerDetailResponse(business);
    }

    /**
     * Update business location (owner).
     */
    @Transactional
    public BusinessDetailResponse updateLocation(UUID ownerId, UUID businessId, UpdateLocationRequest request) {
        log.info("Updating location for business: {}", businessId);

        Business business = getBusinessForOwner(businessId, ownerId);

        business.updateLocation(request.getLatitude(), request.getLongitude());

        business = businessRepository.save(business);

        return businessMapper.toOwnerDetailResponse(business);
    }

    /**
     * Get all businesses for an owner.
     */
    @Transactional(readOnly = true)
    public List<BusinessDetailResponse> getOwnerBusinesses(UUID ownerId) {
        List<Business> businesses = businessRepository.findByOwnerId(ownerId);

        return businesses.stream()
                .map(businessMapper::toOwnerDetailResponse)
                .toList();
    }

    /**
     * Get a specific business for an owner.
     */
    @Transactional(readOnly = true)
    public BusinessDetailResponse getOwnerBusiness(UUID ownerId, UUID businessId) {
        Business business = getBusinessForOwner(businessId, ownerId);
        return businessMapper.toOwnerDetailResponse(business);
    }

    /**
     * Soft delete a business (owner).
     */
    @Transactional
    public void deleteBusiness(UUID ownerId, UUID businessId) {
        log.info("Deleting business: {} by owner: {}", businessId, ownerId);

        Business business = getBusinessForOwner(businessId, ownerId);

        business.softDelete();
        businessRepository.save(business);

        log.info("Business deleted: {}", businessId);
    }

    /**
     * Activate a business (admin or auto-approval).
     */
    @Transactional
    public BusinessDetailResponse activateBusiness(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", businessId));

        business.setStatus(BusinessStatus.ACTIVE);
        business = businessRepository.save(business);

        log.info("Business activated: {}", businessId);

        return businessMapper.toOwnerDetailResponse(business);
    }

    // Helper methods

    private Business getBusinessForOwner(UUID businessId, UUID ownerId) {
        return businessRepository.findByIdAndOwnerId(businessId, ownerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "NOT_FOUND",
                        "Business not found or you don't have access"));
    }

    private BusinessResponse mapToBusinessResponseWithDistance(Object[] row) {
        // Native query returns: business columns + distance
        // We need to map the business entity and add distance

        // The row[0] through row[n-1] are business columns, row[n] is distance
        // Since we're using native query, we need to reconstruct the Business entity
        // For simplicity, let's use a different approach with EntityManager or adjust the query

        // For now, let's assume the last column is distance
        int lastIndex = row.length - 1;
        Double distance = row[lastIndex] != null ? ((Number) row[lastIndex]).doubleValue() : null;

        // Extract business ID (first column) and fetch the entity
        UUID businessId = (UUID) row[0];
        Business business = businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            return null;
        }

        return businessMapper.toResponseWithDistance(business, distance);
    }
}
