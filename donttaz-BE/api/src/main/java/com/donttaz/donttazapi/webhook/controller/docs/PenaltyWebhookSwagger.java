package com.donttaz.donttazapi.webhook.controller.docs;

import com.donttaz.donttazapi.webhook.dto.request.PenaltyWebhookRequest;
import com.donttaz.donttazapi.webhook.dto.response.PenaltyWebhookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@Tag(name = "수치심 메시지 (웹훅)", description = "수치심 메시지 설정 관리 API")
@SecurityRequirement(name = "bearerAuth")
public interface PenaltyWebhookSwagger {

    @Operation(summary = "수치심 메시지 설정 저장 및 수정", description = "위시리스트 금고에 연결된 수치심 메시지와 카카오톡 웹훅 정보를 저장하거나 수정합니다.")
    PenaltyWebhookResponse saveOrUpdateWebhook(
            @Parameter(description = "위시리스트 금고 ID", example = "10") @PathVariable Long vaultId,
            @RequestBody @Valid PenaltyWebhookRequest request);

    @Operation(summary = "수치심 메시지 설정 조회", description = "위시리스트 금고에 연결된 수치심 메시지 설정 정보를 조회합니다.")
    PenaltyWebhookResponse getWebhook(
            @Parameter(description = "위시리스트 금고 ID", example = "10") @PathVariable Long vaultId);
}
