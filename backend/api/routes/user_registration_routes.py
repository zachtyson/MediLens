from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models.user import User
from db.session import SessionLocal
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


@router.get("/users", response_model=List[UserResponse])
async def get_users(db: Session = Depends(get_db)):
    # this definitely should not be in a production environment, so we will remove it later
    users = db.query(User).all()
    return users


@router.get("/users/{user_id}", response_model=UserResponse)
async def get_user(user_id: int, db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    # if current_user_id is None then the token is invalid
    current_user_id = get_id_from_token(token)
    if not current_user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    if current_user_id != user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        # honestly this should never happen since the token is valid
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.post("/users", response_model=UserResponse)
async def create_user(user: UserCreate, db: Session = Depends(get_db)):
    print("User: ", user)
    # check for email conflict
    db_user = db.query(User).filter(User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    user.password = get_password_hash(user.password)

    db_user = User(email=user.email, hashed_password=user.password, name=user.name)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


# currently not used anywhere in the frontend
@router.put("/users/{user_id}", response_model=UserResponse)
async def update_user(user_id: int, user: UserUpdate, db: Session = Depends(get_db),
                      token: str = Depends(get_token_from_header)):
    # if current_user_id is None then the token is invalid
    current_user_id = get_id_from_token(token)
    if not current_user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    if current_user_id != user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
    db_user = db.query(User).filter(User.id == user_id).first()
    if not db_user:
        raise HTTPException(status_code=404, detail="User not found")
    if user.email:
        db_user.email = user.email
    if user.password:
        db_user.password = user.password
    db.commit()
    db.refresh(db_user)
    return db_user


