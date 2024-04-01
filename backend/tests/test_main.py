import asyncio
from contextlib import asynccontextmanager

import pytest
import pytest_asyncio
from fastapi import FastAPI
from httpx import AsyncClient
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import Session

from main import app
from db.session import AsyncSessionLocal as test_database_session


import pytest

@pytest_asyncio.fixture(name="async_client")
async def async_client():
    async with AsyncClient(app=app, base_url="http://test") as ac:
        yield ac


@asynccontextmanager
@pytest_asyncio.fixture(name="test_db")
async def test_db():
    async with test_database_session() as db:
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
