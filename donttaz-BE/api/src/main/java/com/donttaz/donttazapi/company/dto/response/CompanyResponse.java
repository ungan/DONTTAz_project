package com.donttaz.donttazapi.company.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기업 정보 응답")
public record CompanyResponse(
        @Schema(description = "기업 ID", example = "1") Long companyId,

        @Schema(description = "기업 명칭", example = "테스트기업") String name) {
}
