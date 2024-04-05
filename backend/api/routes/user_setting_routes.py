from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models.user import User
from models.base import Base
from models.medication import Medication
from db.session import SessionLocal
from schemas.user import UserCreate, UserUpdate, UserResponse
from typing import List
from core.security import get_password_hash, get_token_from_header, get_id_from_token, verify_password

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# Change email to new email, takes in user token, password, and new email
@router.put("/users/email", response_model=UserResponse)
async def change_email(token: str = Depends(get_token_from_header), password: str = None, new_email: str = None,
                       db: Session = Depends(get_db)):
    user_id = get_id_from_token(token)

    # Return same error no matter what to prevent user enumeration
    if not user_id:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Failed to authenticate user")
    if not verify_password(password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user.email = new_email
    db.commit()
    db.refresh(user)
    return user


# Change password, takes in user token, old password, and new password
@router.put("/users/password", response_model=UserResponse)
async def change_password(token: str = Depends(get_token_from_header), old_password: str = None,
                          new_password: str = None, db: Session = Depends(get_db)):
    user_id = get_id_from_token(token)

    # Return same error no matter what to prevent user enumeration
    if not user_id:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Failed to authenticate user")
    if not verify_password(old_password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user.hashed_password = get_password_hash(new_password)
    db.commit()
    db.refresh(user)
    return user


# Delete account, takes in user token and password
@router.delete("/users", response_model=UserResponse)
async def delete_account(token: str = Depends(get_token_from_header), password: str = None, db: Session = Depends(get_db)):
    user_id = get_id_from_token(token)

    # Return same error no matter what to prevent user enumeration
    if not user_id:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Failed to authenticate user")
    if not verify_password(password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Failed to authenticate user")

    # Go through all user data and delete it
    medications = db.query(Medication).filter(Medication.owner_id == user_id).all()
    for medication in medications:
        db.delete(medication)

    db.delete(user)
    db.commit()
    return user
