package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.ai.repository.SpendingPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 서비스로부터 사용자의 소비 플랜(금고 입금 예정 금액)을 조회하는 서비스.
 * 기존 Mock 형식을 제거하고 실제 MySQL의 SpendingPlan 데이터를 조회하도록 변경되었습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiPlanService {

    private final SpendingPlanRepository spendingPlanRepository;

    /**
     * 특정 사용자의 해당 월 금고 입금 예정 금액을 조회합니다.
     * 
     * @param userId    사용자 ID
     * @param yearMonth 대상 년월 (예: "2026-03")
     * @return 금고 입금 예정 금액 (Long)
     */
    public Long getPlannedDepositAmount(Long userId, String yearMonth) {
        log.info("AI 소비 플랜 조회: userId={}, yearMonth={}", userId, yearMonth);

        return spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)
                .map(plan -> {
                    // 팀원분이 sealedAmount 파싱 로직을 푸시하기 전까지는 NPE 방지를 위해 surplusAmount로 폴백(Fallback)하도록 안전 파이프라인 구성
                    if (plan.getSealedAmount() != null && plan.getSealedAmount() > 0) {
                        return plan.getSealedAmount().longValue();
                    }
                    return plan.getSurplusAmount() != null ? plan.getSurplusAmount().longValue() : 0L;
                })
                .orElseGet(() -> {
                    log.warn("해당 월의 AI 소비 플랜이 존재하지 않습니다. userId={}, yearMonth={}", userId, yearMonth);
                    return 0L;
                });
    }
}
