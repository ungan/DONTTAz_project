package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "수시입출금 상품 목록")
public record DemandDepositProductListResponse(
        @Schema(description = "조회된 상품 목록")
        List<DemandDepositProductResponse> products
) {
}
