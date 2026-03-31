from fastapi import APIRouter
from app.schemas.report import ReportGenerateRequest, ReportGenerateResponse
from app.application.report_usecase import execute

router = APIRouter()


@router.post("/reports/generate", response_model=ReportGenerateResponse)
def create_report(request: ReportGenerateRequest):
    return execute(request)
