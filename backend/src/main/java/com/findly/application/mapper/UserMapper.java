package com.findly.application.mapper;

import com.findly.application.dto.response.UserResponse;
import com.findly.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toResponse(User user);
}
