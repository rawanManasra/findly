package com.findly.application.mapper;

import com.findly.application.dto.response.BusinessDetailResponse;
import com.findly.application.dto.response.BusinessResponse;
import com.findly.domain.entity.Business;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, ServiceMapper.class, WorkingHoursMapper.class, UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessMapper {

    @Mapping(target = "latitude", expression = "java(business.getLatitude())")
    @Mapping(target = "longitude", expression = "java(business.getLongitude())")
    @Mapping(target = "fullAddress", expression = "java(business.getFullAddress())")
    @Mapping(target = "distanceMeters", ignore = true)
    @Mapping(target = "category", qualifiedByName = "toSimpleResponse")
    BusinessResponse toResponse(Business business);

    @Mapping(target = "latitude", expression = "java(business.getLatitude())")
    @Mapping(target = "longitude", expression = "java(business.getLongitude())")
    @Mapping(target = "fullAddress", expression = "java(business.getFullAddress())")
    @Mapping(target = "distanceMeters", ignore = true)
    @Mapping(target = "services", source = "services")
    @Mapping(target = "workingHours", source = "workingHours")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "category", qualifiedByName = "toSimpleResponse")
    BusinessDetailResponse toDetailResponse(Business business);

    @Mapping(target = "latitude", expression = "java(business.getLatitude())")
    @Mapping(target = "longitude", expression = "java(business.getLongitude())")
    @Mapping(target = "fullAddress", expression = "java(business.getFullAddress())")
    @Mapping(target = "services", source = "services")
    @Mapping(target = "workingHours", source = "workingHours")
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "category", qualifiedByName = "toSimpleResponse")
    BusinessDetailResponse toOwnerDetailResponse(Business business);

    default BusinessResponse toResponseWithDistance(Business business, Double distanceMeters) {
        BusinessResponse response = toResponse(business);
        response.setDistanceMeters(distanceMeters);
        return response;
    }
}
