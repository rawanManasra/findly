package com.findly.domain.repository;

import com.findly.domain.entity.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, UUID> {

    List<WorkingHours> findByBusinessIdOrderByDayOfWeekAsc(UUID businessId);

    Optional<WorkingHours> findByBusinessIdAndDayOfWeek(UUID businessId, Integer dayOfWeek);

    void deleteByBusinessId(UUID businessId);
}
