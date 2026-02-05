package com.findly.api.controller;

import com.findly.application.dto.response.BusinessDetailResponse;
import com.findly.application.dto.response.BusinessResponse;
import com.findly.application.dto.response.ServiceResponse;
import com.findly.application.dto.response.WorkingHoursResponse;
import com.findly.application.service.BusinessService;
import com.findly.application.service.ServiceService;
import com.findly.application.service.WorkingHoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
@Tag(name = "Businesses", description = "Public business endpoints")
public class BusinessController {

    private final BusinessService businessService;
    private final ServiceService serviceService;
    private final WorkingHoursService workingHoursService;

    @GetMapping
    @Operation(summary = "Search nearby businesses", description = "Find businesses within a radius of a location")
    public ResponseEntity<Page<BusinessResponse>> searchNearby(
            @Parameter(description = "User's latitude", required = true)
            @RequestParam double lat,

            @Parameter(description = "User's longitude", required = true)
            @RequestParam double lng,

            @Parameter(description = "Search radius in meters (default: 5000)")
            @RequestParam(required = false) Double radius,

            @Parameter(description = "Search term for name/description")
            @RequestParam(required = false) String q,

            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) UUID category,

            @PageableDefault(size = 20) Pageable pageable) {

        Page<BusinessResponse> businesses = businessService.searchNearby(
                lat, lng, radius, q, category, pageable);

        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get business details")
    public ResponseEntity<BusinessDetailResponse> getBusinessById(
            @PathVariable UUID id) {

        BusinessDetailResponse business = businessService.getBusinessById(id);
        return ResponseEntity.ok(business);
    }

    @GetMapping("/{id}/services")
    @Operation(summary = "Get business services")
    public ResponseEntity<List<ServiceResponse>> getBusinessServices(
            @PathVariable UUID id) {

        List<ServiceResponse> services = serviceService.getActiveServicesByBusiness(id);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}/hours")
    @Operation(summary = "Get business working hours")
    public ResponseEntity<List<WorkingHoursResponse>> getBusinessHours(
            @PathVariable UUID id) {

        List<WorkingHoursResponse> hours = workingHoursService.getWorkingHours(id);
        return ResponseEntity.ok(hours);
    }
}
