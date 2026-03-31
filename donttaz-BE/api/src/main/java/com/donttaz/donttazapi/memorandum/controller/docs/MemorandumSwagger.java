package com.donttaz.donttazapi.memorandum.controller.docs;

import com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "각서", description = "각서 관리 API")
@SecurityRequirement(name = "bearerAuth")
public interface MemorandumSwagger {

        @Operation(summary = "금고 연동 각서 조회", description = "특정 금고에 연결된 각서 정보를 조회합니다.")
        MemorandumResponse getMemorandum(
                        @Parameter(description = "위시리스트 금고 ID", example = "10") @PathVariable Long vaultId);

        @Operation(summary = "각서 최종 서명", description = "작성 중인 각서를 최종 서명 처리합니다. 서명 후에는 각서의 상태가 SIGNED로 변경됩니다.")
        void signMemorandum(
                        @Parameter(description = "위시리스트 금고 ID", example = "10") @PathVariable Long vaultId);
}
