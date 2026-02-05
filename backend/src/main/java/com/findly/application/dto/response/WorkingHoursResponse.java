package com.findly.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursResponse {

    private UUID id;
    private Integer dayOfWeek; // 0 = Sunday, 6 = Saturday
    private String dayName;
    private String startTime; // HH:mm format
    private String endTime;
    private boolean closed;
    private String breakStart;
    private String breakEnd;
    private boolean hasBreak;
}
