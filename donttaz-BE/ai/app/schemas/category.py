from pydantic import BaseModel, Field
from typing import List, Optional


class CategoryTransaction(BaseModel):
    transactionId: Optional[str] = None
    merchantName: str = ""
    amount: int = 0
    category: Optional[str] = None
    description: Optional[str] = None


class CategoryClassifyRequest(BaseModel):
    transactions: List[CategoryTransaction] = Field(default_factory=list)


class CategoryUpdateRequest(BaseModel):
    transactionId: str
    category: str
