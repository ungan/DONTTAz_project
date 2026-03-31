package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "계좌 이체 요청")
public record AccountTransferRequest(
        @Schema(description = "입금 계좌번호", example = "0204667768182760")
        @NotBlank(message = "입금 계좌번호는 필수입니다.")
        String depositAccountNo,

        @Schema(description = "입금 거래 요약", example = "(수시입출금) : 입금(이체)")
        @NotBlank(message = "입금 거래 요약은 필수입니다.")
        String depositTransactionSummary,

        @Schema(description = "이체 금액", example = "10000000")
        @NotBlank(message = "이체 금액은 필수입니다.")
        @Pattern(regexp = "^[1-9]\\d*$", message = "이체 금액은 0보다 큰 숫자여야 합니다.")
        String transactionBalance,

        @Schema(description = "출금 계좌번호", example = "0016174648358792")
        @NotBlank(message = "출금 계좌번호는 필수입니다.")
        String withdrawalAccountNo,

        @Schema(description = "출금 거래 요약", example = "(수시입출금) : 출금(이체)")
        @NotBlank(message = "출금 거래 요약은 필수입니다.")
        String withdrawalTransactionSummary
) {
}
