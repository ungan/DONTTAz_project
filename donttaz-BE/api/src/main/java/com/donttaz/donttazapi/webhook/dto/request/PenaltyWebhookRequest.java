package com.donttaz.donttazapi.webhook.dto.request;

import com.donttaz.donttazapi.webhook.domain.PlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "수치심 메시지 설정 요청 DTO")
public record PenaltyWebhookRequest(
        @Schema(description = "플랫폼 타입 (기본값: KAKAOTALK)", example = "KAKAOTALK") @NotNull(message = "플랫폼 타입은 필수입니다.") PlatformType platformType,

        @Schema(description = "웹훅 발송을 위한 식별 토큰", example = "kakao_oauth_token_123") @NotBlank(message = "웹훅 토큰은 필수입니다.") String webhookToken,

        @Schema(description = "수치심 메시지 내용 (최대 200자)", example = "방금 야식 결제해 버렸습니다. 한마디씩 부탁드립니다.") @NotBlank(message = "수치심 메시지는 필수입니다.") @Size(max = 200, message = "수치심 메시지는 200자를 초과할 수 없습니다.") String shameMessage) {
}
