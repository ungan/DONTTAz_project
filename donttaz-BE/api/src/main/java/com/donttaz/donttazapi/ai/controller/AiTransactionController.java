package com.donttaz.donttazapi.ai.controller;

import com.donttaz.donttazapi.ai.dto.request.AiTransactionReanalyzeRequest;
import com.donttaz.donttazapi.ai.dto.request.AiTransactionSyncRequest;
import com.donttaz.donttazapi.ai.dto.request.SpendingPlanCreateRequest;
import com.donttaz.donttazapi.ai.dto.request.TransactionCategoryUpdateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiBurndownChartResponse;
import com.donttaz.donttazapi.ai.dto.response.AiCategorizedListResponse;
import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportResponse;
import com.donttaz.donttazapi.ai.dto.response.AiTransactionReanalyzeResponse;
import com.donttaz.donttazapi.ai.dto.response.AiTransactionSyncResponse;
import com.donttaz.donttazapi.ai.dto.response.SpendingPlanResponse;
import com.donttaz.donttazapi.ai.dto.response.TransactionCategoryUpdateResponse;
import com.donttaz.donttazapi.ai.service.AiTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
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

@Tag(name = "AI Transaction API", description = "AI-based transaction analysis APIs")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AiTransactionController {

    private final AiTransactionService aiTransactionService;

    @Operation(summary = "소비 리포트 조회", description = "특정 월의 소비 플랜 대비 실제 지출 내역을 비교 분석한 리포트를 조회합니다.")
    @GetMapping("/ai/reports")
    public AiSpendingReportResponse getSpendingReport(@RequestParam(required = false) String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().minusMonths(1).toString();
        }
        return aiTransactionService.getSpendingReport(yearMonth);
    }

    @Operation(summary = "생존 번다운 차트 데이터 조회", description = "이번 달 총 예산 대비 일자별 이상적/실제 잔액 추이를 조회합니다.")
    @GetMapping("/control/burndown")
    public AiBurndownChartResponse getBurndownChart(@RequestParam(required = false) String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().toString();
        }
        return aiTransactionService.getBurndownChart(yearMonth);
    }

    @Operation(summary = "맞춤 소비 플랜 생성", description = "최근 소비 내역을 분석하여 이번 달 권장 예산과 잉여 자금을 산출합니다.")
    @PostMapping("/ai/plans")
    public SpendingPlanResponse createSpendingPlan(@RequestBody SpendingPlanCreateRequest request) {
        return aiTransactionService.createSpendingPlan(request);
    }

    @Operation(summary = "카테고리 수정", description = "AI가 분류한 카테고리를 사용자가 직접 수정합니다.")
    @PatchMapping("/transactions/{transactionId}/category")
    public TransactionCategoryUpdateResponse updateTransactionCategory(
            @PathVariable Long transactionId,
            @RequestBody TransactionCategoryUpdateRequest request
    ) {
        return aiTransactionService.updateTransactionCategory(transactionId, request);
    }

    @Operation(summary = "카테고리 결과 조회", description = "AI가 분류한 거래 내역 결과를 조회합니다.")
    @GetMapping("/ai/transactions/categories")
    public AiCategorizedListResponse getCategorizedTransactions(
            @RequestParam String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return aiTransactionService.getCategorizedTransactions(yearMonth, PageRequest.of(page, size));
    }

    @Operation(summary = "통합 거래내역 수집 및 AI 분석", description = "타행 거래 내역을 수집하고 AI를 통해 카테고리를 분류합니다.")
    @PostMapping("/ai/transactions/sync")
    public AiTransactionSyncResponse syncTransactions(@RequestBody(required = false) AiTransactionSyncRequest request) {
        if (request == null) {
            request = new AiTransactionSyncRequest(null, null);
        }
        return aiTransactionService.syncTransactions(request);
    }

    @Operation(summary = "저장된 거래 재분석", description = "DB에 저장된 거래 내역을 기간 기준으로 다시 AI 분류합니다.")
    @PostMapping("/ai/transactions/reanalyze")
    public AiTransactionReanalyzeResponse reanalyzeTransactions(
            @RequestBody(required = false) AiTransactionReanalyzeRequest request
    ) {
        if (request == null) {
            request = new AiTransactionReanalyzeRequest(null, null, false);
        }
        return aiTransactionService.reanalyzeTransactions(request);
    }
}
