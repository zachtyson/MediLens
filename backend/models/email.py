
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from .base import Base
from .user import User

class EmailRequest(Base):
    to: str
    subject: str
    body: str

class EmailResponse(Base):
    message: str
