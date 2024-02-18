import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import os
from PIL import Image
import torch
from torch.utils.data import Dataset, DataLoader, Subset
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
    dataset_size = len(dataset)
    labels = dataset.annotations['splshape_text'].values

    train_dataset = None
    validation_dataset = None

    # Stratified split based on 'splshape_text'
    sss = StratifiedShuffleSplit(n_splits=1, test_size=0.2, random_state=0)
    for train_index, test_index in sss.split(np.zeros(dataset_size), labels):
        train_dataset = Subset(dataset, train_index)
        validation_dataset = Subset(dataset, test_index)

    # DataLoader setup
    train_loader = DataLoader(train_dataset, batch_size=32, shuffle=True, num_workers=4)
    validation_loader = DataLoader(validation_dataset, batch_size=32, shuffle=False, num_workers=4)

    # Model, loss, and optimizer setup (assuming 'resnet18' and 'num_features' are predefined)
    resnet18.fc = nn.Linear(num_features, num_shapes)
    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(resnet18.parameters(), lr=0.001)
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
                print(f'Epoch: {epoch + 1}, Batch: {i + 1}, Loss: {running_loss / 100}')
                running_loss = 0.0

    # validation
    correct = 0
    total = 0
    with torch.no_grad():
        for data in validation_loader:
            images, labels = data['image'], data['splshape_text']
            outputs = resnet18(images)
            _, predicted = torch.max(outputs.data, 1)
            total += labels.size(0)
            correct += (predicted == labels).sum().item()

    print(f'Accuracy: {100 * correct / total}%')

    torch.save(resnet18, 'resnet18_splshape_text_10epochs.pth')


    # save model
    print('Finished Training')



if __name__ == '__main__':
    main()
