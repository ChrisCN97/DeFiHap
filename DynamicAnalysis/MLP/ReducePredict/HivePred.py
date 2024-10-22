import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import torch
from torch import nn
from utils import train_model, pred,pred_train
from Model import get_model, hidden_dim1,hidden_dim2

train = pd.read_csv('./all/joinMlpTrainTrainData_L_notest.csv')
test = pd.read_csv('./all/joinMlpTrainTest_L.csv')

print('there are {} in train set'.format(train.shape[0]))
print('there are {} in test set'.format(test.shape[0]))

all_features = pd.concat((train.loc[:, 't1':'reduce'],
                          test.loc[:, 't1':'reduce']))
numeric_feats = all_features.dtypes[all_features.dtypes != "object"].index # get all numeric features

# normalize data by minus average and then divided by variance
all_features[numeric_feats] = all_features[numeric_feats].apply(lambda x: (x - x.mean())
                                                                / (x.std()))
train['time'] = np.log(train['time'])
all_features = pd.get_dummies(all_features, dummy_na=True)


all_features = all_features.fillna(all_features.mean())
feat_dim = all_features.shape[1]

num_train = int(0.9 * train.shape[0])  # split train and test set
indices = np.arange(train.shape[0])
np.random.shuffle(indices)  # shuffle data set
train_indices = indices[:num_train]
valid_indices = indices[num_train:]

# get features of train set and validation set
train_features = all_features.iloc[train_indices].values.astype(np.float32)
train_features = torch.from_numpy(train_features)
valid_features = all_features.iloc[valid_indices].values.astype(np.float32)
valid_features = torch.from_numpy(valid_features)
train_valid_features = all_features[:train.shape[0]].values.astype(np.float32)
train_valid_features = torch.from_numpy(train_valid_features)

# get labels of train set and validation set
train_labels = train['time'].values[train_indices, None].astype(np.float32)
train_labels = torch.from_numpy(train_labels)
valid_labels = train['time'].values[valid_indices, None].astype(np.float32)
valid_labels = torch.from_numpy(valid_labels)
train_valid_labels = train['time'].values[:, None].astype(np.float32)
train_valid_labels = torch.from_numpy(train_valid_labels)

test_features = all_features[train.shape[0]:].values.astype(np.float32)
test_features = torch.from_numpy(test_features)


net = get_model(feat_dim)
print(net)

# params
batch_size = 128
epochs = 200
lr = 0.01
wd = 0
use_gpu = True

train_model(net, train_features, train_labels, valid_features, valid_labels, epochs,
            batch_size, lr, wd, use_gpu)

train_model(net, train_valid_features, train_valid_labels, None, None, 1,
            batch_size, lr, wd, use_gpu)

torch.save(net, "./hive_pred_model_L_"+str(hidden_dim1)+"_"+str(hidden_dim2)+".pkl")
pred(net, test, test_features)

# net = torch.load("./hive_pred_model_L.pkl")
# pred_train(net,train,train_valid_features,"train_L")