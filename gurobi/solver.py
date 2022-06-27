from json import load
import sys
import gurobipy as gp
from gurobipy import *
import numpy as np
from collections import namedtuple
import glob

Instance = namedtuple(
    'Instance',

    'batteryCapacity '
    'loadCapacity '
    'batteryConsumptionRate '
    'batteryChargeRate '
    'velocity '
    'availableTime '
    'demands '
    'servicesTimes '
    'nodesCoordinates '
    'clientsNodes '
    'chargeStationsNodes '
    'depotNode '
)


def calc_edge_cost(u, v, instance):
    x1, y1 = instance.nodesCoordinates[u][0], instance.nodesCoordinates[u][1]
    x2, y2 = instance.nodesCoordinates[v][0], instance.nodesCoordinates[v][1]
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


class Graph:
    def __init__(self, instance):
        self.timeEdges = None
        self.costEdges = None
        self.n = len(instance.nodesCoordinates)
        self.create_adj_matrix(instance)
        self.nodes = [i for i in range(self.n)]

    def create_adj_matrix(self, instance):
        self.costEdges = {(i, j): calc_edge_cost(i, j, instance) for i in range(self.n) for j in range(self.n)}
        self.timeEdges = {(i, j): self.costEdges[i, j] / instance.velocity for i in range(self.n) for j in range(self.n)}


def read_instance(file_dir):
    sys.stdin = open(file_dir, "r")
    _ = input().split()

    coordinates = list()
    demands = list()
    servicesTimes = list()
    chargeStationNodes = list()
    clientNodes = list()
    availableTime = 0
    nodeIdx = 0
    depotNode = 0

    while True:
        line = input().split()
        if len(line) == 0:
            break

        nodeStringId, nodeType, x, y, demand, readyDate, dueDate, serviceTime = line

        availableTime = max(availableTime, float(dueDate))
        coordinates.append((float(x), float(y)))
        demands.append(float(demand))
        servicesTimes.append(float(serviceTime))

        if nodeType == 'f':
            chargeStationNodes.append(nodeIdx)
        elif nodeType == 'c':
            clientNodes.append(nodeIdx)

        nodeIdx += 1

    batteryCapacity: float = float(input().split('/')[1])
    loadCapacity = float(input().split('/')[1])
    batteryConsumptionRate = float(input().split('/')[1])
    batteryChargeRate = float(input().split('/')[1])
    velocity = float(input().split('/')[1])

    return Instance(
        batteryCapacity,
        loadCapacity,
        batteryConsumptionRate,
        batteryChargeRate,
        velocity,
        availableTime,
        demands,
        servicesTimes,
        coordinates,
        clientNodes,
        chargeStationNodes,
        depotNode
    )


def create_model(instance, vehicles_number):
    graph = Graph(instance)
    lpModel = gp.Model()
    lpModel.setParam('OutputFlag', 1)

    # Create variables
    isEdgeUsedVar = lpModel.addVars(graph.costEdges.keys(), vtype=GRB.BINARY, name='isEdgeUsedVar')
    arriveCargoLoadVar = lpModel.addVars(graph.nodes, vtype=GRB.INTEGER, name='arriveCargoLoadVar')
    arriveTimeVar = lpModel.addVars(graph.nodes, vtype=GRB.INTEGER, name='arriveTimeVar')
    arriveBatteryVar = lpModel.addVars(graph.nodes, vtype=GRB.INTEGER, name='arriveBatteryVar')
    # todo, try to set the variables domains do float numbers instead of integer

    allNodes = graph.nodes

    for i in instance.clientsNodes:
        lpModel.addConstr(  # client visit limit constraint
            sum(isEdgeUsedVar[i, j] for j in allNodes) == 1
        )

    csAndDepotNodes = instance.chargeStationsNodes + [instance.depotNode]
    for i in csAndDepotNodes:
        lpModel.addConstr(  # Charge stations visit limit constraint
            sum(isEdgeUsedVar[i, j] for j in allNodes) <= 1
        )

    lpModel.addConstr(  # vehicles number constraint
        sum(isEdgeUsedVar[instance.depotNode, j] for j in allNodes) <= vehicles_number
    )

    for j in allNodes:
        lpModel.addConstr(  # flow preservation constraint
            sum(isEdgeUsedVar[j, i] for i in allNodes) - sum(isEdgeUsedVar[i, j] for i in allNodes) == 0
        )

    # Cargo Load constraints
    for i in allNodes:
        for j in allNodes:
            lpModel.addConstr(0 <= arriveCargoLoadVar[j])
            lpModel.addConstr(arriveCargoLoadVar[j] <= instance.demands[i] * graph.costEdges[i, j] * instance.loadCapacity * (1 - isEdgeUsedVar[i, j]))

    lpModel.addConstr(
        0 <= arriveCargoLoadVar[instance.depotNode]
    )
    lpModel.addConstr(
        arriveCargoLoadVar[instance.depotNode] <= instance.loadCapacity
    )

    # Battery constraints
    for i in instance.clientsNodes:
        for j in allNodes:
            lpModel.addConstr(
                arriveBatteryVar[j] <= arriveBatteryVar[i] - instance.batteryConsumptionRate * graph.costEdges[i, j] * isEdgeUsedVar[i, j] + instance.batteryCapacity * (1 - isEdgeUsedVar[i, j])
            )

    for i in instance.chargeStationsNodes:
        for j in allNodes:
            lpModel.addConstr(
                arriveBatteryVar[j] <= instance.batteryCapacity - instance.batteryConsumptionRate * graph.costEdges[i, j] * isEdgeUsedVar[i, j]
            )

    # time constraints
    for i in allNodes:
        for j in allNodes:
            if j != instance.depotNode:
                lpModel.addConstr(
                    arriveTimeVar[i] + instance.servicesTimes[i] + graph.timeEdges[i, j] - instance.availableTime * (1 - isEdgeUsedVar[i, j]) <= arriveTimeVar[j]
                )

    for j in allNodes:
        if j == instance.depotNode:
            continue
        lpModel.addConstr(
            instance.servicesTimes[instance.depotNode] + graph.timeEdges[instance.depotNode, j] <= arriveTimeVar[j]
        )

        lpModel.addConstr(
            arriveTimeVar[j] <= instance.availableTime - instance.servicesTimes[instance.depotNode] + graph.timeEdges[j, instance.depotNode]
        )

    # vars limit constraints

    # Objective function
    lpModel.setObjective(
        sum(isEdgeUsedVar[i, j] * graph.costEdges[i, j] for i in allNodes for j in allNodes)
    )
    lpModel.ModelSense = GRB.MINIMIZE

    return lpModel


def get_instances_dir():
    dirs = glob.glob("instances/*")
    return [d for d in dirs if "readme" not in d]


if __name__ == "__main__":
    for d in get_instances_dir():
        instance: Instance = read_instance(d)
        maxVehiclesNumbers = instance.clientsNodes
        model = create_model(instance, 5)
        model.optimize()
        exit(0)
