from datetime import datetime, timedelta
from typing import Optional

from app.services.sealing_service import SealingService


class DDayService:
    def __init__(self):
        self._sealing = SealingService()

    def calculate_zero_day(
        self,
        current_balance: int,
        predictions: list[dict],
        payday_day: int = 25,
        today: Optional[datetime] = None,
        weekday_avg: dict = None,
    ) -> dict:
        if today is None:
            today = datetime.now()

        if current_balance <= 0:
            return {
                "daysToZero": 0,
                "zeroDate": today.strftime("%Y-%m-%d"),
                "isSurviving": False,
            }

        expand = self._sealing.expand_predictions_until_payday(
            predictions=predictions,
            today=today,
            payday_day=payday_day,
            weekday_avg=weekday_avg,
        )

        days_to_payday = expand["daysToPayday"]
        expanded = expand["expandedPredictions"]

        if days_to_payday <= 0:
            return {
                "daysToZero": None,
                "zeroDate": None,
                "isSurviving": True,
            }

        remaining = current_balance
        zero_date = None

        for idx, spend in enumerate(expanded, start=1):
            remaining -= spend
            if remaining <= 0:
                zero_date = today + timedelta(days=idx)
                break

        if zero_date is None:
            return {
                "daysToZero": None,
                "zeroDate": None,
                "isSurviving": True,
            }

        return {
            "daysToZero": (zero_date.date() - today.date()).days,
            "zeroDate": zero_date.strftime("%Y-%m-%d"),
            "isSurviving": False,
        }
