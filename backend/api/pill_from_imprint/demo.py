import torch
from PIL import Image
import numpy as np
from db.session import SessionLocal
from fastapi import APIRouter
from fastapi import File, UploadFile
from torchvision.transforms import transforms
import torchvision.models as models
import pandas as pd
import torch.nn as nn
from ultralytics import YOLO
import easyocr

router = APIRouter()


# Proof of concept for the pill_from_imprint endpoint, doesn't use an official API and uses web scraping
# Will be replaced with a real API in the future
@router.get("/pill_from_imprint-demo")
async def pill_from_imprint(imprint: str, color: int, shape: int):
    return {"imprint": imprint, "color": color, "shape": shape}
