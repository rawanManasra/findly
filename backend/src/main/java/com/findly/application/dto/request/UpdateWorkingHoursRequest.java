package com.findly.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkingHoursRequest {

    @NotNull(message = "Working hours list is required")
    @Size(min = 7, max = 7, message = "Must provide working hours for all 7 days")
    @Valid
    private List<DayHoursRequest> hours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayHoursRequest {

        @NotNull(message = "Day of week is required")
        private Integer dayOfWeek; // 0 = Sunday, 6 = Saturday

        private String startTime; // HH:mm format

        private String endTime; // HH:mm format

        private boolean closed;

        private String breakStart; // HH:mm format

        private String breakEnd; // HH:mm format
    }
}
