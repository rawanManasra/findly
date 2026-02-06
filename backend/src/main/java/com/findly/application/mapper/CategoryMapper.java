package com.findly.application.mapper;

import com.findly.application.dto.response.CategoryResponse;
import com.findly.domain.entity.Category;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Named("toSimpleResponse")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", ignore = true)
    CategoryResponse toResponse(Category category);

    @Named("toResponseWithChildren")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", source = "children")
    CategoryResponse toResponseWithChildren(Category category);

    @IterableMapping(qualifiedByName = "toSimpleResponse")
    List<CategoryResponse> toResponseList(List<Category> categories);
}
