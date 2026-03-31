package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "수시입출금 상품 등록 요청")
public record CreateDemandDepositProductRequest(
        @Schema(description = "은행 코드", example = "001")
        @NotBlank(message = "은행 코드는 필수입니다.")
        @Pattern(regexp = "\\d{3}", message = "은행 코드는 3자리 숫자여야 합니다.")
        String bankCode,

        @Schema(description = "상품명", example = "한국은행 수시입출금 상품명")
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String accountName,

        @Schema(description = "상품 설명", example = "한국은행 수시입출금 상품설명")
        @NotBlank(message = "상품 설명은 필수입니다.")
        @Size(max = 255, message = "상품 설명은 255자 이하여야 합니다.")
        String accountDescription
) {
}
