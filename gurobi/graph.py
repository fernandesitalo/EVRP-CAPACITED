from gurobipy import *


def calc_edge_cost(u, v, instance):
    x1, y1 = instance.nodesCoordinates[u][0], instance.nodesCoordinates[u][1]
    x2, y2 = instance.nodesCoordinates[v][0], instance.nodesCoordinates[v][1]
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


class Graph:
    def __init__(self, instance):
        self.t = None
        self.w = None
        self.n = len(instance.nodesCoordinates)
        self.create_adj_matrix(instance)
        self.nodes = [i for i in range(self.n)]

    def create_adj_matrix(self, instance):
        self.w = {(i, j): calc_edge_cost(i, j, instance) for i in range(self.n) for j in range(self.n)}
        self.t = {(i, j): self.w[i, j] / instance.velocity for i in range(self.n) for j in
                  range(self.n)}

