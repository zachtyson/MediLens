from datetime import timedelta

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from httpx import AsyncClient
from sqlalchemy import select, inspect
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import Session

from core.security import get_password_hash
from models.user import User
from tests.test_main import async_client, test_db
from core.security import create_access_token, get_password_hash, verify_password
from models.medication import Medication

def get_email():
    return "email@email.com"


def get_password():
    return "password"


def sub_to_create(user: User):
    # sub = {"email": user.email, "id": user.id}
    return {
        "email": user.email,
        "id": user.id
    }


def medication_one_to_create():
    return {
        "name": "medication one",
        "color": "red",
        "imprint": "123",
        "shape": "round",
        "dosage": "10mg",
        "intake_method": "oral",
        "description": "description",
        "schedule_start": "2022-01-01T00:00:00",
        "interval_milliseconds": 1000
    }


def medication_two_to_create():
    return {
        "name": "medication two",
        "color": "blue",
        "imprint": "456",
        "shape": "oval",
        "dosage": "20mg",
        "intake_method": "injectable",  # IDK if this is a real intake method
        "description": "description",
        "schedule_start": "2022-01-01T00:00:00",
        "interval_milliseconds": 2000
    }


def user_to_create():
    return {
        "email": "email@email.com",
        "password": "password"
    }


@pytest.mark.asyncio
async def test_authenticate(async_client: AsyncClient, test_db: AsyncSession):
    # delete all entries in medication table and user table
    async with test_db as db:
        await db.execute(Medication.__table__.delete())
        await db.commit()
    async with test_db as db:
        await db.execute(User.__table__.delete())
        await db.commit()

    # verify the tables are empty
    async with test_db as db:
        users = await db.execute(select(User))
        assert users.all() == []
    async with test_db as db:
        medications = await db.execute(select(Medication))
        assert medications.all() == []

    # delete all users from the database
    async with test_db as db:
        await db.execute(User.__table__.delete())
        await db.commit()
    user = user_to_create()

    # Create user directly in the database

    async with test_db as db:
        hashed_password = get_password_hash(user["password"])
        await db.execute(User.__table__.insert().values(email=user["email"], hashed_password=hashed_password))
        await db.commit()

    user = user_to_create()

    # login

    response = await async_client.post("/login/e", data={"email": user["email"], "password": user["password"]})

    assert response.status_code == 200
    assert "access_token" in response.json()
    assert "token_type" in response.json()
    assert response.json()["access_token"] is not None
    assert response.json()["token_type"] == "bearer"

    access_token = response.json()["access_token"]

    # create medication
    medication_one = medication_one_to_create()

    # add medication with the medication being the body of the request

    response = await async_client.post("/medication/add_medication",
                                       data=medication_one, headers={"token": f"{access_token}"})

    assert response.status_code == 200

    # get medications directly from the database
    async with test_db as db:
        # select medication
        query = select(Medication)
        db_medication = await db.execute(query)
        assert len(db_medication.all()) == 1

        query = select(Medication).filter(Medication.name == medication_one["name"])
        db_medication = await db.execute(query)
        db_medication = db_medication.scalars().first()
        assert db_medication.name == medication_one["name"]
        assert db_medication.color == medication_one["color"]
        assert db_medication.imprint == medication_one["imprint"]
        assert db_medication.shape == medication_one["shape"]
        assert db_medication.dosage == medication_one["dosage"]
        assert db_medication.intake_method == medication_one["intake_method"]
        assert db_medication.description == medication_one["description"]
        assert db_medication.schedule_start.strftime("%Y-%m-%dT%H:%M:%S") == medication_one["schedule_start"]
        assert db_medication.interval_milliseconds == medication_one["interval_milliseconds"]

    # delete all entries in medication table
    async with test_db as db:
        await db.execute(Medication.__table__.delete())
        await db.commit()
    # delete all users from the database
    async with test_db as db:
        await db.execute(User.__table__.delete())
        await db.commit()

