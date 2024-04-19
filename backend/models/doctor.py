from sqlalchemy import Column, Integer, String, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from .base import Base
from .user import User


class Doctor(Base):
    __tablename__ = "doctor"
    doctor_id = Column(Integer, primary_key=True, index=True)
    doctor_name = Column(String(100), index=True)
    specialty = Column(String(100))
    office_number = Column(String(20))
    emergency_number = Column(String(20))
    office_address = Column(String(255))
    email = Column(String(255))
    owner_id = Column(Integer, ForeignKey("users.id"))
    owner = relationship(User)

    def __repr__(self):
        return f"<Doctor(doctor_id={self.doctor_id!r}, doctor_name={self.doctor_name!r}, " \
               f"specialty={self.specialty!r}, office_number={self.office_number!r}, " \
               f"emergency_number={self.emergency_number!r}, office_address={self.office_address!r}, " \
               f"email={self.email!r}, user_id={self.user_id!r})>"
