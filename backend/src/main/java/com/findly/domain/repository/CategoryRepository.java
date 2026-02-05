package com.findly.domain.repository;

import com.findly.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrderAsc();

    List<Category> findByParentIdAndActiveTrueOrderBySortOrderAsc(UUID parentId);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.sortOrder")
    List<Category> findAllActiveCategories();

    boolean existsBySlug(String slug);
}
