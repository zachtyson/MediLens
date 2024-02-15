import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import os
from PIL import Image
import torch
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
import pandas as pd

from pandas import json_normalize

from CustomDataset import CustomDataset


def main():
    transform = transforms.Compose([
        transforms.Resize((256, 256)),
        transforms.ToTensor(),
    ])

    dataset = CustomDataset(csv_file='way_clean.csv', root_dir='pillbox_production_images_full_202008',
                            transform=transform)
    dataloader = DataLoader(dataset, batch_size=32, shuffle=True)

    # print when dataset is loaded
    print('Dataset loaded')
    # print head of the dataset
    pd.set_option('display.max_columns', None)
    print(dataset.annotations.head())


if __name__ == '__main__':
    main()
