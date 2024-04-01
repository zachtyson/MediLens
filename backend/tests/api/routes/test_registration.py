import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from httpx import AsyncClient
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import Session

from core.security import get_password_hash
from models.user import User
from tests.test_main import async_client, test_db


@pytest.fixture
def new_user():
    return {
        "email": "test@example.com",
        "password": "strongpassword"
    }


# async_client, test_db, client are all defined in confest.py

@pytest.mark.asyncio
async def test_create_user(async_client: AsyncClient, test_db: AsyncSession):
    user_data = {"email": "testuser@example.com", "password": "password123"}
    response = await async_client.post("/users", json=user_data)
    assert response.status_code == 200
    user = response.json()
    assert user["email"] == user_data["email"]
    # Verify the user is actually saved in the database
    async with test_db as db:
        # Query the database for the user
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user.email == user_data["email"]
        assert db_user.hashed_password != user_data["password"]

        # Query based on the given id from the response
        query = select(User).filter(User.id == user["id"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user.email == user_data["email"]

    # delete the user directly from the database
    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        await db.delete(db_user)
        await db.commit()
    assert response.status_code == 200
    # Verify the user is actually deleted from the database

    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user is None

