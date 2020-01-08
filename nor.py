# -*- coding: utf-8 -*-
import numpy as np

#用于normalize数据的脚本

def normalize(X_all, X_test):
    X_train_test = np.concatenate((X_all, X_test))
    mu = (sum(X_train_test) / X_train_test.shape[0])
    sigma = np.std(X_train_test, axis=0)
    mu = np.tile(mu, (X_train_test.shape[0], 1))
    sigma = np.tile(sigma, (X_train_test.shape[0], 1))
    X_train_test_normed = (X_train_test - mu) / sigma
 
    X_all = X_train_test_normed[0:X_all.shape[0]]
    X_test = X_train_test_normed[X_all.shape[0]:]
    return X_all,X_test

def z_score(x, axis):
    
    x = np.array(x).astype(float)
    xr = np.rollaxis(x, axis=axis)
    xr -= np.mean(x, axis=axis)
    xr /= np.std(x, axis=axis)
    return x

def main():
    trainxx = []
    trainy = []
    with open('./data/pendigits/pendigits.tra','r') as f:
        data = f.readlines()
        for i in data:
            i = i.strip('\n')
            i = i.split(',')
            tmpx = []
            tmpy = []
            for j in i[:-1]:
                tmpx.append(float(j))
            trainxx.append(tmpx)
            trainy.append(float(i[-1]))

    testxx = []
    testy = []
    with open('./data/pendigits/pendigits.tes','r') as f:
        data = f.readlines()
        for i in data:
            i = i.strip('\n')
            i = i.split(',')
            tmpx = []
            tmpy = []
            for j in i[:-1]:
                tmpx.append(float(j))
            testxx.append(tmpx)
            testy.append(float(i[-1]))
    X_all,X_test = normalize(np.array(trainxx),np.array(testxx))
    # X_all = z_score(trainxx,1)
    # X_test = z_score(testxx,1)
    X_all =  list(X_all)
    X_test =  list(X_test)
    with open('pendigits.tra','w') as f:
        for i in range(len(X_all)):
            res = ''
            index = 0
            for j in X_all[i]:
                res += str(j) + ','
            res += str(trainy[i]) + '\n'
            f.write(res)
    with open('pendigits.tes','w') as f:
        for i in range(len(X_test)):
            res = ''
            index = 0
            for j in X_test[i]:
                res += str(j) + ','
            res += str(testy[i]) + '\n'
            f.write(res)
main()