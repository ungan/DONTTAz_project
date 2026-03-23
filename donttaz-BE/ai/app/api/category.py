from fastapi import APIRouter
from app.clients.api_adapter import map_ssafy_card_response
from app.services.nlp_service import NLPService
from app.schemas.category import CategoryClassifyRequest, CategoryUpdateRequest
from app.services.normalization_service import NormalizationService

router = APIRouter()
normalization_service = NormalizationService()
_nlp_service = NLPService()


@router.post("/categories/classify")
def classify_categories(request: CategoryClassifyRequest):
    normalized_transactions = []
    for i, tx in enumerate(request.transactions, start=1):
        normalized_transactions.append({
            "id": tx.transactionId or i,
            "merchant": tx.merchantName,
            "amount": int(tx.amount),
            "type": "WITHDRAWAL",
            "category": normalization_service.to_service_category(tx.category),
        })

    categorized = _nlp_service.categorize_transactions(normalized_transactions)

    results = []
    for tx in categorized:
        results.append({
            "transactionId": tx.get("id"),
            "merchantName": tx.get("merchant", ""),
            "amount": int(tx.get("amount", 0)),
            "category": normalization_service.to_service_category(tx.get("category", "기타")),
            "confidenceScore": float(tx.get("confidenceScore", 0.35)),
            "matchedKeyword": tx.get("matchedKeyword"),
            "matchType": tx.get("matchType", "default"),
            "isModified": bool(tx.get("isModified", False)),
        })

    return {"categories": results}


@router.post("/categories/classify/ssafy")
def classify_categories_from_ssafy(payload: dict):
    mapped = map_ssafy_card_response(payload)
    request = CategoryClassifyRequest(
        transactions=[
            {
                "transactionId": tx.get("transaction_id"),
                "merchantName": tx.get("merchant", ""),
                "amount": int(tx.get("amount", 0)),
                "category": tx.get("category"),
            }
            for tx in mapped
        ]
    )
    return classify_categories(request)


@router.patch("/categories")
def update_transaction_category(request: CategoryUpdateRequest):
    return {
        "transactionId": request.transactionId,
        "category": normalization_service.to_service_category(request.category),
        "isModified": True,
        "message": "카테고리가 수정되었습니다.",
    }
