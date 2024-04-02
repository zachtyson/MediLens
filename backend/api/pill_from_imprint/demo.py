import requests
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
from bs4 import BeautifulSoup
from fastapi import APIRouter, Depends, HTTPException

router = APIRouter()


# Proof of concept for the pill_from_imprint endpoint,
# uses drugs.com endpoint which for some reason returns a html page
# will be replaced later with a more reliable endpoint

@router.get("/pill_from_imprint-demo")
async def pill_from_imprint(imprint: str, color: int, shape: int):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.5",
        "Sec-GPC": "1",
        "Upgrade-Insecure-Requests": "1",
    }
    url = f"https://www.drugs.com/imprints.php?imprint={imprint}&color={color}&shape={shape}"
    response = requests.get(url, headers=headers)
    if response.status_code != 200:
        # return code 500 if the request fails
        raise HTTPException(status_code=500, detail="Request failed")

    res = response.text

    soup = BeautifulSoup(res, 'html.parser')

    # Find all divs with the specific class
    div_content = soup.find_all('div', class_='ddc-card')

    # Extract and print the desired information
    extracted_info = extract_info(div_content)
    return extracted_info


def extract_info(div_content):
    extracted_info = []
    if div_content:
        for div in div_content:
            # Get URL: data-image-src
            img_url = div.find('div')['data-image-src']
            obj = {}
            if img_url == '/img/pillid/no-image-placeholder.png':
                img_url = None
            obj['imageURL'] = img_url

            div_smaller = div.find_all('div', class_='ddc-card-content ddc-card-content-pid')
            for d in div_smaller:
                dt_dd_pairs = d.find_all(['dt', 'dd'])
                # extract the content of the <a> tag
                obj['pillName'] = d.find('a').get_text().strip()
                for i in range(0, len(dt_dd_pairs), 2):
                    key = dt_dd_pairs[i].get_text().strip().lower()
                    value = dt_dd_pairs[i + 1].get_text().strip()
                    obj[key] = value
                extracted_info.append(obj)
    return extracted_info
