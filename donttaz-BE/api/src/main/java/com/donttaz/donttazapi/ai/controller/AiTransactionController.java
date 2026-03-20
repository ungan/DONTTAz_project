package com.donttaz.donttazapi.ai.controller;

import com.donttaz.donttazapi.ai.dto.request.AiTransactionSyncRequest;
import com.donttaz.donttazapi.ai.dto.request.TransactionCategoryUpdateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiCategorizedListResponse;
import com.donttaz.donttazapi.ai.dto.response.AiTransactionSyncResponse;
import com.donttaz.donttazapi.ai.dto.response.TransactionCategoryUpdateResponse;
import com.donttaz.donttazapi.ai.service.AiTransactionService;
import com.donttaz.donttazapi.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.donttaz.donttazapi.ai.dto.request.SpendingPlanCreateRequest;
import com.donttaz.donttazapi.ai.dto.response.SpendingPlanResponse;

import com.donttaz.donttazapi.ai.dto.response.SpendingPlanResponse;

import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportResponse;
import com.donttaz.donttazapi.ai.dto.response.AiBurndownChartResponse;
import java.time.YearMonth;

@Tag(name = "AI Transaction API", description = "AI 기반 거래 내역 분석 및 동기화 API")

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AiTransactionController {

    private final AiTransactionService aiTransactionService;

    @Operation(summary = "소비 리포트 조회", description = "특정 월의 소비 플랜 대비 실제 지출 내역을 비교 분석한 리포트를 조회합니다.")
    @GetMapping("/ai/reports")
    public ApiResponse<AiSpendingReportResponse> getSpendingReport(
            @RequestParam(required = false) String yearMonth
    ) {
        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().minusMonths(1).toString(); // 기본값: 지난달
        }
        AiSpendingReportResponse response = aiTransactionService.getSpendingReport(yearMonth);
        return ApiResponse.success("소비 리포트 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "생존 번다운 차트 데이터 조회", description = "이번 달 총 예산 대비 일자별 이상적/실제 잔액 추이를 조회합니다.")
    @GetMapping("/control/burndown")
    public ApiResponse<AiBurndownChartResponse> getBurndownChart(
            @RequestParam(required = false) String yearMonth
    ) {
        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().toString(); // 기본값: 이번 달
        }
        AiBurndownChartResponse response = aiTransactionService.getBurndownChart(yearMonth);
        return ApiResponse.success("생존 번다운 차트 데이터 조회 성공", response);
    }

    @Operation(summary = "맞춤 소비 플랜 생성", description = "최근 소비 내역을 분석하여 이번 달 권장 예산과 잉여 자금을 산출합니다.")
    @PostMapping("/ai/plans")
    public ApiResponse<SpendingPlanResponse> createSpendingPlan(@RequestBody SpendingPlanCreateRequest request) {
        SpendingPlanResponse response = aiTransactionService.createSpendingPlan(request);
        return ApiResponse.success("맞춤 소비 플랜이 성공적으로 생성되었습니다.", response);
    }

    @Operation(summary = "카테고리 수정", description = "AI가 분류한 카테고리를 사용자가 직접 수정합니다.")
    @PatchMapping("/transactions/{transactionId}/category")
    public ApiResponse<TransactionCategoryUpdateResponse> updateTransactionCategory(
            @PathVariable Long transactionId,
            @RequestBody TransactionCategoryUpdateRequest request
    ) {
        TransactionCategoryUpdateResponse response = aiTransactionService.updateTransactionCategory(transactionId, request);
        return ApiResponse.success("카테고리가 성공적으로 수정되었습니다.", response);
    }

    @Operation(summary = "카테고리 결과 조회", description = "AI가 분류한 거래 내역 결과를 조회합니다.")
    @GetMapping("/ai/transactions/categories")
    public ApiResponse<AiCategorizedListResponse> getCategorizedTransactions(
            @RequestParam String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AiCategorizedListResponse response = aiTransactionService.getCategorizedTransactions(yearMonth, PageRequest.of(page, size));
        return ApiResponse.success("카테고리 분류 결과 조회 성공", response);
    }

    @Operation(summary = "통합 거래내역 수집 및 AI 분석", description = "타행 거래 내역을 수집하고 AI를 통해 카테고리를 분류합니다.")
    @PostMapping("/ai/transactions/sync")
    public ApiResponse<AiTransactionSyncResponse> syncTransactions(@RequestBody(required = false) AiTransactionSyncRequest request) {
        if (request == null) {
            request = new AiTransactionSyncRequest(null, null);
        }
        AiTransactionSyncResponse response = aiTransactionService.syncTransactions(request);
        return ApiResponse.success("최신 거래 내역 수집이 완료되었습니다.", response);
    }
}
