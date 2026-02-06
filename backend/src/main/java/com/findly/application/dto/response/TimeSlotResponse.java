package com.findly.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {

    private LocalDate date;
    private boolean businessOpen;
    private String openTime;
    private String closeTime;
    private List<Slot> slots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Slot {
        private String startTime; // HH:mm
        private String endTime;   // HH:mm
        private boolean available;
    }
}
