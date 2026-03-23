from typing import List, Optional

from pydantic import BaseModel, Field

from app.schemas.common import AiTransaction


class BurndownPlan(BaseModel):
    totalBudget: int
    sealedAmount: int = 0
    paydayDay: int = 25


class BurndownChartItem(BaseModel):
    date: str
    idealRemaining: int
    actualRemaining: int
    predictedRemaining: int


class BurndownRequest(BaseModel):
    yearMonth: str
    currentBalance: int
    plan: Optional[BurndownPlan] = None
    cardTransactions: List[AiTransaction] = Field(default_factory=list)


class BurndownResponse(BaseModel):
    yearMonth: str
    currentBalance: int
    sealedAmount: int
    usableBalance: int
    predictedTotalSpending: int
    zeroDate: Optional[str]
    daysToZero: Optional[int]
    isSurviving: bool
    chartData: List[BurndownChartItem]
