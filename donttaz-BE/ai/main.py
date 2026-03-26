from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI
from app.api.report import router as report_router
from app.api.plan import router as plan_router
from app.api.category import router as category_router
from app.api.burndown import router as burndown_router

app = FastAPI(
    title="Donttaz AI Service",
    description="Internal AI calculation service for Donttaz",
    version="1.1.0",
)


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
