package com.donttaz.donttazapi.ai.repository;

import com.donttaz.donttazapi.ai.domain.SpendingPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendingPlanRepository extends JpaRepository<SpendingPlan, Long> {

    Optional<SpendingPlan> findByUserIdAndTargetYearMonth(Long userId, String targetYearMonth);
    
    void deleteByUserIdAndTargetYearMonth(Long userId, String targetYearMonth);
}
