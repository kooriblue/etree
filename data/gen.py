from cyaron import *
import random
from sys import argv
import numpy as np

test_data = IO(file_prefix="./data/data", data_id="")

n = int(argv[1])
delayMean = int(argv[2])
delayVar = int(argv[3])

m = random.randint(n, n*(n-1)//2)

graph = Graph.graph(n, m, self_loop=False, repeated_edges=False, directed=True)

for edge in graph.iterate_edges():
    edge.start # 获取这条边的起点
    edge.end # 获取这条边的终点
    weight = delayMean + np.random.randn()*delayVar
    edge.weight = 1 if weight < 0 else weight # 获取这条边的边权
    test_data.input_writeln(edge) # 输出这条边，以u v w的形式

# test_data.input_writeln(graph)