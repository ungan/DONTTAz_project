import numpy as np
from calendar import monthrange
from datetime import datetime, timedelta
from typing import Optional


def _safe_payday(year: int, month: int, payday_day: int) -> datetime:
    last_day = monthrange(year, month)[1]
    return datetime(year, month, min(payday_day, last_day))


class SealingService:
    def expand_predictions_until_payday(
        self,
        predictions: list[dict],
        today: Optional[datetime] = None,
        payday_day: int = 25,
        weekday_avg: dict = None,
    ) -> dict:
        if today is None:
            today = datetime.now()

        year = today.year
        month = today.month

        if today.day < payday_day:
            next_payday = _safe_payday(year, month, payday_day)
        else:
            if month == 12:
                next_payday = _safe_payday(year + 1, 1, payday_day)
            else:
                next_payday = _safe_payday(year, month + 1, payday_day)

        days_to_payday = (next_payday.date() - today.date()).days

        pred_values = [int(p.get("predictedSpending", 0)) for p in predictions]
        if not pred_values:
            pred_values = [0] * 7

        if len(pred_values) < 7:
            pred_values = pred_values + [pred_values[-1]] * (7 - len(pred_values))

        expanded = []
        for i in range(days_to_payday):
            if i < len(pred_values):
                expanded.append(pred_values[i])
            elif weekday_avg:
                future_date = today + timedelta(days=i)
                fallback = weekday_avg.get(future_date.weekday(), pred_values[-1])
                expanded.append(int(fallback))
            else:
                expanded.append(pred_values[-1])

        return {
            "daysToPayday": days_to_payday,
            "expandedPredictions": expanded,
            "predictedSpendUntilPayday": int(sum(expanded)),
        }

    def calculate_recent_daily_std(self, daily_amounts: list[int]) -> float:
        if not daily_amounts:
            return 0.0
        return float(np.std(daily_amounts))

    def calculate_sealed_amount(
        self,
        available_money: int,
        predicted_spend_until_payday: int,
        recent_daily_amounts: list[int],
        alpha: float = 0.15,
        beta: float = 1.0,
    ) -> dict:
        rolling_std = self.calculate_recent_daily_std(recent_daily_amounts)

        risk_buffer = (alpha * predicted_spend_until_payday) + (beta * rolling_std)
        required_budget = predicted_spend_until_payday + risk_buffer

        usable_budget = max(round(required_budget), 0)
        usable_budget = min(usable_budget, available_money)

        min_usable_budget = round(available_money * 0.2)
        if usable_budget < min_usable_budget:
            usable_budget = min_usable_budget

        usable_budget = min(usable_budget, available_money)
        sealed_amount = max(available_money - usable_budget, 0)

        warning = None
        if predicted_spend_until_payday >= available_money:
            warning = "예측 지출이 잔액을 초과합니다. 봉인 금액이 0원으로 설정됩니다."
        elif sealed_amount == 0:
            warning = "지출 예측 기반 필요 예산이 잔액 전체를 차지해 봉인이 불가합니다."

        return {
            "predictedSpendUntilPayday": int(predicted_spend_until_payday),
            "rollingStd": round(rolling_std),
            "riskBuffer": round(risk_buffer),
            "requiredBudget": round(required_budget),
            "sealedAmount": int(sealed_amount),
            "usableBudget": int(usable_budget),
            "warning": warning,
        }