package com.donttaz.donttazapi.webhook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수치심 메시지 설정 요청 DTO")
public record PenaltyWebhookRequest(
        @Schema(description = "알림 플랫폼 타입 (MATTERMOST, DISCORD)", example = "MATTERMOST") com.donttaz.donttazapi.webhook.domain.PlatformType platformType,
        @Schema(description = "수치심 메시지 내용 (최대 200자)", example = "방금 야식 결제해 버렸습니다. 한마디씩 부탁드립니다.") @NotBlank(message = "수치심 메시지는 필수입니다.") @Size(max = 200, message = "수치심 메시지는 200자를 초과할 수 없습니다.") String shameMessage) {
}
