package com.donttaz.donttazapi.memorandum.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "각서 생성 요청 (현재는 별도 입력값 없음)")
public record MemorandumRequest() {
}
