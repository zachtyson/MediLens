from pydantic import BaseModel
from datetime import datetime
from typing import Optional, List, Union


class DrugInteractionBase(BaseModel):
    drug_a: str
    drug_b: str
    severity: str
    description: Optional[str]
    extended_description: Optional[str]
