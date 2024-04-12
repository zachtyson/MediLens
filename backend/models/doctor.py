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

    def __repr__(self):
        return f"<Doctor(doctor_id={self.doctor_id!r}, doctor_name={self.doctor_name!r}, " \
               f"specialty={self.specialty!r}, office_number={self.office_number!r}, " \
               f"emergency_number={self.emergency_number!r}, office_address={self.office_address!r}, " \
               f"email={self.email!r}, user_id={self.user_id!r})>"

    def __init__(self, doctor_id, doctor_name, specialty, office_number, emergency_number, office_address, email, user_id):
        self.doctor_id = doctor_id
        self.doctor_name = doctor_name
        self.specialty = specialty
        self.office_number = office_number
        self.emergency_number = emergency_number
        self.office_address = office_address
        self.email = email
        self.user_id = user_id