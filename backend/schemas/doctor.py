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
    email: str
    user_id: int
