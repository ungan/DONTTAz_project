package com.donttaz.donttazapi.vault.domain;

public enum VaultStatus {
    LOCKED, // 금고 봉인 상태 (기본)
    UNLOCKED, // 금고 해제 상태 (접근 가능)
    CLOSED // 금고 종료 상태 (위시리스트 달성 완료 또는 금고 폐쇄)
}
