from fastapi import APIRouter
from app.schemas.burndown import BurndownRequest, BurndownResponse
from app.application.burndown_usecase import execute

router = APIRouter()


@router.post("/burndown/calculate", response_model=BurndownResponse)
def get_burndown(request: BurndownRequest):
    return execute(request)
