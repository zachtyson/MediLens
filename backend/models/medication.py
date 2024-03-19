from sqlalchemy import Column, Integer, String, DateTime, func, ForeignKey, Table
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()
metadata = Base.metadata

# Temporary table for many-to-many relationship until
# Google Health API is implemented
class Medication(Base):
    __tablename__ = "medications"
    id = Column(Integer, primary_key=True, index=True)
    created_date = Column(DateTime, default=func.now(), nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"))
    owner = relationship("User", back_populates="medications")
    name = Column(String(100), index=True)
    description = Column(String(100), nullable=True)
    color = Column(String(100), nullable=True)
    imprint = Column(String(100), nullable=True)
    shape = Column(String(100), nullable=True)
    dosage = Column(String(100), nullable=True)
    intake_method = Column(String(100), nullable=True)
    schedule_start = Column(DateTime, nullable=True)
    interval_milliseconds = Column(Integer, nullable=True)
