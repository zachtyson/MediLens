from sqlalchemy import Column, Integer, String, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from .base import Base

class Doctor(Base):
    __tablename__ = "Doctor"

    doctor_id = Column(Integer, primary_key=True, index=True)
    doctor_name = Column(String(100), index=True)
    specialty = Column(String(100))
    office_number = Column(String(20))
    emergency_number = Column(String(20))
    office_address = Column(String(255))
    email = Column(String(100), index=True, unique=True)
    user_id = Column(Integer, ForeignKey('users.id'))  # Foreign key referencing the i


    # Relationship with the User table
    user = relationship("User", back_populates="doctor")
