from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from models.doctor import Doctor
from models.user import User
from db.session import SessionLocal
from schemas.doctor import DoctorCreate, DoctorDelete
from schemas.user import UserCreate, UserUpdate, UserResponse
from typing import List
from core.security import get_password_hash, get_token_from_header, get_id_from_token

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.post("/doctor/add_doctor", response_model=UserResponse)
async def add_doctor(doctor: DoctorCreate, db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    print("Doctor: ", doctor)
    user_id = get_id_from_token(token)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")

    # honestly I don't even think that there needs to be conflict checks for doctors
    # since multiple doctors can have the same email, etc, so for now
    # I'm just gonna let the user have multiple doctors that can be completely identical
    db_doctor = Doctor(doctor_name=doctor.doctor_name, specialty=doctor.specialty, office_number=doctor.office_number,
                       emergency_number=doctor.emergency_number, office_address=doctor.office_address,
                       email=doctor.email, owner_id=doctor.user_id)
    db.add(db_doctor)
    db.commit()
    db.refresh(db_doctor)
    return db_doctor


@router.get("/doctor/get_user_doctors", response_model=List[Doctor])
async def get_user_doctors(db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    user_id = get_id_from_token(token)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    doctors = db.query(Doctor).filter(Doctor.owner_id == user_id).all()
    return doctors

@router.post("/doctor/delete_doctor")
async def delete_doctor(doctor: DoctorDelete, db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    user_id = get_id_from_token(token)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    if not db.query(User).filter(User.id == user_id).first():
        raise HTTPException(status_code=404, detail="User not found")
    db_doctor = db.query(Doctor).filter(Doctor.doctor_id == doctor.doctor_id).first()
    if not db_doctor:
        raise HTTPException(status_code=404, detail="Doctor not found")
    db.delete(db_doctor)
    db.commit()
    return {"message": "Doctor deleted successfully"}
