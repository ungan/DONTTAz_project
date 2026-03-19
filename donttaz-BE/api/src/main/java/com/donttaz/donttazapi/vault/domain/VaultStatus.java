package com.donttaz.donttazapi.vault.domain;

public enum VaultStatus {
    LOCKED, // 금고 봉인 상태 (입금 가능, 출금 시 페널티)
    BROKEN, // 중도 해지 상태 (페널티 이자 지급 완료)
    MATURED // 만기 해지 상태 (정상 이자 지급 완료)
}
