from sqlalchemy import Column, Integer, String, DateTime, func, ForeignKey, Table
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()
metadata = Base.metadata


class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)  # Use a larger integer data type if needed
    email = Column(String(100), index=True, unique=True)
    hashed_password = Column(String(100))
    created_date = Column(DateTime, default=func.now(), nullable=False)
