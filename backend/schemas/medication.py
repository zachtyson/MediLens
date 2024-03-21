from pydantic import BaseModel
from datetime import datetime
from typing import Optional, List, Union

# Temporary Medication class
# Until Google Health API is implemented
class MedicationCreate(BaseModel):
    name: str
    color: Optional[str]
    imprint: Optional[str]
    shape: Optional[str]
    dosage: Optional[str]
    intake_method: Optional[str]
    description: Optional[str]
    schedule_start: Optional[datetime]
    interval_milliseconds: Optional[int]
