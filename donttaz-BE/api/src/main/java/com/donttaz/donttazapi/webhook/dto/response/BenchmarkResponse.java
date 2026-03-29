package com.donttaz.donttazapi.webhook.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "웹훅 성능 벤치마크 응답 DTO")
public record BenchmarkResponse(
        @Schema(description = "처리 유형 (Sync / Async)", example = "Async")
        String mode,

        @Schema(description = "소요 시간 (milliseconds)", example = "15")
        long elapsedTimeMs,

        @Schema(description = "상태 메시지", example = "RabbitMQ 메시지 발행 완료")
        String message
) {
}
