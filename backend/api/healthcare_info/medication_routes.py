from zoneinfo import ZoneInfo

from fastapi import APIRouter, Depends, HTTPException, Form
from sqlalchemy import null
from sqlalchemy.orm import Session
from models.user import User
from models.base import Base
from models.medication import Medication
from db.session import SessionLocal
from schemas.medication import MedicationCreate, MedicationModify
from schemas.user import UserCreate, UserUpdate, UserResponse
from typing import List, Annotated
from core.security import get_password_hash, get_token_from_header, get_id_from_token, verify_password, verify_token
from fastapi.responses import JSONResponse

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# Simple route for adding medicine to the database, should return simple success message or error message
@router.post("/medication/add_medication")
async def add_medication(medication_data: MedicationCreate, db: Session = Depends(get_db),
                         token: str = Depends(get_token_from_header)):
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Invalid token")
    # get user id from token
    user_id = get_id_from_token(token)

    # store schedule_start as a datetime object with timezone, might be needed later
    # schedule_start = datetime.fromisoformat(schedule_start)
    # schedule_start = schedule_start.replace(tzinfo=ZoneInfo("UTC"))
    print(user_id)
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")
    try:
        # get all the data from the request
        new_medication = Medication(
            owner_id=user_id,
            name=medication_data.name,
            description=medication_data.description,
            color=medication_data.color,
            imprint=medication_data.imprint,
            shape=medication_data.shape,
            dosage=medication_data.dosage,
            intake_method=medication_data.intake_method,
            init_vector=medication_data.init_vector
        )

        # Add the new medication to the database
        db.add(new_medication)
        db.commit()
        db.refresh(new_medication)
        # return success message 200
        return {"message": "Medication added successfully"}
    except Exception as e:
        # return
        raise HTTPException(status_code=400, detail="Failed to add medication")


@router.get("/medication/get_medications")
async def get_medications(db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Invalid token")
    user_id = get_id_from_token(token)
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")
    medications = db.query(Medication).filter(Medication.owner_id == user_id).all()
    # convert created_date to ISO8601 format with timezone

    for med in medications:
        if med.created_date is not None:
            med.created_date = med.created_date.replace(tzinfo=ZoneInfo("UTC")).isoformat()
        if med.schedule_start is not None:
            med.schedule_start = med.schedule_start.replace(tzinfo=ZoneInfo("UTC")).isoformat()

    # convert to dictionary
    medications = [med.__dict__ for med in medications]
    return medications


@router.post("/medication/modify_medication")
async def modify_medication(medication_data: MedicationModify, db: Session = Depends(get_db),
                            token: str = Depends(get_token_from_header)):
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Invalid token")
    user_id = get_id_from_token(token)
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")
    medication = db.query(Medication).filter(Medication.owner_id == user_id).filter(
        Medication.id == medication_data.id).first()
    if not medication:
        raise HTTPException(status_code=404, detail="Medication not found")
    try:
        medication.name = medication_data.name
        medication.description = medication_data.description
        medication.color = medication_data.color
        medication.imprint = medication_data.imprint
        medication.shape = medication_data.shape
        medication.dosage = medication_data.dosage
        medication.intake_method = medication_data.intake_method
        medication.schedule_start = medication_data.schedule_start
        medication.interval_milliseconds = medication_data.interval_milliseconds
        medication.init_vector = medication_data.init_vector
        db.commit()
        db.refresh(medication)
        return {"message": "Medication modified successfully"}
    except Exception as e:
        raise HTTPException(status_code=400, detail="Failed to modify medication")
