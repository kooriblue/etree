# -*- coding: utf-8 -*-
import random

# 将数据随机划分数据集和测试集
with open('./spambase.data','r') as f:
    data = f.readlines()
    d = []
    for i in data:
        d.append(i)
    test = random.sample(d,461)
    for i in test:
        d.remove(i)
    
    with open('./spambase.tra','w') as f1:
        #是否打乱顺序，不打乱就是每个节点分到一个单类的数据
        #random.shuffle(d)
        for i in d:
            f1.write(i)
    with open('./spambase.tes','w') as f2:
        #random.shuffle(test)
        for i in test:
            f2.write(i)