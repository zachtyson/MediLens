from pydantic import BaseModel
from datetime import datetime
from typing import Optional, List, Union

# Temporary Medication class
# Until Google Health API is implemented
class MedicationCreate(BaseModel):
    name: str
    color: Optional[str] = None
    imprint: Optional[str] = None
    shape: Optional[str] = None
    dosage: Optional[str] = None
    intake_method: Optional[str] = None
    description: Optional[str] = None
    schedule_start: Optional[datetime] = None
    # interval in milliseconds can be null
    interval_milliseconds: Optional[int] = None
    init_vector: Optional[str] = None
