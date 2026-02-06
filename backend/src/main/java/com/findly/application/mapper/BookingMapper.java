package com.findly.application.mapper;

import com.findly.application.dto.response.BookingResponse;
import com.findly.domain.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "businessName", source = "business.name")
    @Mapping(target = "businessPhone", source = "business.phone")
    @Mapping(target = "businessAddress", expression = "java(booking.getBusiness().getFullAddress())")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "serviceDurationMins", source = "service.durationMins")
    @Mapping(target = "servicePrice", expression = "java(booking.getService().getFormattedPrice())")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(booking.getCustomerName())")
    @Mapping(target = "customerPhone", expression = "java(booking.getCustomerPhone())")
    @Mapping(target = "customerEmail", expression = "java(booking.getCustomerEmail())")
    @Mapping(target = "guestBooking", expression = "java(booking.isGuestBooking())")
    @Mapping(target = "startTime", expression = "java(formatTime(booking.getStartTime()))")
    @Mapping(target = "endTime", expression = "java(formatTime(booking.getEndTime()))")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    default String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }
}
