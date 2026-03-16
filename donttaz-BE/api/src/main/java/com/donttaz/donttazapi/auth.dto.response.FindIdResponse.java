package com.donttaz.donttazapi.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindIdResponse {
    private String maskedLoginId;
    private String createdAt; // 가입일 (yyyy-MM-dd 포맷)
}
