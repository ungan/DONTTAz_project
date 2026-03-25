package com.donttaz.donttazapi.webhook.event;

/**
 * 위시리스트 금고 규칙을 어겼을 때 발생하는 페널티 이벤트 (RabbitMQ 전송용)
 */
public record VaultPenaltyEvent(Long wishlistVaultId, Long withdrawAmount, boolean terminated, boolean success) {
}
