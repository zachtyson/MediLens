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


class MultiTaskResNet(nn.Module):
    def __init__(self, base_model, num_features, num_colors, num_shapes):
        super(MultiTaskResNet, self).__init__()
        self.base_model = base_model
        self.base_model.fc = nn.Identity()  # Remove the original fully connected layer
        self.color_classifier = nn.Linear(num_features, num_colors)
        self.shape_classifier = nn.Linear(num_features, num_shapes)

    def forward(self, x):
        features = self.base_model(x)
        color_outputs = self.color_classifier(features)
        shape_outputs = self.shape_classifier(features)
        return color_outputs, shape_outputs

        # Load the complete model (recommended)


model = MultiTaskResNet(models.resnet18(pretrained=True), 512, 105, 16)
model.load_state_dict(torch.load('ml/multi_task_resnet18.pth'))
model.eval()  # Set the model to evaluation mode
# try cuda
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

shape_labels = pd.read_csv('ml/splshape_text_encoding.csv')
shape_labels = {row[1]: row[0] for row in shape_labels.values}

color_labels = pd.read_csv('ml/splcolor_text_encoding.csv')
color_labels = {row[1]: row[0] for row in color_labels.values}

transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
])
reader = easyocr.Reader(['en'])

color_shape_model = YOLO("ml/best.pt")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.post("/predict")
async def predict_image(file: UploadFile = File(...)):
    # save the image
    with open("img.jpg", "wb") as buffer:
        buffer.write(await file.read())

    # predict the image, minimum confidence is 0.25
    results = color_shape_model("img.jpg", conf=0.25)

    boxes = results[0].boxes.xyxy.tolist()
    classes = results[0].boxes.cls.tolist()
    names = results[0].names
    confidences = results[0].boxes.conf.tolist()
    json_objects = []
    index = 0
    for box, cls, conf in zip(boxes, classes, confidences):
        x1, y1, x2, y2 = box
        confidence = conf
        detected_class = cls

        # name = names[int(cls)]
        # this really doesn't matter since we are only identifying one class (pills) so

        # crop the image to the bounding box
        img = Image.open("img.jpg")
        img = img.crop((x1, y1, x2, y2))
        # convert img to numpy array
        img_array = np.array(img)

        text = reader.readtext(img_array)
        print(text)
        # jsonify the text if it exists
        if len(text) > 0:
            # empty array of strings
            temp = []
            for i in range(len(text)):
                # convert 2d array into object
                temp.append(str(text[i][0]))
            text = [[temp[i], text[i][1], text[i][2]] for i in range(len(text))]

        # Preprocess the image

        transform = transforms.Compose([
            transforms.Resize((256, 256)),
            transforms.ToTensor(),
        ])

        img = transform(img).unsqueeze(0)  # Add batch dimension
        # labels are in splcolor_text_encoding.csv and splshape_text_encoding.csv
        # first column is the label, second column is the encoding

        # Predict the label
        with torch.no_grad():
            m = model(img)
            color_output, shape_output = m
            color_prediction = color_labels[color_output.argmax(1).item()]
            shape_prediction = shape_labels[shape_output.argmax(1).item()]

        obj = {
            "x1": x1,
            "y1": y1,
            "x2": x2,
            "y2": y2,
            "confidence": confidence,
            "class": detected_class,
            "name": "pill",
            "color": color_prediction,
            "shape": shape_prediction,
            "ocr": text
        }
        json_objects.append(obj)
        index += 1
    return {
        "predictions": json_objects
    }
