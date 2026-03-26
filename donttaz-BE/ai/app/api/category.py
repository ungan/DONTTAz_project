from fastapi import APIRouter
from app.clients.api_adapter import map_ssafy_card_response
from app.schemas.category import CategoryClassifyRequest, CategoryUpdateRequest
from app.application.category_usecase import classify, update_category

router = APIRouter()


@router.post("/categories/classify")
def classify_categories(request: CategoryClassifyRequest):
    return classify(request)


@router.post("/categories/classify/ssafy")
def classify_categories_from_ssafy(payload: dict):
    mapped = map_ssafy_card_response(payload)
    request = CategoryClassifyRequest(
        transactions=[
            {
                "transactionId": tx.get("transaction_id"),
                "counterpartyName": tx.get("merchant", ""),
                "amount": int(tx.get("amount", 0)),
                "category": tx.get("category"),
            }
            for tx in mapped
        ]
    )
    return classify(request)


@router.patch("/categories")
def update_transaction_category(request: CategoryUpdateRequest):
    return update_category(request)
