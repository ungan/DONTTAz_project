from __future__ import annotations

from typing import Iterable

CATEGORY_KR_MAP = {
    "food": "식비",
    "shopping": "쇼핑",
    "transport": "교통",
    "living": "생활",
    "leisure": "여가",
    "health": "의료/건강",
    "education": "교육",
    "finance": "금융/보험",
    "gift": "경조사/선물",
    "etc": "기타",
    "income": "수입",
    "식비": "식비",
    "쇼핑": "쇼핑",
    "교통": "교통",
    "여가": "여가",
    "의료/건강": "의료/건강",
    "교육": "교육",
    "금융/보험": "금융/보험",
    "경조사/선물": "경조사/선물",
    "기타": "기타",
    "수입": "수입",
    "편의점": "생활",
    "주유": "교통",
    "구독": "여가",
    # SSAFY 금융망 카테고리 매핑
    "대형마트": "생활",
    "교육/육아": "교육",
    "통신": "금융/보험",
    "해외": "쇼핑",
    "생활": "기타",  # SSAFY 생활 = 음식점+카페+편의점+약국 혼합 → NLP가 merchant명으로 재분류
}





class NormalizationService:
    @staticmethod
    def to_korean_category(category: str | None) -> str:
        if not category:
            return "기타"
        return CATEGORY_KR_MAP.get(str(category).strip(), str(category).strip())
    
    @classmethod
    def to_service_category(cls, category: str | None) -> str:
        return cls.to_korean_category(category)


    @classmethod
    def normalize_card_transactions(cls, transactions: Iterable) -> list[dict]:
        result: list[dict] = []
        for tx in transactions:
            item = tx.model_dump() if hasattr(tx, "model_dump") else dict(tx)
            result.append(
                {
                    "transaction_id": item.get("id"),
                    "date": str(item.get("transactionDate", ""))[:10],
                    "amount": int(item.get("amount", 0)),
                    "merchant": item.get("counterpartyName") or "",
                    "category": cls.to_korean_category(item.get("category")),
                    "type": "WITHDRAWAL",
                    "summary": item.get("memo"),
                    "memo": item.get("memo"),
                }
            )
        return result

    @classmethod
    def normalize_account_transactions(cls, transactions: Iterable) -> list[dict]:
        result: list[dict] = []
        for tx in transactions:
            item = tx.model_dump() if hasattr(tx, "model_dump") else dict(tx)
            raw_amount = int(item.get("amount", 0))
            tx_type = item.get("type")
            if not tx_type:
                tx_type = "DEPOSIT" if raw_amount > 0 else "WITHDRAWAL"

            result.append(
                {
                    "transaction_id": item.get("id"),
                    "date": str(item.get("transactionDate", ""))[:10],
                    "amount": abs(raw_amount),
                    "merchant": item.get("counterpartyName") or "",
                    "category": cls.to_korean_category(item.get("category")),
                    "type": tx_type,
                    "summary": item.get("memo"),
                    "memo": item.get("memo"),
                }
            )
        return result
