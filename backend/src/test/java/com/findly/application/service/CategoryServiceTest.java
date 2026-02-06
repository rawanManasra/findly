package com.findly.application.service;

import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.response.CategoryResponse;
import com.findly.application.mapper.CategoryMapper;
import com.findly.domain.entity.Category;
import com.findly.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private UUID categoryId;
    private Category category;
    private Category parentCategory;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        parentCategory = Category.builder()
                .name("Parent Category")
                .slug("parent-category")
                .active(true)
                .build();
        parentCategory.setId(UUID.randomUUID());

        category = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .active(true)
                .parent(parentCategory)
                .build();
        category.setId(categoryId);

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Test Category")
                .slug("test-category")
                .build();
    }

    @Nested
    @DisplayName("Get All Categories Tests")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return all parent categories with children")
        void shouldReturnAllParentCategories() {
            when(categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc())
                    .thenReturn(List.of(parentCategory));
            when(categoryMapper.toResponseWithChildren(any(Category.class)))
                    .thenReturn(categoryResponse);

            List<CategoryResponse> result = categoryService.getAllCategories();

            assertThat(result).hasSize(1);
            verify(categoryRepository).findByParentIsNullAndActiveTrueOrderBySortOrderAsc();
        }

        @Test
        @DisplayName("Should return empty list when no categories")
        void shouldReturnEmptyListWhenNoCategories() {
            when(categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc())
                    .thenReturn(List.of());

            List<CategoryResponse> result = categoryService.getAllCategories();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Category By ID Tests")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("Should return category by ID")
        void shouldReturnCategoryById() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponseWithChildren(category)).thenReturn(categoryResponse);

            CategoryResponse result = categoryService.getCategoryById(categoryId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(categoryId);
        }

        @Test
        @DisplayName("Should throw when category not found by ID")
        void shouldThrowWhenCategoryNotFoundById() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Category By Slug Tests")
    class GetCategoryBySlugTests {

        @Test
        @DisplayName("Should return category by slug")
        void shouldReturnCategoryBySlug() {
            String slug = "test-category";
            when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponseWithChildren(category)).thenReturn(categoryResponse);

            CategoryResponse result = categoryService.getCategoryBySlug(slug);

            assertThat(result).isNotNull();
            assertThat(result.getSlug()).isEqualTo(slug);
        }

        @Test
        @DisplayName("Should throw when category not found by slug")
        void shouldThrowWhenCategoryNotFoundBySlug() {
            String slug = "non-existent";
            when(categoryRepository.findBySlug(slug)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getCategoryBySlug(slug))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Subcategories Tests")
    class GetSubcategoriesTests {

        @Test
        @DisplayName("Should return subcategories for parent")
        void shouldReturnSubcategories() {
            UUID parentId = parentCategory.getId();
            when(categoryRepository.existsById(parentId)).thenReturn(true);
            when(categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(parentId))
                    .thenReturn(List.of(category));
            when(categoryMapper.toResponseList(anyList())).thenReturn(List.of(categoryResponse));

            List<CategoryResponse> result = categoryService.getSubcategories(parentId);

            assertThat(result).hasSize(1);
            verify(categoryRepository).findByParentIdAndActiveTrueOrderBySortOrderAsc(parentId);
        }

        @Test
        @DisplayName("Should throw when parent category not found")
        void shouldThrowWhenParentNotFound() {
            UUID parentId = UUID.randomUUID();
            when(categoryRepository.existsById(parentId)).thenReturn(false);

            assertThatThrownBy(() -> categoryService.getSubcategories(parentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return empty list when no subcategories")
        void shouldReturnEmptyListWhenNoSubcategories() {
            UUID parentId = parentCategory.getId();
            when(categoryRepository.existsById(parentId)).thenReturn(true);
            when(categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(parentId))
                    .thenReturn(List.of());
            when(categoryMapper.toResponseList(anyList())).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.getSubcategories(parentId);

            assertThat(result).isEmpty();
        }
    }
}
