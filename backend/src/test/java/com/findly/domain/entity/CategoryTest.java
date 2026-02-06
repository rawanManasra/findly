package com.findly.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .build();
    }

    @Nested
    @DisplayName("isParent Tests")
    class IsParentTests {

        @Test
        @DisplayName("Should return true when parent is null")
        void shouldReturnTrueWhenParentNull() {
            category.setParent(null);
            assertThat(category.isParent()).isTrue();
        }

        @Test
        @DisplayName("Should return false when parent is set")
        void shouldReturnFalseWhenParentSet() {
            Category parentCategory = Category.builder()
                    .name("Parent Category")
                    .slug("parent-category")
                    .build();
            category.setParent(parentCategory);
            assertThat(category.isParent()).isFalse();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have sort order 0 by default")
        void shouldHaveSortOrderZeroByDefault() {
            Category newCategory = Category.builder()
                    .name("New Category")
                    .slug("new-category")
                    .build();

            assertThat(newCategory.getSortOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should be active by default")
        void shouldBeActiveByDefault() {
            Category newCategory = Category.builder()
                    .name("New Category")
                    .slug("new-category")
                    .build();

            assertThat(newCategory.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should have empty children list by default")
        void shouldHaveEmptyChildrenByDefault() {
            Category newCategory = Category.builder()
                    .name("New Category")
                    .slug("new-category")
                    .build();

            assertThat(newCategory.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("Should have empty businesses list by default")
        void shouldHaveEmptyBusinessesByDefault() {
            Category newCategory = Category.builder()
                    .name("New Category")
                    .slug("new-category")
                    .build();

            assertThat(newCategory.getBusinesses()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get name correctly")
        void shouldSetAndGetName() {
            category.setName("Updated Name");
            assertThat(category.getName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should set and get slug correctly")
        void shouldSetAndGetSlug() {
            category.setSlug("updated-slug");
            assertThat(category.getSlug()).isEqualTo("updated-slug");
        }

        @Test
        @DisplayName("Should set and get icon correctly")
        void shouldSetAndGetIcon() {
            category.setIcon("icon-name");
            assertThat(category.getIcon()).isEqualTo("icon-name");
        }
    }
}
