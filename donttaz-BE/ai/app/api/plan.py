from fastapi import APIRouter
from app.schemas.plan import PlanGenerateRequest, PlanGenerateResponse
from app.application.plan_usecase import execute

router = APIRouter()


@router.post("/plans/generate", response_model=PlanGenerateResponse)
def create_plan(request: PlanGenerateRequest):
    return execute(request)
