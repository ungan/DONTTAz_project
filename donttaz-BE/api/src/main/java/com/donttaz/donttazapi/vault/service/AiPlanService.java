package com.donttaz.donttazapi.vault.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 서비스로부터 사용자의 소비 플랜(금고 입금 예정 금액)을 조회하는 서비스.
 * 현재는 'Mock' 형식으로 구현되어 있으며,
 * 향후 AI의 PostgreSQL DB 직접 조회 또는 REST API 호출로 대체될 예정입니다.
 */
@Slf4j
@Service
public class AiPlanService {

    /**
     * 특정 사용자의 해당 월 금고 입금 예정 금액을 조회합니다.
     * 
     * @param userId    사용자 ID
     * @param yearMonth 대상 년월 (예: "2026-03")
     * @return 금고 입금 예정 금액 (Long)
     */
    public Long getPlannedDepositAmount(Long userId, String yearMonth) {
        log.info("AI 소비 플랜 조회 (예시): userId={}, yearMonth={}", userId, yearMonth);

        // TODO: 실제 AI PostgreSQL DB 조회 또는 API 호출 로직 구현 필요
        // 현재는 테스트를 위해 사용자별로 고정된 더미 값을 반환하거나, 특정 로직에 따른 값을 반환합니다.

        // 예시: 기본 500,000원 반환
        return 500000L;
    }
}
