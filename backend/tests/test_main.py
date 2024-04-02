import asyncio
from contextlib import asynccontextmanager

import pytest
import pytest_asyncio
from fastapi import FastAPI
from httpx import AsyncClient
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import Session, sessionmaker

from main import app
from core.config import settings

# pytest replace

TEST_URL = settings.DATABASE_URL.replace("pymysql", "aiomysql")


import pytest

@pytest_asyncio.fixture(name="async_client")
async def async_client():
    async with AsyncClient(app=app, base_url="http://test") as ac:
        yield ac


@asynccontextmanager
@pytest_asyncio.fixture(name="test_db")
async def test_db():
    async_engine = create_async_engine(TEST_URL, echo=True)
    AsyncSessionLocal = sessionmaker(autocommit=False, autoflush=False,
                                 bind=async_engine, class_=AsyncSession, expire_on_commit=False)
    async with AsyncSessionLocal() as db:
        trans = await db.begin()
        try:
            yield db
        except Exception:
            await trans.rollback()
        finally:
            db.close()

@pytest.mark.asyncio
async def test_root(async_client: AsyncClient):
    response = await async_client.get("/")
    assert response.status_code == 200
    assert response.json() == {"Hello": "World"}
