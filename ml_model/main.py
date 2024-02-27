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

    num_colors = len(dataset.annotations['splcolor_text'].unique())
    num_shapes = len(dataset.annotations['splshape_text'].unique())

    # drop classes with only 1 member
    dataset.annotations = dataset.annotations[~dataset.annotations['splshape_text'].isin(
        dataset.annotations['splshape_text'].value_counts().where(lambda x: x == 1).dropna().index)]
    dataset.annotations = dataset.annotations[~dataset.annotations['splcolor_text'].isin(
        dataset.annotations['splcolor_text'].value_counts().where(lambda x: x == 1).dropna().index)]

    dataset_size = len(dataset)
    labels_color = dataset.annotations['splcolor_text'].values
    # print labels_color
    print(labels_color)
    labels_shape = dataset.annotations['splshape_text'].values
    # print labels_shape
    print(labels_shape)

    # get encoding for labels
    label_encodings = {}
    for column in ['splshape_text', 'splcolor_text']:
        unique_values = dataset.annotations[column].unique()
        label_encodings[column] = {value: idx for idx, value in enumerate(unique_values)}

    # save to csv
    for column in ['splshape_text', 'splcolor_text']:
        unique_values = dataset.annotations[column].unique()
        label_encodings[column] = {value: idx for idx, value in enumerate(unique_values)}
        pd.DataFrame.from_dict(label_encodings[column], orient='index').to_csv(f'{column}_encoding.csv', header=False)

    # save csv files

    sss_color = StratifiedShuffleSplit(n_splits=1, test_size=0.2, random_state=0)
    sss_shape = StratifiedShuffleSplit(n_splits=1, test_size=0.2,
                                       random_state=42)  # Different random_state for diversity

    train_idx_color, test_idx_color = next(sss_color.split(np.zeros(dataset_size), labels_color))
    train_idx_shape, test_idx_shape = next(sss_shape.split(np.zeros(dataset_size), labels_shape))

    # Assuming the dataset can handle a tuple (train_idx, test_idx) to fetch the correct labels
    train_dataset = Subset(dataset, train_idx_color)
    validation_dataset = Subset(dataset, test_idx_color)

    train_loader = DataLoader(train_dataset, batch_size=32, shuffle=True, num_workers=4)
    validation_loader = DataLoader(validation_dataset, batch_size=32, shuffle=False, num_workers=4)

    resnet18 = models.resnet18(pretrained=True)
    num_features = resnet18.fc.in_features

    print("num_features: ", num_features)
    print("num_colors: ", num_colors)
    print("num_shapes: ", num_shapes)

    multi_task_model = MultiTaskResNet(resnet18, num_features, num_colors, num_shapes)

    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(multi_task_model.parameters(), lr=0.001)

    for epoch in range(10):
        multi_task_model.train()
        running_loss = 0.0
        for i, data in enumerate(train_loader, 0):
            inputs, labels_color, labels_shape = data['image'], data['splcolor_text'], data['splshape_text']
            optimizer.zero_grad()
            color_outputs, shape_outputs = multi_task_model(inputs)
            loss_color = criterion(color_outputs, labels_color)
            loss_shape = criterion(shape_outputs, labels_shape)
            loss = loss_color + loss_shape
            loss.backward()
            optimizer.step()
            running_loss += loss.item()
            if i % 100 == 99:
                print(f'Epoch: {epoch + 1}, Batch: {i + 1}, Loss Shape: '
                      f'{loss_shape.item()}, Loss Color: {loss_color.item()}')
                running_loss = 0.0

    # Validation code here
    correct_color = 0
    correct_shape = 0
    correct = 0
    total = 0
    with torch.no_grad():
        for data in validation_loader:
            inputs, labels_color, labels_shape = data['image'], data['splcolor_text'], data['splshape_text']
            color_outputs, shape_outputs = multi_task_model(inputs)
            _, predicted_color = torch.max(color_outputs, 1)
            _, predicted_shape = torch.max(shape_outputs, 1)
            correct_color += (predicted_color == labels_color).sum().item()
            correct_shape += (predicted_shape == labels_shape).sum().item()
            total += labels_color.size(0)
            if predicted_color == labels_color and predicted_shape == labels_shape:
                correct += 1

    print(f'Accuracy of the network on the validation images: {100 * correct / total}%')
    print(f'Accuracy of the network on the color validation images: {100 * correct_color / total}%')
    print(f'Accuracy of the network on the shape validation images: {100 * correct_shape / total}%')

    # Save model
    torch.save(multi_task_model.state_dict(), 'multi_task_resnet18.pth')
    torch.save(multi_task_model, 'multi_task_resnet18_full.pth')
    print('Finished Training')


if __name__ == '__main__':
    main()
