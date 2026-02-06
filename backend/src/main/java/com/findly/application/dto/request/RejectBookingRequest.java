package com.findly.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectBookingRequest {

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
