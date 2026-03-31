"""
리포트 생성 유스케이스 — app/api/report.py의 진입점에서 호출됨.

거래 정규화 → 소비 분석 → 페르소나 추출 → 피드백 생성 → 카테고리 리포트 조립 순서로 오케스트레이션.
"""
from __future__ import annotations

from app.domains.transaction.normalizer import NormalizationService
from app.domains.budget.estimator import BudgetEstimationService
from app.domains.report.analyzer import SpendingAnalysisService
from app.domains.report.feedback import FeedbackService
from app.domains.report.personalized_feedback import PersonalizedFeedbackService


def build_category_reports(report: dict, normalization_service: NormalizationService) -> list:
    category_budget_status = report.get("categories", {}).get("categoryBudgetStatus", {})
    category_spending = report.get("categories", {}).get("categorySpending", {})
    total_spent = int(report.get("summary", {}).get("totalSpending", 0))

    all_categories = set(category_budget_status.keys()) | set(category_spending.keys())
    results = []

    for category in all_categories:
        budget_data = category_budget_status.get(category, {})
        allocated_amount = int(budget_data.get("budget", 0))
        spent_amount = int(budget_data.get("used", category_spending.get(category, 0)))

        if allocated_amount == 0 and spent_amount == 0:
            continue

        category_rate = round((spent_amount / total_spent) * 100, 1) if total_spent > 0 else 0.0

        feedback = (
            "계획 대비 초과 소비입니다."
            if allocated_amount > 0 and spent_amount > allocated_amount
            else "계획 범위 내 소비입니다."
        )
        if allocated_amount == 0:
            feedback = "계획이 없던 지출 항목입니다."

        results.append({
            "categoryName": normalization_service.to_service_category(category),
            "allocatedAmount": allocated_amount,
            "spentAmount": spent_amount,
            "categoryRate": category_rate,
            "feedback": feedback,
        })

    results.sort(key=lambda x: x["spentAmount"], reverse=True)
    return results


def execute(request) -> dict:
    normalization_service = NormalizationService()
    card_transactions = normalization_service.normalize_card_transactions(request.cardTransactions)
    account_transactions = normalization_service.normalize_account_transactions(request.accountTransactions)

    if request.plan:
        total_budget = int(request.plan.totalBudget)
        sealed_amount = int(request.plan.sealedAmount)
        category_budgets = {
            normalization_service.to_service_category(item.categoryName): int(item.allocatedAmount)
            for item in request.plan.categoryPlans
        }
    else:
        budget_service = BudgetEstimationService()
        budget_info = budget_service.estimate(
            current_account_transactions=account_transactions,
            current_card_transactions=card_transactions,
            payday_day=request.paydayDay,
        )
        total_budget = int(budget_info.get("totalBudget", 0))
        sealed_amount = 0
        category_budgets = {
            normalization_service.to_service_category(category): value
            for category, value in budget_info.get("categoryBudgets", {}).items()
        }

    all_transactions = card_transactions + account_transactions

    analysis_service = SpendingAnalysisService()
    report = analysis_service.analyze(
        current_transactions=all_transactions,
        previous_transactions=[],
        total_budget=total_budget,
        category_budgets=category_budgets,
    )
    spending_persona = analysis_service.extract_spending_persona(all_transactions)

    feedback_service = FeedbackService()
    feedback_result = feedback_service.generate_feedback(report, spending_persona=spending_persona)
    category_reports = build_category_reports(report, normalization_service)

    personalized_feedback_service = PersonalizedFeedbackService()
    personalized_feedback = personalized_feedback_service.build(
        report=report,
        category_reports=category_reports,
        spending_persona=spending_persona,
    )

    summary = report.get("summary", {})

    return {
        "status": "SUCCESS",
        "message": "분석 리포트 생성 완료",
        "data": {
            "yearMonth": request.yearMonth,
            "sealedAmount": sealed_amount,
            "totalBudget": total_budget,
            "totalSpent": int(summary.get("totalSpending", 0)),
            "achievementRate": float(summary.get("achievementRate", 0.0)),
            "spendingPersona": spending_persona.get("persona") if spending_persona else None,
            "aiOverallFeedback": feedback_result["overallFeedback"],
            "categoryReports": category_reports,
            "personalizedFeedback": personalized_feedback,
        },
    }
