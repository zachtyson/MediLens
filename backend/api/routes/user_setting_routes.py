from fastapi import APIRouter, Depends, HTTPException, Form
from sqlalchemy.orm import Session
from models.user import User
from models.base import Base
from models.medication import Medication
from db.session import SessionLocal
from schemas.user import UserCreate, UserUpdate, UserResponse
from typing import List, Annotated
from core.security import get_password_hash, get_token_from_header, get_id_from_token, verify_password
from fastapi.responses import JSONResponse

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# Change email to new email, takes in user token, password, and new email
@router.post("/users/email", response_model=UserResponse)
async def change_email(
        old_email: Annotated[str, Form()],
        new_email: Annotated[str, Form()],
        password: Annotated[str, Form()],
        db: Session = Depends(get_db),
        token: str = Depends(get_token_from_header),):
    user_id = get_id_from_token(token)

    # Return same error no matter what to prevent user enumeration
    if not user_id:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    if not verify_password(password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    if user.email != old_email:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user.email = new_email
    db.commit()
    db.refresh(user)
    return user


# Change password, takes in user token, old password, and new password
@router.post("/users/password_change", response_model=UserResponse)
async def change_password(old_password: Annotated[str, Form()],
                          new_password: Annotated[str, Form()],
                          db: Session = Depends(get_db),
                          token: str = Depends(get_token_from_header),):
    if not old_password or not new_password:
        raise HTTPException(status_code=400, detail="Missing password")
    user_id = get_id_from_token(token)

    # Return same error no matter what to prevent user enumeration
    if not user_id:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    if not verify_password(old_password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user.hashed_password = get_password_hash(new_password)
    db.commit()
    db.refresh(user)
    return user


# Delete account, takes in user token and password
# Returns simple 200 status code
@router.post("/users/delete_account")
async def delete_account(password: Annotated[str, Form()],
                         db: Session = Depends(get_db),
                         token: str = Depends(get_token_from_header),):
    user_id = get_id_from_token(token)

    # Return same error no matter what to prevent user enumeration
    if not user_id:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=401, detail="Failed to authenticate user")
    if not verify_password(password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Failed to authenticate user")

    # Go through all user data and delete it
    medications = db.query(Medication).filter(Medication.owner_id == user_id).all()
    for medication in medications:
        db.delete(medication)

    db.delete(user)
    db.commit()
