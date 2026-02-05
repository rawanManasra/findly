package com.findly.application.dto.response;

import com.findly.domain.enums.BusinessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDetailResponse {

    private UUID id;
    private String name;
    private String description;
    private CategoryResponse category;
    private String phone;
    private String email;
    private String website;
    private String imageUrl;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String fullAddress;

    // Location
    private Double latitude;
    private Double longitude;
    private Double distanceMeters;

    // Status
    private BusinessStatus status;
    private boolean verified;
    private BigDecimal ratingAvg;
    private Integer ratingCount;

    // Related data
    private List<ServiceResponse> services;
    private List<WorkingHoursResponse> workingHours;

    // Owner info (only for owner view)
    private UserResponse owner;

    private Instant createdAt;
}
