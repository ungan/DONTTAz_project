from pydantic import BaseModel, Field
from typing import List, Optional
from app.schemas.common import AiTransaction, CategoryPlanItem, PersonalizedRecommendationItem


class PlanGenerateRequest(BaseModel):
    targetYearMonth: str
    monthlyIncome: int = 0
    fixedExpense: int = 0
    currentBalance: int
    paydayDay: int = 25
    cardTransactions: List[AiTransaction] = Field(default_factory=list)
    accountTransactions: List[AiTransaction] = Field(default_factory=list)


class PlanGenerateResult(BaseModel):
    targetYearMonth: str
    referenceStartMonth: Optional[str] = None
    referenceEndMonth: Optional[str] = None
    totalBudget: int
    sealedAmount: int
    surplusAmount: int
    paydayDay: int
    riskScore: float
    budgetStrategy: str
    categoryPlans: List[CategoryPlanItem]
    personalizedRecommendations: List[PersonalizedRecommendationItem] = Field(default_factory=list)
    spendingPersona: Optional[str] = None


class PlanGenerateResponse(BaseModel):
    status: str
    message: str
    data: PlanGenerateResult
