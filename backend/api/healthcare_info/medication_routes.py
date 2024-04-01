from datetime import datetime

from db.session import SessionLocal
from fastapi import APIRouter, HTTPException, Form, Depends
from models.user import User
from models.medication import Medication
from zoneinfo import ZoneInfo

from schemas.medication import MedicationCreate
from typing import Annotated, List

from sqlalchemy.orm import Session

from core.security import verify_token, get_id_from_token

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# Simple route for adding medicine to the database, should return simple success message or error message
@router.post("/medication/add_medication")
async def add_medication(token: Annotated[str, Form()], name: str = Form(...), color: str = Form(...),
                         imprint: str = Form(...),
                         shape: str = Form(...), dosage: str = Form(...), intake_method: str = Form(...),
                         description: str = Form(...), schedule_start: str = Form(...),
                         interval_milliseconds: int = Form(...), db: Session = Depends(get_db)):
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
        medication_info = MedicationCreate(name=name, color=color, imprint=imprint, shape=shape, dosage=dosage,
                                           intake_method=intake_method, description=description,
                                           schedule_start=schedule_start,
                                           interval_milliseconds=interval_milliseconds)
        new_name = medication_info.name
        new_color = medication_info.color
        new_imprint = medication_info.imprint
        new_shape = medication_info.shape
        new_dosage = medication_info.dosage
        new_intake_method = medication_info.intake_method
        new_description = medication_info.description
        new_schedule_start = medication_info.schedule_start
        new_interval_milliseconds = medication_info.interval_milliseconds
        new_medication = Medication(name=new_name, color=new_color, imprint=new_imprint,
                                    shape=new_shape, dosage=new_dosage, intake_method=new_intake_method,
                                    description=new_description, schedule_start=new_schedule_start,
                                    interval_milliseconds=new_interval_milliseconds, owner_id=user_id) \
            # Add the new medication to the database
        db.add(new_medication)
        db.commit()
        db.refresh(new_medication)
        return {"message": "Medication added successfully"}
    except Exception as e:
        return {"message": f"Error adding medication: {e}"}


@router.get("/medication/get_medications")
# todo: why did i ever let the token be
#  passed as a query parameter instead of a
#  header or in the body HAHAHAHAHA ill fix this later
async def get_medications(token: str, db: Session = Depends(get_db)):
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Invalid token")
    user_id = get_id_from_token(token)
    print(user_id)
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")
    medications = db.query(Medication).filter(Medication.owner_id == user_id).all()
    # convert created_date to ISO8601 format with timezone

    for med in medications:
        if med.created_date:  # Check if created_date is not None
            med.created_date = med.created_date.replace(tzinfo=ZoneInfo("UTC")).isoformat()
        if med.schedule_start:  # Check if schedule_start is not None
            med.schedule_start = med.schedule_start.replace(tzinfo=ZoneInfo("UTC")).isoformat()

    # convert to dictionary
    medications = [med.__dict__ for med in medications]
    print(medications)
    return medications
