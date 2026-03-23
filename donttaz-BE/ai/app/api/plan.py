from fastapi import APIRouter
from app.schemas.plan import PlanGenerateRequest, PlanGenerateResponse
from app.services.budget_estimation_service import BudgetEstimationService
from app.services.forecast_service import ForecastService
from app.services.sealing_service import SealingService
from app.services.normalization_service import NormalizationService
from app.services.recommendation_service import RecommendationService
from app.services.llm_recommendation_service import LLMRecommendationService
from app.services.spending_analysis_service import SpendingAnalysisService
from app.services.nlp_service import _nlp_instance as nlp_service

router = APIRouter()


@router.post("/plans/generate", response_model=PlanGenerateResponse)
def create_plan(request: PlanGenerateRequest):
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

    sealing_result = sealing_service.calculate_sealed_amount(
        available_money=request.currentBalance,
        predicted_spend_until_payday=sealing_expand["predictedSpendUntilPayday"],
        recent_daily_amounts=recent_daily_amounts,
        alpha=alpha,
        beta=1.2,
    )

    sealed_amount = int(sealing_result["sealedAmount"])
    usable_budget = int(sealing_result["usableBudget"])

    # sealed_amount에 상한선 적용 후 usable_budget 재조정
    if estimated_monthly_income > 0:
        disposable = max(estimated_monthly_income - estimated_fixed_expense, 0)
        sealed_amount = min(sealed_amount, int(disposable * 0.3))
        sealed_amount = min(sealed_amount, int(request.currentBalance * 0.8))
        usable_budget = max(request.currentBalance - sealed_amount, 0)

    # 카테고리별 예산 배분
    raw_sum = sum(category_budgets.values())
    category_plans = []

    for category, amount in category_budgets.items():
        allocated = round((amount / raw_sum) * usable_budget) if raw_sum > 0 else 0
        if allocated <= 0:
            continue
        category_name = normalization_service.to_service_category(category)
        category_plans.append({
            "categoryName": category_name,
            "allocatedAmount": allocated,
            "aiComment": "",
        })

    category_plans.sort(key=lambda x: x["allocatedAmount"], reverse=True)

    predicted_until_payday = int(sealing_expand["predictedSpendUntilPayday"])
    risk_score = 0.0
    if request.currentBalance > 0:
        risk_score = min(round(predicted_until_payday / request.currentBalance, 2), 1.0)

    if risk_score >= 0.8:
        strategy = "tight"
    elif risk_score >= 0.5:
        strategy = "balanced"
    else:
        strategy = "safe"

    # 소비 추세 분석 + LDA 페르소나 도출 후 LLM 추천 생성, 실패 시 룰 기반 fallback
    svc = SpendingAnalysisService()
    spending_analysis = svc.analyze_spending_trends(card_transactions)
    spending_persona = svc.extract_spending_persona(card_transactions)
    weekday_pattern = svc.analyze_weekday_pattern(card_transactions)
    persona_shift = svc.analyze_persona_shift(card_transactions)
    llm_result = LLMRecommendationService().generate_recommendations(
        spending_analysis=spending_analysis,
        spending_persona=spending_persona,
        weekday_pattern=weekday_pattern,
        persona_shift=persona_shift,
        risk_score=risk_score,
        sealed_amount=sealed_amount,
        payday_day=payday,
        monthly_income=estimated_monthly_income,
    )

    category_comments = llm_result.get("categoryComments", {})
    personalized_recommendations = llm_result.get("recommendations", [])

    if category_comments:
        for plan in category_plans:
            plan["aiComment"] = category_comments.get(
                plan["categoryName"],
                f"{plan['categoryName']} 예산을 배정했습니다.",
            )
    else:
        # LLM 실패 시 룰 기반 fallback
        for plan in category_plans:
            plan["aiComment"] = "최근 소비 패턴을 기반으로 예산을 배정했습니다."

    if not personalized_recommendations:
        personalized_recommendations = RecommendationService().generate_plan_recommendations(
            category_plans=category_plans,
            risk_score=risk_score,
            sealed_amount=sealed_amount,
            payday_day=payday,
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
