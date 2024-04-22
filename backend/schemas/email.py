from typing import Optional, List
from pydantic import BaseModel, EmailStr
from datetime import datetime


class EmailRequest(BaseModel):
    to: EmailStr
    subject: str
    body: str


class EmailResponse(BaseModel):
    message: str
