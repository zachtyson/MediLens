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


@pytest.mark.asyncio
async def test_create_user_duplicate(async_client: AsyncClient, test_db: AsyncSession):
    email = "duplicate@email.com"
    password = "password123"
    user_data = {"email": email, "password": password}

    # first verify that the user is not already in the database
    async with test_db as db:
        query = select(User).filter(User.email == email)
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user is None

    # create the user
    response = await async_client.post("/users", json=user_data)
    assert response.status_code == 200
    user = response.json()
    assert user["email"] == email

    # try to create the user again
    response = await async_client.post("/users", json=user_data)
    assert response.status_code == 400
    assert response.json() == {"detail": "Email already registered"}

    # delete the user directly from the database
    async with test_db as db:
        query = select(User).filter(User.email == email)
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        await db.delete(db_user)
        await db.commit()

    # verify the user is actually deleted from the database
    async with test_db as db:
        query = select(User).filter(User.email == email)
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user is None


@pytest.mark.asyncio
async def test_create_user_no_email(async_client: AsyncClient, test_db: AsyncSession):
    user_data = {"password": "password123"}
    response = await async_client.post("/users", json=user_data)
    assert response.status_code == 422


@pytest.mark.asyncio
async def test_create_user_no_password(async_client: AsyncClient, test_db: AsyncSession):
    user_data = {"email": "email@email.com"}
    response = await async_client.post("/users", json=user_data)
    assert response.status_code == 422


@pytest.mark.asyncio
async def test_create_user_no_email_password(async_client: AsyncClient, test_db: AsyncSession):
    response = await async_client.post("/users")
    assert response.status_code == 422


@pytest.mark.asyncio
async def test_create_user_invalid_email(async_client: AsyncClient, test_db: AsyncSession):
    user_data = {"email": "invalidemail", "password": "password123"}
    response = await async_client.post("/users", json=user_data)
    assert response.status_code == 422


@pytest.mark.asyncio
async def test_get_users_empty(async_client: AsyncClient, test_db: AsyncSession):
    response = await async_client.get("/users")
    assert response.status_code == 200
    assert response.json() == []


@pytest.mark.asyncio
async def test_get_users(async_client: AsyncClient, test_db: AsyncSession):
    # verify that user1 email, user2 email, and user3 email are not already in the database
    user_data1 = {"email": "user1@email.com", "password": "password123"}
    user_data2 = {"email": "user2@email.com", "password": "password123"}
    user_data3 = {"email": "user3@email.com", "password": "password123"}

    response = await async_client.get("/users")
    assert response.status_code == 200
    users = response.json()

    # assert those users are not in the response
    assert user_data1["email"] not in [user["email"] for user in users]
    assert user_data2["email"] not in [user["email"] for user in users]
    assert user_data3["email"] not in [user["email"] for user in users]

    async with test_db as db:
        db_user1 = User(email=user_data1["email"], hashed_password=get_password_hash(user_data1["password"]))
        db.add(db_user1)
        await db.commit()
        await db.refresh(db_user1)

        db_user2 = User(email=user_data2["email"], hashed_password=get_password_hash(user_data2["password"]))
        db.add(db_user2)
        await db.commit()
        await db.refresh(db_user2)

        db_user3 = User(email=user_data3["email"], hashed_password=get_password_hash(user_data3["password"]))
        db.add(db_user3)
        await db.commit()
        await db.refresh(db_user3)

    response = await async_client.get("/users")
    assert response.status_code == 200
    users = response.json()
    assert len(users) == 3
    # the users might not be in the same order as they were created
    assert user_data1["email"] in [user["email"] for user in users]
    assert user_data2["email"] in [user["email"] for user in users]
    assert user_data3["email"] in [user["email"] for user in users]

    # delete the users directly from the database
    async with test_db as db:
        query = select(User).filter(User.email.in_([user_data1["email"], user_data2["email"], user_data3["email"]]))
        db_users = await db.execute(query)
        db_users = db_users.scalars().all()
        for db_user in db_users:
            await db.delete(db_user)
        await db.commit()

    response = await async_client.get("/users")
    assert response.status_code == 200
    users = response.json()
    # assert those users are not in the response
    assert user_data1["email"] not in [user["email"] for user in users]
    assert user_data2["email"] not in [user["email"] for user in users]
    assert user_data3["email"] not in [user["email"] for user in users]


@pytest.mark.asyncio
async def test_get_user(async_client: AsyncClient, test_db: AsyncSession):
    user_data = {"email": "email@email.com", "password": "password123"}

    # verify that the user is not already in the database
    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user is None

    # create the user
    async with test_db as db:
        # set outer
        db_user = User(email=user_data["email"], hashed_password=get_password_hash(user_data["password"]))
        db.add(db_user)
        await db.commit()
        await db.refresh(db_user)

    # get token from the login route
    response = await async_client.post(f"/login/e",
                                       data={"email": user_data["email"], "password": user_data["password"]})
    assert response.status_code == 200
    token = response.json()["access_token"]

    response = await async_client.get(f"/users/{db_user.id}", headers={"token": f"{token}"})
    assert response.status_code == 200
    user = response.json()
    assert user["email"] == user_data["email"]
    # assert id is the same as the one in the response
    assert user["id"] == db_user.id

    # delete the user directly from the database
    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        await db.delete(db_user)
        await db.commit()


@pytest.mark.asyncio
async def test_get_user_fake_token(async_client: AsyncClient, test_db: AsyncSession):
    fake_token = "fake"
    response = await async_client.get("/users/0", headers={"token": f"{fake_token}"})
    assert response.status_code == 401
    assert response.json() == {"detail": "Invalid token"}

@pytest.mark.asyncio
async def test_get_user_unauthorized(async_client: AsyncClient, test_db: AsyncSession):
    # create the user
    email = "email@email.com"
    password = "password123"
    user_data = {"email": email, "password": password}
    async with test_db as db:
        db_user = User(email=user_data["email"], hashed_password=get_password_hash(user_data["password"]))
        db.add(db_user)
        await db.commit()
        await db.refresh(db_user)

    # get new user id from the database
    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user is not None

    id = db_user.id

    # get token from the login route
    response = await async_client.post(f"/login/e",
                                        data={"email": user_data["email"], "password": user_data["password"]})
    assert response.status_code == 200
    token = response.json()["access_token"]

    # try to get user with a different id

    wrong_id = id + 1

    response = await async_client.get(f"/users/{wrong_id}", headers={"token": f"{token}"})
    assert response.status_code == 401
    assert response.json() == {"detail": "Unauthorized"}

    # delete the user directly from the database
    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        await db.delete(db_user)
        await db.commit()

    # verify the user is actually deleted from the database
    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user is None


@pytest.mark.asyncio
async def test_get_user_not_found(async_client: AsyncClient, test_db: AsyncSession):
    # todo: come back when the delete user route is implemented
    assert True

@pytest.mark.asyncio
async def test_update_user(async_client: AsyncClient, test_db: AsyncSession):
    user_data = {"email": "update@email.com", "password": "password123"}

    # verify that the user is not already in the database
    async with test_db as db:
        query = select(User).filter(User.email == user_data["email"])
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user is None

    # create the user
    async with test_db as db:
        db_user = User(email=user_data["email"], hashed_password=get_password_hash(user_data["password"]))
        db.add(db_user)
        await db.commit()
        await db.refresh(db_user)
    # testing this function DOES require the creation of a token, so this can't be tested in isolation
    # email and password in request body
    response = await async_client.post(f"/login/e",
                                       data={"email": user_data["email"], "password": user_data["password"]})
    assert response.status_code == 200
    # get token from access_token
    token = response.json()["access_token"]
    # update the user
    new_email = "updated2@email.com"
    new_password = "newpassword123"
    response = await async_client.put(f"/users/{db_user.id}",
                                      json={"email": new_email, "password": new_password},
                                      headers={"token": f"{token}"})
    assert response.status_code == 200
    user = response.json()
    assert user["email"] == new_email
    # verify the user is actually updated in the database
    async with test_db as db:
        query = select(User).filter(User.id == db_user.id)
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        assert db_user.email == new_email
        assert db_user.hashed_password != new_password

    # delete the user directly from the database
    async with test_db as db:
        query = select(User).filter(User.email == new_email)
        db_user = await db.execute(query)
        db_user = db_user.scalars().first()
        await db.delete(db_user)
        await db.commit()

