package com.findly.application.service;

import com.findly.api.exception.ApiException;
import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.request.CreateServiceRequest;
import com.findly.application.dto.request.UpdateServiceRequest;
import com.findly.application.dto.response.ServiceResponse;
import com.findly.application.mapper.ServiceMapper;
import com.findly.domain.entity.Business;
import com.findly.domain.entity.Service;
import com.findly.domain.repository.BusinessRepository;
import com.findly.domain.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final ServiceMapper serviceMapper;

    /**
     * Get active services for a business (public).
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> getActiveServicesByBusiness(UUID businessId) {
        // Verify business exists
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", "id", businessId);
        }

        List<Service> services = serviceRepository.findByBusinessIdAndActiveTrueOrderBySortOrderAsc(businessId);
        return serviceMapper.toResponseList(services);
    }

    /**
     * Get all services for a business (owner).
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByBusiness(UUID ownerId, UUID businessId) {
        verifyBusinessOwnership(businessId, ownerId);

        List<Service> services = serviceRepository.findByBusinessIdOrderBySortOrderAsc(businessId);
        return serviceMapper.toResponseList(services);
    }

    /**
     * Create a new service (owner).
     */
    @Transactional
    public ServiceResponse createService(UUID ownerId, UUID businessId, CreateServiceRequest request) {
        log.info("Creating service for business: {}", businessId);

        Business business = verifyBusinessOwnership(businessId, ownerId);

        // Check for duplicate name
        if (serviceRepository.existsByBusinessIdAndName(businessId, request.getName().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_SERVICE",
                    "A service with this name already exists");
        }

        Service service = Service.builder()
                .business(business)
                .name(request.getName().trim())
                .description(request.getDescription())
                .durationMins(request.getDurationMins())
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "ILS")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(true)
                .build();

        service = serviceRepository.save(service);

        log.info("Service created: {}", service.getId());

        return serviceMapper.toResponse(service);
    }

    /**
     * Update a service (owner).
     */
    @Transactional
    public ServiceResponse updateService(UUID ownerId, UUID businessId, UUID serviceId, UpdateServiceRequest request) {
        log.info("Updating service: {} for business: {}", serviceId, businessId);

        verifyBusinessOwnership(businessId, ownerId);

        Service service = serviceRepository.findByIdAndBusinessId(serviceId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));

        if (request.getName() != null) {
            // Check for duplicate name (excluding current service)
            if (!service.getName().equals(request.getName().trim()) &&
                serviceRepository.existsByBusinessIdAndName(businessId, request.getName().trim())) {
                throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_SERVICE",
                        "A service with this name already exists");
            }
            service.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getDurationMins() != null) {
            service.setDurationMins(request.getDurationMins());
        }
        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null) {
            service.setCurrency(request.getCurrency());
        }
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }
        if (request.getSortOrder() != null) {
            service.setSortOrder(request.getSortOrder());
        }

        service = serviceRepository.save(service);

        log.info("Service updated: {}", service.getId());

        return serviceMapper.toResponse(service);
    }

    /**
     * Delete a service (owner).
     */
    @Transactional
    public void deleteService(UUID ownerId, UUID businessId, UUID serviceId) {
        log.info("Deleting service: {} for business: {}", serviceId, businessId);

        verifyBusinessOwnership(businessId, ownerId);

        Service service = serviceRepository.findByIdAndBusinessId(serviceId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));

        serviceRepository.delete(service);

        log.info("Service deleted: {}", serviceId);
    }

    // Helper methods

    private Business verifyBusinessOwnership(UUID businessId, UUID ownerId) {
        return businessRepository.findByIdAndOwnerId(businessId, ownerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "NOT_FOUND",
                        "Business not found or you don't have access"));
    }
}
