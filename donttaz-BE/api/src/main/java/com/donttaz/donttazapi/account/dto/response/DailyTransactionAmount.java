package com.donttaz.donttazapi.account.dto.response;

import java.time.LocalDate;

/**
 * 기간 내 거래 금액을 날짜별로 합산한 한 행. 번다운 차트처럼 일별 합계만 필요한 곳에서
 * 거래 엔티티를 전부 불러오지 않고 DB 집계 결과만 받기 위해 쓴다.
 */
public record DailyTransactionAmount(LocalDate day, Long totalAmount) {
}
