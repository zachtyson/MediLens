from db.session import SessionLocal
from fastapi import APIRouter, HTTPException, Form
from models.medication import Medication
from models.user import User
from schemas.medication import MedicationCreate

from backend.core.security import verify_token, get_id_from_token

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# Simple route for adding medicine to the database, should return simple success message or error message
@router.post("/medication/add_medication")
async def add_medication(token: str = Form(...), medication_info: MedicationCreate = Form(...)):
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Invalid token")
    # Try to find the user in the database
    db = get_db()
    # get user id from token
    user_id = get_id_from_token(token)
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")
    try :
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
                                    interval_milliseconds=new_interval_milliseconds, owner_id=user_id)\
        # Add the new medication to the database
        db.add(new_medication)
        db.commit()
        db.refresh(new_medication)
        return {"message": "Medication added successfully"}
    except Exception as e:
        return {"message": f"Error adding medication: {e}"}


@router.get("/medication/get_medications")
async def get_medications(token: str):
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Invalid token")
    db = get_db()
    user_id = get_id_from_token(token)
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")
    medications = db.query(Medication).filter(Medication.owner_id == user_id).all()
    return medications
