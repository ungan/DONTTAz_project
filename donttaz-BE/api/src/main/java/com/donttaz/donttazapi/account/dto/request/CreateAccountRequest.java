package com.donttaz.donttazapi.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank(message = "은행 코드는 필수입니다.")
        @Pattern(regexp = "\\d{3}", message = "은행 코드는 3자리 숫자여야 합니다.")
        String bankCode,

        @NotBlank(message = "계좌 번호는 필수입니다.")
        @Pattern(regexp = "\\d{6,20}", message = "계좌 번호는 하이픈 없는 숫자여야 합니다.")
        String accountNumber,

        @Size(max = 100, message = "통장 별칭은 100자 이하로 입력해 주세요.")
        String accountAlias,

        @NotBlank(message = "동의 토큰은 필수입니다.")
        String consentToken
) {
}
