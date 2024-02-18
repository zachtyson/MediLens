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
import torchvision.models as models
import torch.nn as nn
from sklearn.model_selection import StratifiedShuffleSplit
from torch.utils.data import random_split
from pandas import json_normalize

from CustomDataset import CustomDataset


def main():
    transform = transforms.Compose([
        transforms.Resize((256, 256)),
        transforms.ToTensor(),
    ])

    # pillbox dataset, download on your own cause there's too many files
    dataset = CustomDataset(csv_file='small_set.csv', root_dir='pillbox_production_images_full_202008',
                            transform=transform)
    dataloader = DataLoader(dataset, batch_size=32, shuffle=True)

    # print when dataset is loaded
    # print('Dataset loaded')
    # print head of the dataset
    # pd.set_option('display.max_columns', None)
    # print(dataset.annotations.head())

    resnet18 = models.resnet18(pretrained=True)

    num_features = resnet18.fc.in_features

    num_shapes = len(dataset.annotations['splshape_text'].unique())

    # drop classes with only 1 member
    dataset.annotations = dataset.annotations[~dataset.annotations['splshape_text'].isin(
        dataset.annotations['splshape_text'].value_counts().where(lambda x: x == 1).dropna().index)]
    pass
    resnet18.fc = nn.Linear(num_features, num_shapes)

    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(resnet18.parameters(), lr=0.001)

    dataset_size = len(dataset)
    train_size = int(dataset_size * 0.8)
    val_size = dataset_size - train_size

    # stratified split based on splshape_text
    labels = dataset.annotations['splshape_text'].values
    sss = StratifiedShuffleSplit(n_splits=1, test_size=0.2, random_state=0)
    train_dataset, validation_dataset = random_split(dataset, [train_size, val_size])
    for train_index, test_index in sss.split(np.zeros(dataset_size), labels):
        train_dataset = torch.utils.data.Subset(dataset, train_index)
        validation_dataset = torch.utils.data.Subset(dataset, test_index)

    train_loader = DataLoader(train_dataset, batch_size=32, shuffle=True, num_workers=4)
    validation_loader = DataLoader(validation_dataset, batch_size=32, shuffle=False, num_workers=4)

    for epoch in range(10):
        resnet18.train()
        running_loss = 0.0
        for i, data in enumerate(train_loader, 0):
            inputs, labels = data['image'], data['splshape_text']
            optimizer.zero_grad()
            outputs = resnet18(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            running_loss += loss.item()
            if i % 100 == 99:
                print('[%d, %5d] loss: %.3f' % (epoch + 1, i + 1, running_loss / 100))
                running_loss = 0.0

    torch.save(resnet18, 'resnet18_splshape_text_10epochs.pth')


    # save model
    print('Finished Training')



if __name__ == '__main__':
    main()
