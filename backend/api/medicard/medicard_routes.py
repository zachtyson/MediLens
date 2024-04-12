from db.session import SessionLocal
from fastapi import APIRouter, HTTPException, Form, Depends
from sqlalchemy.orm import Session
from models.doctor import doctor

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# create a doctor api endpoint using the doctor base model

@router.post("/doctor")
def create_doctor(doctor_name: str = Form(...), specialty: str = Form(...), office_number: str = Form(...), emergency_number: str = Form(...), office_address: str = Form(...), email: str = Form(...), user_id: int = Form(...), db: Session = Depends(get_db)):
    new_doctor = doctor(doctor_name=doctor_name, specialty=specialty, office_number=office_number, emergency_number=emergency_number, office_address=office_address, email=email, user_id=user_id)
    db.add(new_doctor)
    db.commit()
    db.refresh(new_doctor)
    return new_doctor

@router.get("/doctor/{doctor_id}")
def get_doctor(doctor_id: int, db: Session = Depends(get_db)):
    # query the database for the doctor with the doctor_id
    doctor_query = db.query(doctor).filter(doctor.doctor_id == doctor_id).first()
    if doctor_query is None:
        raise HTTPException(status_code=404, detail="Doctor not found")
    return doctor_query
