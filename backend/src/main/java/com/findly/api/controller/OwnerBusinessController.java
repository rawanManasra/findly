package com.findly.api.controller;

import com.findly.application.dto.request.*;
import com.findly.application.dto.response.BusinessDetailResponse;
import com.findly.application.dto.response.ServiceResponse;
import com.findly.application.dto.response.WorkingHoursResponse;
import com.findly.application.service.BusinessService;
import com.findly.application.service.ServiceService;
import com.findly.application.service.WorkingHoursService;
import com.findly.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner/businesses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUSINESS_OWNER')")
@Tag(name = "Owner - Businesses", description = "Business owner management endpoints")
public class OwnerBusinessController {

    private final BusinessService businessService;
    private final ServiceService serviceService;
    private final WorkingHoursService workingHoursService;

    // ==================== Business CRUD ====================

    @PostMapping
    @Operation(summary = "Create a new business")
    public ResponseEntity<BusinessDetailResponse> createBusiness(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBusinessRequest request) {

        BusinessDetailResponse business = businessService.createBusiness(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(business);
    }

    @GetMapping
    @Operation(summary = "Get all my businesses")
    public ResponseEntity<List<BusinessDetailResponse>> getMyBusinesses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<BusinessDetailResponse> businesses = businessService.getOwnerBusinesses(userDetails.getId());
        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my business by ID")
    public ResponseEntity<BusinessDetailResponse> getMyBusiness(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {

        BusinessDetailResponse business = businessService.getOwnerBusiness(userDetails.getId(), id);
        return ResponseEntity.ok(business);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update my business")
    public ResponseEntity<BusinessDetailResponse> updateBusiness(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBusinessRequest request) {

        BusinessDetailResponse business = businessService.updateBusiness(userDetails.getId(), id, request);
        return ResponseEntity.ok(business);
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "Update business location")
    public ResponseEntity<BusinessDetailResponse> updateLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequest request) {

        BusinessDetailResponse business = businessService.updateLocation(userDetails.getId(), id, request);
        return ResponseEntity.ok(business);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete my business")
    public ResponseEntity<Void> deleteBusiness(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {

        businessService.deleteBusiness(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Services Management ====================

    @PostMapping("/{businessId}/services")
    @Operation(summary = "Add a service to my business")
    public ResponseEntity<ServiceResponse> addService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateServiceRequest request) {

        ServiceResponse service = serviceService.createService(userDetails.getId(), businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    @GetMapping("/{businessId}/services")
    @Operation(summary = "Get all services for my business")
    public ResponseEntity<List<ServiceResponse>> getMyServices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID businessId) {

        List<ServiceResponse> services = serviceService.getServicesByBusiness(userDetails.getId(), businessId);
        return ResponseEntity.ok(services);
    }

    @PutMapping("/{businessId}/services/{serviceId}")
    @Operation(summary = "Update a service")
    public ResponseEntity<ServiceResponse> updateService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID businessId,
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceRequest request) {

        ServiceResponse service = serviceService.updateService(userDetails.getId(), businessId, serviceId, request);
        return ResponseEntity.ok(service);
    }

    @DeleteMapping("/{businessId}/services/{serviceId}")
    @Operation(summary = "Delete a service")
    public ResponseEntity<Void> deleteService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID businessId,
            @PathVariable UUID serviceId) {

        serviceService.deleteService(userDetails.getId(), businessId, serviceId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Working Hours Management ====================

    @GetMapping("/{businessId}/hours")
    @Operation(summary = "Get working hours for my business")
    public ResponseEntity<List<WorkingHoursResponse>> getWorkingHours(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID businessId) {

        List<WorkingHoursResponse> hours = workingHoursService.getWorkingHoursForOwner(userDetails.getId(), businessId);
        return ResponseEntity.ok(hours);
    }

    @PutMapping("/{businessId}/hours")
    @Operation(summary = "Update working hours (all days)")
    public ResponseEntity<List<WorkingHoursResponse>> updateWorkingHours(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID businessId,
            @Valid @RequestBody UpdateWorkingHoursRequest request) {

        List<WorkingHoursResponse> hours = workingHoursService.updateWorkingHours(
                userDetails.getId(), businessId, request);
        return ResponseEntity.ok(hours);
    }
}
