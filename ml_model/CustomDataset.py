import os
from PIL import Image
import torch
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
import pandas as pd


class CustomDataset(Dataset):
    def __init__(self, csv_file, root_dir, transform=None):
        self.annotations = pd.read_csv(csv_file)
        self.root_dir = root_dir
        self.transform = transform
        self.label_encodings = self.preprocess_labels(self.annotations)

    def preprocess_labels(self, annotations):
        label_encodings = {}
        for column in ['splshape_text', 'splimprint', 'splcolor_text']:
            unique_values = annotations[column].unique()
            label_encodings[column] = {value: idx for idx, value in enumerate(unique_values)}
        return label_encodings

    def __len__(self):
        return len(self.annotations)

    def __getitem__(self, index):
        img_path = os.path.join(self.root_dir, self.annotations.iloc[index, 0]) + '.jpg'
        image = Image.open(img_path).convert('RGB')
        # 3 labels: splshape_text, splimprint, splcolor_text
        splshape_text = self.label_encodings['splshape_text'][self.annotations.iloc[index, 1]]
        splimprint = self.label_encodings['splimprint'][self.annotations.iloc[index, 2]]
        splcolor_text = self.label_encodings['splcolor_text'][self.annotations.iloc[index, 3]]

        if self.transform:
            image = self.transform(image)

        sample = {'image': image, 'splshape_text': splshape_text, 'splimprint': splimprint,
                  'splcolor_text': splcolor_text}

        return sample
