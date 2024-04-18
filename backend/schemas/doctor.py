from pydantic import BaseModel
from datetime import datetime
from typing import Optional, List, Union

class Doctor(BaseModel):
    doctor_id: int
    doctor_name:  Optional[str]
    specialty:  Optional[str]
    office_number: Optional[str]
    emergency_number:  Optional[str]
    office_address:  Optional[str]
    email: Optional[str]
    user_id: int

# same as regular doctor except there's no doctor_id or user_id
class DoctorCreate(BaseModel):
    doctor_name:  Optional[str]
    specialty:  Optional[str]
    office_number: Optional[str]
    emergency_number:  Optional[str]
    office_address:  Optional[str]
    email: Optional[str]

class DoctorDelete(BaseModel):
    doctor_id: int
