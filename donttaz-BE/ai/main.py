from dotenv import load_dotenv
load_dotenv()

import logging
import traceback
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.api.report import router as report_router
from app.api.plan import router as plan_router
from app.api.category import router as category_router
from app.api.burndown import router as burndown_router
from prometheus_fastapi_instrumentator import Instrumentator

logger = logging.getLogger("uvicorn.error")

app = FastAPI(
    title="Donttaz AI Service",
    description="Internal AI calculation service for Donttaz",
    version="1.1.2",
)

@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled Exception: {str(exc)}")
    logger.error(traceback.format_exc())
    return JSONResponse(
        status_code=500,
        content={"status": "ERROR", "message": "Internal Server Error", "detail": str(exc)},
    )

# Prometheus 메트릭스 등록 (Dashboard 'DOWN' 현상 해결용)
Instrumentator().instrument(app).expose(app)


@app.get("/")
def read_root():
    return {"message": "Welcome to Donttaz AI Service"}


@app.get("/health")
def health_check():
    return {"status": "ok"}


app.include_router(report_router, prefix="/internal/ai", tags=["AI Internal"])
app.include_router(plan_router, prefix="/internal/ai", tags=["AI Internal"])
app.include_router(category_router, prefix="/internal/ai", tags=["AI Internal"])
app.include_router(burndown_router, prefix="/internal/ai", tags=["AI Internal"])
