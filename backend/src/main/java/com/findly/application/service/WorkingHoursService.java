package com.findly.application.service;

import com.findly.api.exception.ApiException;
import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.request.UpdateWorkingHoursRequest;
import com.findly.application.dto.response.WorkingHoursResponse;
import com.findly.application.mapper.WorkingHoursMapper;
import com.findly.domain.entity.Business;
import com.findly.domain.entity.WorkingHours;
import com.findly.domain.repository.BusinessRepository;
import com.findly.domain.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;
    private final BusinessRepository businessRepository;
    private final WorkingHoursMapper workingHoursMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Get working hours for a business (public).
     */
    @Transactional(readOnly = true)
    public List<WorkingHoursResponse> getWorkingHours(UUID businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", "id", businessId);
        }

        List<WorkingHours> hours = workingHoursRepository.findByBusinessIdOrderByDayOfWeekAsc(businessId);

        // If no working hours exist, return default (all days closed)
        if (hours.isEmpty()) {
            return getDefaultWorkingHours();
        }

        return workingHoursMapper.toResponseList(hours);
    }

    /**
     * Get working hours for a business (owner).
     */
    @Transactional(readOnly = true)
    public List<WorkingHoursResponse> getWorkingHoursForOwner(UUID ownerId, UUID businessId) {
        verifyBusinessOwnership(businessId, ownerId);
        return getWorkingHours(businessId);
    }

    /**
     * Update all working hours for a business (owner).
     */
    @Transactional
    public List<WorkingHoursResponse> updateWorkingHours(UUID ownerId, UUID businessId, UpdateWorkingHoursRequest request) {
        log.info("Updating working hours for business: {}", businessId);

        Business business = verifyBusinessOwnership(businessId, ownerId);

        // Delete existing working hours
        workingHoursRepository.deleteByBusinessId(businessId);

        // Create new working hours
        List<WorkingHours> newHours = new ArrayList<>();

        for (UpdateWorkingHoursRequest.DayHoursRequest dayRequest : request.getHours()) {
            WorkingHours wh = WorkingHours.builder()
                    .business(business)
                    .dayOfWeek(dayRequest.getDayOfWeek())
                    .closed(dayRequest.isClosed())
                    .startTime(parseTime(dayRequest.getStartTime()))
                    .endTime(parseTime(dayRequest.getEndTime()))
                    .breakStart(parseTime(dayRequest.getBreakStart()))
                    .breakEnd(parseTime(dayRequest.getBreakEnd()))
                    .build();

            newHours.add(wh);
        }

        List<WorkingHours> saved = workingHoursRepository.saveAll(newHours);

        log.info("Working hours updated for business: {}", businessId);

        return workingHoursMapper.toResponseList(saved);
    }

    /**
     * Initialize default working hours for a new business.
     */
    @Transactional
    public void initializeDefaultHours(Business business) {
        List<WorkingHours> defaultHours = new ArrayList<>();

        for (int day = 0; day < 7; day++) {
            WorkingHours wh = WorkingHours.builder()
                    .business(business)
                    .dayOfWeek(day)
                    .closed(day == 6) // Saturday closed by default (Israel)
                    .startTime(day != 6 ? LocalTime.of(9, 0) : null)
                    .endTime(day != 6 ? LocalTime.of(18, 0) : null)
                    .build();

            defaultHours.add(wh);
        }

        workingHoursRepository.saveAll(defaultHours);
    }

    // Helper methods

    private Business verifyBusinessOwnership(UUID businessId, UUID ownerId) {
        return businessRepository.findByIdAndOwnerId(businessId, ownerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "NOT_FOUND",
                        "Business not found or you don't have access"));
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    private List<WorkingHoursResponse> getDefaultWorkingHours() {
        List<WorkingHoursResponse> defaults = new ArrayList<>();

        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (int day = 0; day < 7; day++) {
            WorkingHoursResponse wh = WorkingHoursResponse.builder()
                    .dayOfWeek(day)
                    .dayName(dayNames[day])
                    .closed(true)
                    .hasBreak(false)
                    .build();
            defaults.add(wh);
        }

        return defaults;
    }
}
