package com.findly.application.mapper;

import com.findly.application.dto.response.ServiceResponse;
import com.findly.domain.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceMapper {

    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "formattedDuration", expression = "java(service.getFormattedDuration())")
    @Mapping(target = "formattedPrice", expression = "java(service.getFormattedPrice())")
    ServiceResponse toResponse(Service service);

    List<ServiceResponse> toResponseList(List<Service> services);
}
