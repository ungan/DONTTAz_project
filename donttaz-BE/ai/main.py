from fastapi import FastAPI
from prometheus_fastapi_instrumentator import Instrumentator

app = FastAPI(
    title="Donttaz AI Service",
    description="AI Service API for Donttaz",
    version="1.0.0",
)

Instrumentator().instrument(app).expose(app)

@app.get("/")
def read_root():
    return {"message": "Welcome to Donttaz AI Service"}

@app.get("/health")
def health_check():
    return {"status": "ok"}
