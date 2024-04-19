from typing import Optional, List
from pydantic import BaseModel, EmailStr
from datetime import datetime


# Base structure for User
class UserBase(BaseModel):
    email: EmailStr


# Used for creating a new user
class UserCreate(UserBase):
    password: str
    name: str


# Used for additional db fields, used for retrieving data from db
class UserInDB(UserBase):
    id: int
    hashed_password: str
    salt: str
    created_date: datetime
    name: str

    class Config:
        orm_mode = True


# Used for updating user information, optional fields
class UserUpdate(BaseModel):
    email: Optional[EmailStr] = None
    password: Optional[str] = None
    name: Optional[str] = None


# Used for returning user information with id and created_date, does not include password
class UserResponse(UserBase):
    id: int
    created_date: datetime
    name: str

    class Config:
        orm_mode = True
