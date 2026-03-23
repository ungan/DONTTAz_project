from pydantic import BaseModel
from typing import Optional


class Transaction(BaseModel):
    date: str
    amount: int
    type: str
    balance: int = 0
    merchant: Optional[str] = None
    category: Optional[str] = None
    summary: Optional[str] = None
    memo: Optional[str] = None
    transaction_id: Optional[str] = None