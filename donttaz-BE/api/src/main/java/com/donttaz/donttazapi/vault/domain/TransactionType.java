package com.donttaz.donttazapi.vault.domain;

public enum TransactionType {
    DEPOSIT, // 입금
    WITHDRAW_BROKEN, // 중도 해지 (유혹에 지기)
    WITHDRAW_MATURED // 만기 해지 (목표 달성)
}
