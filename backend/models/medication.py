from sqlalchemy import Column, Integer, String, DateTime, func, ForeignKey, Table
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base
from .user import User
from .base import Base


# Temporary table for many-to-many relationship until
# Google Health API is implemented
class Medication(Base):
    __tablename__ = "medications"
    id = Column(Integer, primary_key=True, index=True)
    created_date = Column(DateTime, default=func.now(), nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"))
    owner = relationship(User)
    name = Column(String(2000), index=True)
    description = Column(String(2000), nullable=True)
    color = Column(String(2000), nullable=True)
    imprint = Column(String(2000), nullable=True)
    shape = Column(String(2000), nullable=True)
    dosage = Column(String(2000), nullable=True)
    intake_method = Column(String(2000), nullable=True)
    schedule_start = Column(DateTime, nullable=True)
    interval_milliseconds = Column(Integer, nullable=True)
    init_vector = Column(String(2000), nullable=False)
