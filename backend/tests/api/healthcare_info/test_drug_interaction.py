import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from httpx import AsyncClient
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import Session

from core.security import get_password_hash
from models.drug_interaction import DrugInteraction
from models.user import User
from tests.test_main import async_client, test_db

import requests

# 5 test drugs with differing interactions with eachother
# drug_a, drug_b, drug_c, drug_d, drug_e
# drug_a interacts with drug_b (severity: mild)
# drug_b interacts with drug_c (severity: moderate)
# drug_c interacts with drug_d (severity: severe)
# drug_d interacts with drug_e (severity: moderate)
# drug_e interacts with drug_a (severity: mild)

interaction_1 = {
    "drug_a": "drug_a",
    "drug_b": "drug_b",
    "severity": "mild",
    "description": "drug_a interacts with drug_b",
    "extended_description": "drug_a interacts with drug_b extended"
}

interaction_2 = {
    "drug_a": "drug_b",
    "drug_b": "drug_c",
    "severity": "moderate",
    "description": "drug_b interacts with drug_c",
    "extended_description": "drug_b interacts with drug_c extended"
}

interaction_3 = {
    "drug_a": "drug_c",
    "drug_b": "drug_d",
    "severity": "severe",
    "description": "drug_c interacts with drug_d",
    "extended_description": "drug_c interacts with drug_d extended"
}

interaction_4 = {
    "drug_a": "drug_d",
    "drug_b": "drug_e",
    "severity": "moderate",
    "description": "drug_d interacts with drug_e",
    "extended_description": "drug_d interacts with drug_e extended"
}

interaction_5 = {
    "drug_a": "drug_a",
    "drug_b": "drug_e",
    "severity": "mild",
    "description": "drug_e interacts with drug_a",
    "extended_description": "drug_e interacts with drug_a extended"
}

@pytest.mark.asyncio
async def test_get_medication_interaction_1(async_client: AsyncClient, test_db: AsyncSession):
    # clear the testing interaction table
    await test_db.execute("DELETE FROM drug_interactions")
    await test_db.commit()

    # verify that the table is empty
    i = await test_db.execute(select(DrugInteraction))
    i = i.all()
    interactions_arr = [interaction_1, interaction_2, interaction_3, interaction_4, interaction_5]

    assert len(i) == 0

    # add all five interactions to the table
    # await db.execute(User.__table__.insert().values(email=user["email"], hashed_password=hashed_password))
    async with test_db as db:
        for interaction in interactions_arr:
            await db.execute(DrugInteraction.__table__.insert().values(drug_a=interaction["drug_a"],
                                                                       drug_b=interaction["drug_b"],
                                                                       severity=interaction["severity"],
                                                                       description=interaction["description"],
                                                                       extended_description=interaction[
                                                                           "extended_description"]))
        await db.commit()

    # verify that the table has five interactions
    i = await test_db.execute(select(DrugInteraction))
    i = i.all()

    assert len(i) == 5

    # test the first interaction between drug_a and drug_b

    response = await async_client.get("/medication/interactions?drug_a=drug_a&drug_b=drug_b")
    assert response.status_code == 200

    interaction = response.json()

    assert interaction["drug_a"] == "drug_a"
    assert interaction["drug_b"] == "drug_b"
    assert interaction["severity"] == "mild"
    assert interaction["description"] == "drug_a interacts with drug_b"
    assert interaction["extended_description"] == "drug_a interacts with drug_b extended"

    # test the second interaction

    response = await async_client.get("/medication/interactions?drug_a=drug_b&drug_b=drug_c")
    assert response.status_code == 200

    interaction = response.json()

    assert interaction["drug_a"] == "drug_b"
    assert interaction["drug_b"] == "drug_c"
    assert interaction["severity"] == "moderate"
    assert interaction["description"] == "drug_b interacts with drug_c"
    assert interaction["extended_description"] == "drug_b interacts with drug_c extended"

    # test the third interaction

    response = await async_client.get("/medication/interactions?drug_a=drug_d&drug_b=drug_c")
    assert response.status_code == 200

    interaction = response.json()

    assert interaction["drug_a"] == "drug_c"
    assert interaction["drug_b"] == "drug_d"
    assert interaction["severity"] == "severe"
    assert interaction["description"] == "drug_c interacts with drug_d"
    assert interaction["extended_description"] == "drug_c interacts with drug_d extended"

    # test the fourth interaction

    response = await async_client.get("/medication/interactions?drug_a=drug_d&drug_b=drug_e")
    assert response.status_code == 200

    interaction = response.json()

    assert interaction["drug_a"] == "drug_d"
    assert interaction["drug_b"] == "drug_e"
    assert interaction["severity"] == "moderate"
    assert interaction["description"] == "drug_d interacts with drug_e"
    assert interaction["extended_description"] == "drug_d interacts with drug_e extended"

    # test the fifth interaction

    response = await async_client.get("/medication/interactions?drug_a=drug_e&drug_b=drug_a")
    assert response.status_code == 200

    interaction = response.json()

    assert interaction["drug_a"] == "drug_a"
    assert interaction["drug_b"] == "drug_e"
    assert interaction["severity"] == "mild"
    assert interaction["description"] == "drug_e interacts with drug_a"
    assert interaction["extended_description"] == "drug_e interacts with drug_a extended"

    # test an interaction that does not exist, such as drug_a and drug_c

    response = await async_client.get("/medication/interactions?drug_a=drug_a&drug_b=drug_c")
    assert response.status_code == 204

    # delete all interactions from the table
    await test_db.execute("DELETE FROM drug_interactions")
    await test_db.commit()

    # verify that the table is empty
    interactions = await test_db.execute(select(DrugInteraction))
    interactions = interactions.all()

    assert len(interactions) == 0
