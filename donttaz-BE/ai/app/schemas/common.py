from pydantic import BaseModel, Field
from typing import Optional, List


class AiTransaction(BaseModel):
    id: Optional[str] = None
    transactionDate: str
    amount: int
    counterpartyName: Optional[str] = None
    category: Optional[str] = None
    memo: Optional[str] = None
    type: Optional[str] = None


class CategoryPlanItem(BaseModel):
    categoryName: str
    allocatedAmount: int
    aiComment: Optional[str] = None


class CategoryReportItem(BaseModel):
    categoryName: str
    allocatedAmount: int
    spentAmount: int
    categoryRate: float
    feedback: str


class PersonalizedRecommendationItem(BaseModel):
    type: str
    categoryName: Optional[str] = None
    message: str


class PersonalizedFeedbackItem(BaseModel):
    type: str
    categoryName: Optional[str] = None
    message: str
