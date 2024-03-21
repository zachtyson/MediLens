from typing import Optional, List
from pydantic import BaseModel, EmailStr
from datetime import datetime


# Base structure for Image, used for creating a new image in the database that a user has uploaded Image has an id,
# date created, description, owner id, and binary data I don't think storing binary data in the database is a good idea,
# but this can always change later just need to get something working - Zach

class ImageBase(BaseModel):
    description: str
    time_created: datetime
    image_id: int
    owner_id: int
    binary_data: bytes
