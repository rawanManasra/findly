package com.findly.domain.repository;

import com.findly.domain.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findByBusinessIdAndActiveTrueOrderBySortOrderAsc(UUID businessId);

    List<Service> findByBusinessIdOrderBySortOrderAsc(UUID businessId);

    Optional<Service> findByIdAndBusinessId(UUID id, UUID businessId);

    long countByBusinessId(UUID businessId);

    boolean existsByBusinessIdAndName(UUID businessId, String name);
}
