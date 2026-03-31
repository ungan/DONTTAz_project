from pydantic import BaseModel, Field
from typing import List, Optional
from app.schemas.common import (
    AiTransaction,
    CategoryPlanItem,
    CategoryReportItem,
    PersonalizedFeedbackItem,
)


class ReportPlanSummary(BaseModel):
    totalBudget: int
    sealedAmount: int = 0
    categoryPlans: List[CategoryPlanItem] = Field(default_factory=list)


class ReportGenerateRequest(BaseModel):
    yearMonth: str
    paydayDay: Optional[int] = None
    plan: Optional[ReportPlanSummary] = None
    cardTransactions: List[AiTransaction] = Field(default_factory=list)
    accountTransactions: List[AiTransaction] = Field(default_factory=list)


class ReportGenerateResult(BaseModel):
    yearMonth: str
    sealedAmount: int
    totalBudget: int
    totalSpent: int
    achievementRate: float
    spendingPersona: Optional[str] = None
    aiOverallFeedback: str
    categoryReports: List[CategoryReportItem]
    personalizedFeedback: List[PersonalizedFeedbackItem] = Field(default_factory=list)


class ReportGenerateResponse(BaseModel):
    status: str
    message: str
    data: ReportGenerateResult
