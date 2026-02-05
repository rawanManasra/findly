package com.findly.application.mapper;

import com.findly.application.dto.response.WorkingHoursResponse;
import com.findly.domain.entity.WorkingHours;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkingHoursMapper {

    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mapping(target = "dayName", expression = "java(workingHours.getDayName())")
    @Mapping(target = "startTime", expression = "java(formatTime(workingHours.getStartTime()))")
    @Mapping(target = "endTime", expression = "java(formatTime(workingHours.getEndTime()))")
    @Mapping(target = "breakStart", expression = "java(formatTime(workingHours.getBreakStart()))")
    @Mapping(target = "breakEnd", expression = "java(formatTime(workingHours.getBreakEnd()))")
    @Mapping(target = "hasBreak", expression = "java(workingHours.hasBreak())")
    WorkingHoursResponse toResponse(WorkingHours workingHours);

    List<WorkingHoursResponse> toResponseList(List<WorkingHours> workingHours);

    default String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }
}
