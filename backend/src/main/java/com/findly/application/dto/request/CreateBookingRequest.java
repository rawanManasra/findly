package com.findly.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    private LocalDate date;

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format (HH:mm)")
    private String startTime;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Guest info (required if not authenticated)
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String guestName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String guestPhone;

    @Email(message = "Invalid email format")
    private String guestEmail;
}
