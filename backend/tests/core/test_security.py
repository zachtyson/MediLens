import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from httpx import AsyncClient
from jose import jwt
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import Session
from core.security import create_access_token, secret_key, algorithm


@pytest.mark.asyncio
async def test_create_access_token_1():
    sub = {"email": "email@email.com", "id": 1}
    access_token = create_access_token(sub)
    assert access_token is not None
    assert isinstance(access_token, str)
    assert len(access_token) > 0

    # assert that decoded token contains the email and id
    decoded_token = jwt.decode(access_token, secret_key, algorithms=[algorithm])
    assert decoded_token["sub"] == str(sub)

@pytest.mark.asyncio
async def test_create_access_token_2():
    sub = {"email": "email2@email.com", "id": 2}
    access_token = create_access_token(sub)
    assert access_token is not None
    assert isinstance(access_token, str)
    assert len(access_token) > 0

    # assert that decoded token contains the email and id
    decoded_token = jwt.decode(access_token, secret_key, algorithms=[algorithm])
    assert decoded_token["sub"] == str(sub)

@pytest.mark.asyncio
async def test_password_hashing():
    from core.security import get_password_hash, verify_password

    password = "password"
    hashed_password = get_password_hash(password)
    assert hashed_password is not None
    assert isinstance(hashed_password, str)
    assert len(hashed_password) > 0
    assert hashed_password != password
    assert verify_password(password, hashed_password)

    password = "password2"
    hashed_password = get_password_hash(password)
    assert hashed_password is not None
    assert isinstance(hashed_password, str)
    assert len(hashed_password) > 0
    assert hashed_password != password
    assert verify_password(password, hashed_password)

@pytest.mark.asyncio
async def test_verify_token():
    from core.security import verify_token

    sub = {"email": "email@email.com", "id": 1}
    access_token = create_access_token(sub)
    assert verify_token(access_token)

    sub = {"email": "email@email2.com", "id": 2}
    access_token = create_access_token(sub)
    assert verify_token(access_token)


@pytest.mark.asyncio
async def test_get_id_from_token():
    from core.security import get_id_from_token

    sub = {"email": "email@email.com", "id": 1}
    access_token = create_access_token(sub)
    assert get_id_from_token(access_token) == 1

    sub = {"email": "email@email.com", "id": 2}
    access_token = create_access_token(sub)
    assert get_id_from_token(access_token) == 2


