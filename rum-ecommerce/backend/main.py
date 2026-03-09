"""
Classic Jazz – FastAPI backend.
Run from repo root: uvicorn backend.main:app --reload
Or with Docker: PYTHONPATH=/app uvicorn backend.main:app --host 0.0.0.0 --port 8000
"""
import os
from contextlib import asynccontextmanager

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from backend.routers import auth, orders, customers

load_dotenv()


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: verify MongoDB connection
    from backend.database import get_client
    get_client().admin.command("ping")
    yield
    # Shutdown: close client
    from backend.database import close_client
    close_client()


app = FastAPI(
    title="Classic Jazz API",
    description="Backend for Classic Jazz e-commerce (auth + orders)",
    version="0.1.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000", "http://127.0.0.1:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(orders.router)
app.include_router(customers.router)


@app.get("/")
def root():
    return {
        "message": "Classic Jazz API",
        "docs": "/docs",
        "frontend": "Use the web app at http://localhost:8080",
    }


@app.get("/health")
def health():
    return {"status": "ok"}
