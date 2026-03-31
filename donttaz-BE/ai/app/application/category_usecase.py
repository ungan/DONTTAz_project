"""
카테고리 분류 유스케이스 — app/api/category.py의 진입점에서 호출됨.

NLP 기반 카테고리 분류 및 사용자 수정 반영을 오케스트레이션.
"""
from __future__ import annotations

from app.domains.transaction.normalizer import NormalizationService
from app.domains.transaction.classifier import get_nlp_service

_normalization_service = NormalizationService()
_nlp_service = get_nlp_service()


def classify(request) -> dict:
    normalized_transactions = []
    for i, tx in enumerate(request.transactions, start=1):
        normalized_transactions.append({
            "id": tx.transactionId or i,
            "merchant": tx.counterpartyName,
            "amount": int(tx.amount),
            "type": "WITHDRAWAL",
            "category": _normalization_service.to_service_category(tx.category),
        })

    categorized = _nlp_service.categorize_transactions(normalized_transactions)

    results = []
    for tx in categorized:
        results.append({
            "transactionId": tx.get("id"),
            "merchantName": tx.get("merchant", ""),
            "amount": int(tx.get("amount", 0)),
            "category": tx.get("category", "기타"),
            "confidenceScore": float(tx.get("confidenceScore", 0.35)),
            "matchedKeyword": tx.get("matchedKeyword"),
            "matchType": tx.get("matchType", "default"),
            "isModified": bool(tx.get("isModified", False)),
        })

    return {"categories": results}


def update_category(request) -> dict:
    return {
        "transactionId": request.transactionId,
        "category": _normalization_service.to_service_category(request.category),
        "isModified": True,
        "message": "카테고리가 수정되었습니다.",
    }
