from datetime import datetime

from backend.db.session import SessionLocal
from fastapi import APIRouter, HTTPException, Form, Depends
from backend.models.user import User
from backend.models.medication import Medication
from zoneinfo import ZoneInfo

from backend.schemas.medication import MedicationCreate
from typing import Annotated, List

from sqlalchemy.orm import Session

from backend.core.security import verify_token, get_id_from_token
from backend.models.drug_interaction import DrugInteraction

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get("/medication/interactions")
async def get_medication_interactions(drug_a: str = Form(...), drug_b: str = Form(...), db: Session = Depends(get_db)):
    # sort the drugs alphabetically
    drug_a, drug_b = sorted([drug_a, drug_b])
    interaction = db.query(DrugInteraction).filter(DrugInteraction.drug_a == drug_a, DrugInteraction.drug_b == drug_b).first()
    if interaction is None:
        raise HTTPException(status_code=404, detail="Interaction not found")
    return interaction
