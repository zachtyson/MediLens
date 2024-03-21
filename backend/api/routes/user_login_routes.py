from fastapi import APIRouter, Depends, HTTPException, status, Form
from sqlalchemy.orm import Session
from datetime import timedelta
from backend.models.user import User
from backend.db.session import SessionLocal
from fastapi.security import OAuth2PasswordRequestForm
from typing import Annotated
from backend.core.security import Token, verify_password, access_token_minutes, create_access_token

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.post("/login/e", response_model=Token)
async def create_token(email: Annotated[str, Form()], password: Annotated[str, Form()]):
    user = authenticate_user_email(email, password, SessionLocal())
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token_expires = timedelta(minutes=access_token_minutes)
    # subject = email and user.id in json format
    sub = {"email": user.email, "id": user.id}
    access_token = create_access_token(sub, expires_delta=access_token_expires)
    return {"access_token": access_token, "token_type": "bearer"}


def authenticate_user_email(email: str, password: str, db: Session):
    user = db.query(User).filter(User.email == email).first()
    if not user:
        return False
    if not verify_password(password, user.hashed_password):
        return False
    return user
