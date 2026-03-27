package com.donttaz.donttazapi.webhook.dto.response;

import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.domain.PlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "수치심 메시지 설정 응답 DTO")
public record PenaltyWebhookResponse(
        @Schema(description = "웹훅 설정 ID", example = "1") Long webhookId,

        @Schema(description = "플랫폼 타입", example = "MATTERMOST") PlatformType platformType,

        @Schema(description = "수치심 메시지 내용", example = "방금 야식 결제해 버렸습니다. 한마디씩 부탁드립니다.") String shameMessage,
        @Schema(description = "웹훅 URL", example = "https://discord.com/api/webhooks/...") String webhookUrl) {
    public static PenaltyWebhookResponse from(PenaltyWebhook webhook) {
        return new PenaltyWebhookResponse(
                webhook.getId(),
                webhook.getPlatformType(),
                webhook.getShameMessage(),
                webhook.getWebhookUrl());
    }
}
