package com.donttaz.donttazapi.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindIdResponse {
    private String maskedLoginId;
    private String createdAt; // 가입일 (yyyy-MM-dd 포맷)
}
