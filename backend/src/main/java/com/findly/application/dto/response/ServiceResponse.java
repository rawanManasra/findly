package com.findly.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private UUID id;
    private UUID businessId;
    private String name;
    private String description;
    private Integer durationMins;
    private String formattedDuration;
    private BigDecimal price;
    private String currency;
    private String formattedPrice;
    private boolean active;
    private Integer sortOrder;
}
