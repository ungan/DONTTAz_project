package com.donttaz.donttazapi.memorandum.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "각서 생성 요청")
public record MemorandumRequest(
        @Schema(description = "각서 제1조: 사인 규명", example = "야식 배달의 유혹") @NotBlank(message = "제1조 내용은 필수입니다.") String content1,

        @Schema(description = "각서 제2조: 주변 사람 공표 사항", example = "주변 사람들에게 망신 주기 요청") @NotBlank(message = "제2조 내용은 필수입니다.") String content2,

        @Schema(description = "각서 제3조: 향후 다짐", example = "물과 공기로 연명하겠다") @NotBlank(message = "제3조 내용은 필수입니다.") String content3) {
}
