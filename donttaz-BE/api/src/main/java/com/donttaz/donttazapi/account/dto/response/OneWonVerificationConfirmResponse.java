package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "1원 송금 인증코드 검증 결과")
public record OneWonVerificationConfirmResponse(
        @Schema(description = "검증 요청 ID", example = "7")
        String verificationId,

        @Schema(description = "인증 계좌번호", example = "0011214764051239")
        String accountNo,

        @Schema(description = "검증 상태", example = "SUCCESS")
        String status,

        @Schema(description = "인증 성공 여부", example = "true")
        boolean verified
) {
}
