from fastapi import APIRouter
from calendar import monthrange
from datetime import datetime, timedelta
from collections import defaultdict

from app.schemas.burndown import BurndownRequest, BurndownResponse
from app.services.forecast_service import ForecastService
from app.services.dday_service import DDayService
from app.services.sealing_service import SealingService
from app.services.normalization_service import NormalizationService

router = APIRouter()


@router.post("/burndown/calculate", response_model=BurndownResponse)
def get_burndown(request: BurndownRequest):
    normalization_service = NormalizationService()
    card_transactions = normalization_service.normalize_card_transactions(request.cardTransactions)

    forecast_service = ForecastService()
    dday_service = DDayService()
    sealing_service = SealingService()

    today = datetime.now()

    if request.plan:
        payday_day = int(request.plan.paydayDay)
    else:
        payday_day = 25

    predictions = forecast_service.predict_next_7_days(card_transactions, payday=payday_day)
    recent_daily_amounts = forecast_service.get_recent_daily_amounts(card_transactions, payday=payday_day)
    weekday_avg = forecast_service.get_weekday_avg(card_transactions)

    if request.plan:
        base_budget = int(request.plan.totalBudget)
        sealed_amount = int(request.plan.sealedAmount)
        usable_balance = base_budget
        sealing_expand = sealing_service.expand_predictions_until_payday(
            predictions=predictions,
            today=today,
            payday_day=payday_day,
            weekday_avg=weekday_avg,
        )
    else:
        base_budget = int(request.currentBalance)
        alpha = max(0.10, 1.5 * forecast_service.calculate_historical_mae_ratio(card_transactions))
        sealing_expand = sealing_service.expand_predictions_until_payday(
            predictions=predictions,
            today=today,
            payday_day=payday_day,
            weekday_avg=weekday_avg,
        )
        sealing_result = sealing_service.calculate_sealed_amount(
            available_money=base_budget,
            predicted_spend_until_payday=sealing_expand["predictedSpendUntilPayday"],
            recent_daily_amounts=recent_daily_amounts,
            alpha=alpha,
            beta=1.2,
        )
        sealed_amount = int(sealing_result["sealedAmount"])
        usable_balance = int(sealing_result["usableBudget"])

    # 실제 지출: 날짜 문자열 → 금액
    spending_by_date = defaultdict(int)
    for tx in card_transactions:
        amount = int(tx.get("amount", 0))
        date_str = str(tx.get("date", ""))[:10]
        if amount > 0:
            spending_by_date[date_str] += amount

    # 예측: 내일부터 다음 월급일까지
    prediction_map = {}
    for i, amount in enumerate(sealing_expand["expandedPredictions"]):
        date_str = (today + timedelta(days=i + 1)).strftime("%Y-%m-%d")
        prediction_map[date_str] = amount

    # 차트 범위: 직전 월급일 ~ 다음 월급일
    def safe_payday(year, month):
        last_day = monthrange(year, month)[1]
        return datetime(year, month, min(payday_day, last_day))

    if today.day >= payday_day:
        last_payday = safe_payday(today.year, today.month)
    elif today.month == 1:
        last_payday = safe_payday(today.year - 1, 12)
    else:
        last_payday = safe_payday(today.year, today.month - 1)

    chart_start = last_payday
    next_payday = today + timedelta(days=sealing_expand["daysToPayday"])
    total_chart_days = (next_payday.date() - chart_start.date()).days + 1
    ideal_daily = usable_balance / total_chart_days if total_chart_days > 0 else 0

    chart_data = []
    actual_spent_cumulative = 0
    predicted_spent_cumulative = 0
    today_str = today.strftime("%Y-%m-%d")

    for i in range(total_chart_days):
        current_date = chart_start + timedelta(days=i)
        date_str = current_date.strftime("%Y-%m-%d")

        if date_str <= today_str:
            actual_spent_cumulative += spending_by_date.get(date_str, 0)
        else:
            predicted_spent_cumulative += prediction_map.get(date_str, 0)

        ideal_remaining = max(round(usable_balance - (ideal_daily * (i + 1))), 0)
        actual_remaining = max(usable_balance - actual_spent_cumulative, 0)
        predicted_remaining = max(usable_balance - actual_spent_cumulative - predicted_spent_cumulative, 0)

        chart_data.append({
            "date": date_str,
            "idealRemaining": ideal_remaining,
            "actualRemaining": actual_remaining,
            "predictedRemaining": predicted_remaining,
        })

    zero_result = dday_service.calculate_zero_day(
        current_balance=usable_balance,
        predictions=predictions,
        payday_day=payday_day,
        today=today,
        weekday_avg=weekday_avg,
    )

    return {
        "yearMonth": request.yearMonth,
        "currentBalance": request.currentBalance,
        "sealedAmount": sealed_amount,
        "usableBalance": usable_balance,
        "predictedTotalSpending": sealing_expand["predictedSpendUntilPayday"],
        "zeroDate": zero_result["zeroDate"],
        "daysToZero": zero_result["daysToZero"],
        "isSurviving": zero_result["isSurviving"],
        "chartData": chart_data,
    }
