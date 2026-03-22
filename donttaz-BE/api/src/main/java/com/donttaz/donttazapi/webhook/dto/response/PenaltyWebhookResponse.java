package com.donttaz.donttazapi.webhook.dto.response;

import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.domain.PlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "수치심 메시지 설정 응답 DTO")
public record PenaltyWebhookResponse(
        @Schema(description = "웹훅 설정 ID", example = "1") Long webhookId,

        @Schema(description = "사용자 ID", example = "10") Long userId,

        @Schema(description = "연동된 위시리스트 금고 ID", example = "100") Long wishlistVaultId,

        @Schema(description = "플랫폼 타입", example = "KAKAOTALK") PlatformType platformType,

        @Schema(description = "수신할 웹훅 토큰 (Access Token)", example = "kakao_oauth_token_123") String webhookToken,

        @Schema(description = "리프레시 토큰", example = "kakao_refresh_token_123") String refreshToken,

        @Schema(description = "액세스 토큰 만료 시각", example = "2026-03-22T23:59:59") java.time.LocalDateTime accessTokenExpiresAt,

        @Schema(description = "메시지를 수신할 친구들의 UUID 목록", example = "[\"uuid1\", \"uuid2\"]") java.util.List<String> receiverUuuids,

        @Schema(description = "수치심 메시지 내용", example = "방금 야식 결제해 버렸습니다. 한마디씩 부탁드립니다.") String shameMessage) {
    public static PenaltyWebhookResponse from(PenaltyWebhook webhook) {
        java.util.List<String> uuids = null;
        if (webhook.getReceiverUuuids() != null && !webhook.getReceiverUuuids().isEmpty()) {
            // 간단하게 콤마로 분리하거나 JSON 파싱 (여기서는 콤마 기준으로 처리 예시)
            uuids = java.util.Arrays.asList(webhook.getReceiverUuuids().split(","));
        }

        return PenaltyWebhookResponse.builder()
                .webhookId(webhook.getId())
                .userId(webhook.getUserId())
                .wishlistVaultId(webhook.getWishlistVaultId())
                .platformType(webhook.getPlatformType())
                .webhookToken(webhook.getWebhookToken())
                .refreshToken(webhook.getRefreshToken())
                .accessTokenExpiresAt(webhook.getAccessTokenExpiresAt())
                .receiverUuuids(uuids)
                .shameMessage(webhook.getShameMessage())
                .build();
    }
}
