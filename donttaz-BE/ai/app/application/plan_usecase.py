"""
플랜 생성 유스케이스 — app/api/plan.py의 진입점에서 호출됨.

거래 정규화 → 카테고리 분류 → 예산 추정 → PatchTST 예측
→ 봉인금액 계산 → risk_score 계산 → LLM 추천 생성 순서로 오케스트레이션.
"""
from __future__ import annotations

from app.domains.transaction.normalizer import NormalizationService
from app.domains.transaction.classifier import get_nlp_service
from app.domains.budget.estimator import BudgetEstimationService
from app.domains.vault.sealing import SealingService
from app.domains.risk.scorer import calculate_risk_score, get_budget_strategy
from app.domains.report.analyzer import SpendingAnalysisService
from app.infrastructure.ml.forecaster import ForecastService
from app.infrastructure.llm.recommendation import LLMRecommendationService, RecommendationService


def _apply_ai_comments(category_plans: list, category_comments: dict) -> None:
    if category_comments:
        for plan in category_plans:
            plan["aiComment"] = category_comments.get(
                plan["categoryName"],
                f"{plan['categoryName']} 예산을 배정했습니다.",
            )
    else:
        for plan in category_plans:
            plan["aiComment"] = "최근 소비 패턴을 기반으로 예산을 배정했습니다."


def _resolve_recommendations(personalized_recommendations: list, category_plans: list, risk_score: float, sealed_amount: int, payday: int) -> list:
    if personalized_recommendations:
        return personalized_recommendations
    return RecommendationService().generate_plan_recommendations(
        category_plans=category_plans,
        risk_score=risk_score,
        sealed_amount=sealed_amount,
        payday_day=payday,
    )


def execute(request) -> dict:
    nlp_service = get_nlp_service()
    normalization_service = NormalizationService()
    card_transactions = normalization_service.normalize_card_transactions(request.cardTransactions)
    card_transactions = nlp_service.categorize_transactions(card_transactions)
    account_transactions = normalization_service.normalize_account_transactions(request.accountTransactions)

    budget_service = BudgetEstimationService()
    forecast_service = ForecastService()
    sealing_service = SealingService()

    budget_info = budget_service.estimate(
        current_account_transactions=account_transactions,
        current_card_transactions=card_transactions,
        payday_day=request.paydayDay,
    )

    estimated_monthly_income = int(budget_info.get("monthlyIncome", 0))
    estimated_fixed_expense = int(budget_info.get("fixedExpense", 0))
    category_budgets = budget_info.get("categoryBudgets", {})

    payday = request.paydayDay
    predictions = forecast_service.predict_next_7_days(card_transactions, payday=payday)
    recent_daily_amounts = forecast_service.get_recent_daily_amounts(card_transactions, payday=payday)
    weekday_avg = forecast_service.get_weekday_avg(card_transactions)
    alpha = max(0.10, 1.5 * forecast_service.calculate_historical_mae_ratio(card_transactions))

    sealing_expand = sealing_service.expand_predictions_until_payday(
        predictions=predictions,
        payday_day=payday,
        weekday_avg=weekday_avg,
    )

    _predicted = sealing_expand["predictedSpendUntilPayday"]
    _balance = request.currentBalance
    preliminary_risk = min(_predicted / (_balance + 1), 1.0)

    sealing_result = sealing_service.calculate_sealed_amount(
        available_money=request.currentBalance,
        predicted_spend_until_payday=sealing_expand["predictedSpendUntilPayday"],
        recent_daily_amounts=recent_daily_amounts,
        risk_score=preliminary_risk,
        alpha=alpha,
        beta=1.2,
    )

    sealed_amount = int(sealing_result["sealedAmount"])
    usable_budget = int(sealing_result["usableBudget"])

    if estimated_monthly_income > 0:
        disposable = max(estimated_monthly_income - estimated_fixed_expense, 0)
        sealed_amount = min(sealed_amount, int(disposable * 0.3))
        sealed_amount = min(sealed_amount, int(request.currentBalance * 0.8))
        usable_budget = max(request.currentBalance - sealed_amount, 0)

    category_plans = budget_service.allocate_to_category_plans(category_budgets, usable_budget, normalization_service)

    predicted_until_payday = int(sealing_expand["predictedSpendUntilPayday"])
    risk_score = calculate_risk_score(
        predicted_until_payday=predicted_until_payday,
        usable_budget=usable_budget,
        card_transactions=card_transactions,
        category_plans=category_plans,
        payday_day=payday,
    )

    strategy = get_budget_strategy(risk_score)

    svc = SpendingAnalysisService()
    spending_analysis = svc.analyze_spending_trends(card_transactions)
    spending_persona = svc.extract_spending_persona(card_transactions)
    weekday_pattern = svc.analyze_weekday_pattern(card_transactions)
    persona_shift = svc.analyze_persona_shift(card_transactions)
    llm_result = LLMRecommendationService().generate_recommendations(
        spending_analysis=spending_analysis,
        risk_score=risk_score,
        sealed_amount=sealed_amount,
        payday_day=payday,
        monthly_income=estimated_monthly_income,
        spending_persona=spending_persona,
        weekday_pattern=weekday_pattern,
        persona_shift=persona_shift,
    )

    category_comments = llm_result.get("categoryComments", {})
    personalized_recommendations = llm_result.get("recommendations", [])

    _apply_ai_comments(category_plans, category_comments)
    personalized_recommendations = _resolve_recommendations(
        personalized_recommendations, category_plans, risk_score, sealed_amount, payday
    )

    return {
        "status": "SUCCESS",
        "message": "플랜 생성 완료",
        "data": {
            "targetYearMonth": request.targetYearMonth,
            "referenceStartMonth": None,
            "referenceEndMonth": None,
            "totalBudget": usable_budget,
            "sealedAmount": sealed_amount,
            "surplusAmount": max(estimated_monthly_income - estimated_fixed_expense - usable_budget - sealed_amount, 0),
            "paydayDay": request.paydayDay,
            "riskScore": risk_score,
            "budgetStrategy": strategy,
            "categoryPlans": category_plans,
            "personalizedRecommendations": personalized_recommendations,
            "spendingPersona": spending_persona.get("persona") if spending_persona else None,
        },
    }
