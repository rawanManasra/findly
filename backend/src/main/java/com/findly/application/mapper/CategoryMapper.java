package com.findly.application.mapper;

import com.findly.application.dto.response.CategoryResponse;
import com.findly.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", ignore = true)
    CategoryResponse toResponse(Category category);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", source = "children")
    CategoryResponse toResponseWithChildren(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);
}
