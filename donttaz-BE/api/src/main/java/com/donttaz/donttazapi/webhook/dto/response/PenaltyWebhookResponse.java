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

        @Schema(description = "수신할 웹훅 토큰", example = "kakao_oauth_token_123") String webhookToken,

        @Schema(description = "수치심 메시지 내용", example = "방금 야식 결제해 버렸습니다. 한마디씩 부탁드립니다.") String shameMessage) {
    public static PenaltyWebhookResponse from(PenaltyWebhook webhook) {
        return PenaltyWebhookResponse.builder()
                .webhookId(webhook.getId())
                .userId(webhook.getUserId())
                .wishlistVaultId(webhook.getWishlistVaultId())
                .platformType(webhook.getPlatformType())
                .webhookToken(webhook.getWebhookToken())
                .shameMessage(webhook.getShameMessage())
                .build();
    }
}
