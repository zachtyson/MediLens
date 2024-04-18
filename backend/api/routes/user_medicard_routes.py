from fastapi import APIRouter, Depends, HTTPException, status, Form
from sqlalchemy.orm import Session
from datetime import timedelta
from models.user import User
from db.session import SessionLocal, get_db
from fastapi.security import OAuth2PasswordRequestForm
from typing import Annotated
from core.security import Token, verify_password, access_token_minutes, create_access_token, get_token_from_header, \
    get_id_from_token

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
# get medicard info, token required
@router.get('/medicard/user_info/{user_id}')
def get_medicard(user_id: int, db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    current_user_id = get_id_from_token(token)
    if not current_user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    if current_user_id != user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user.medicard
