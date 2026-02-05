package com.findly.application.service;

import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.response.CategoryResponse;
import com.findly.application.mapper.CategoryMapper;
import com.findly.domain.entity.Category;
import com.findly.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Get all parent categories with their children.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> parentCategories = categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc();

        return parentCategories.stream()
                .map(categoryMapper::toResponseWithChildren)
                .toList();
    }

    /**
     * Get category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        return categoryMapper.toResponseWithChildren(category);
    }

    /**
     * Get category by slug.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));

        return categoryMapper.toResponseWithChildren(category);
    }

    /**
     * Get subcategories for a parent category.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Category", "id", parentId);
        }

        List<Category> subcategories = categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(parentId);

        return categoryMapper.toResponseList(subcategories);
    }
}
