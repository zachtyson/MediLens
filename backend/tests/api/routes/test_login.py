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


def user_to_create():
    return {
        "email": "email@email.com",
        "password": "password"
    }


@pytest.mark.asyncio
async def test_successful_login(async_client: AsyncClient, test_db: AsyncSession):
    user = user_to_create()

    # Create user directly in the database

    async with test_db as db:
        hashed_password = get_password_hash(user["password"])
        await db.execute(User.__table__.insert().values(email=user["email"], hashed_password=hashed_password))
        await db.commit()

    # Test login

    response = await async_client.post("/login/e", data={"email": user["email"], "password": user["password"]})
    assert response.status_code == 200
    assert "access_token" in response.json()
    assert "token_type" in response.json()

    # Delete user from the database

    async with test_db as db:
        await db.execute(User.__table__.delete().where(User.email == user["email"]))
        await db.commit()


@pytest.mark.asyncio
async def test_unsuccessful_login_wrong_password(async_client: AsyncClient, test_db: AsyncSession):
    user = user_to_create()

    # Create user directly in the database

    async with test_db as db:
        hashed_password = get_password_hash(user["password"])
        await db.execute(User.__table__.insert().values(email=user["email"], hashed_password=hashed_password))
        await db.commit()

    # Test login

    response = await async_client.post("/login/e", data={"email": user["email"], "password": "wrong_password"})
    assert response.status_code == 401
    assert "access_token" not in response.json()
    assert "token_type" not in response.json()

    # Delete user from the database

    async with test_db as db:
        await db.execute(User.__table__.delete().where(User.email == user["email"]))
        await db.commit()


@pytest.mark.asyncio
async def test_unsuccessful_login_wrong_email(async_client: AsyncClient, test_db: AsyncSession):
    user = user_to_create()

    # Create user directly in the database

    async with test_db as db:
        hashed_password = get_password_hash(user["password"])
        await db.execute(User.__table__.insert().values(email=user["email"], hashed_password=hashed_password))
        await db.commit()

    # Test login

    response = await async_client.post("/login/e", data={"email": "wrong_email", "password": user["password"]})
    assert response.status_code == 401
    assert "access_token" not in response.json()
    assert "token_type" not in response.json()

    # Delete user from the database

    async with test_db as db:
        await db.execute(User.__table__.delete().where(User.email == user["email"]))
        await db.commit()


@pytest.mark.asyncio
async def test_unsuccessful_login_wrong_email_and_password(async_client: AsyncClient, test_db: AsyncSession
                                                           ):
    user = user_to_create()

    # Create user directly in the database

    async with test_db as db:
        hashed_password = get_password_hash(user["password"])
        await db.execute(User.__table__.insert().values(email=user["email"], hashed_password=hashed_password))
        await db.commit()

    # Test login

    response = await async_client.post("/login/e", data={"email": "wrong_email", "password": "wrong_password"})
    assert response.status_code == 401
    assert "access_token" not in response.json()
    assert "token_type" not in response.json()

    # Delete user from the database

    async with test_db as db:
        await db.execute(User.__table__.delete().where(User.email == user["email"]))
        await db.commit()


@pytest.mark.asyncio
async def test_missing_email(async_client: AsyncClient):
    response = await async_client.post("/login/e", data={"password": "password"})
    assert response.status_code == 422
    assert "access_token" not in response.json()
    assert "token_type" not in response.json()


@pytest.mark.asyncio
async def test_missing_password(async_client: AsyncClient):
    response = await async_client.post("/login/e", data={"email": "email@email.com"})
    assert response.status_code == 422
    assert "access_token" not in response.json()
    assert "token_type" not in response.json()


@pytest.mark.asyncio
async def test_missing_email_and_password(async_client: AsyncClient):
    response = await async_client.post("/login/e")
    assert response.status_code == 422
    assert "access_token" not in response.json()
    assert "token_type" not in response.json()
