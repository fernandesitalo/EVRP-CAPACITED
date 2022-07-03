from datetime import datetime
import pandas as pd
from math import ceil
import networkx as nx
import matplotlib.pyplot as plt
import gurobipy as gp
from gurobipy import *
from collections import namedtuple
import glob

from gurobi.graph import Graph
from gurobi.instance import Instance, read_instance
from gurobi.solution import plot_solution


def create_model(g, instance, vehicles_number, timelimit):
    lpModel = gp.Model()
    lpModel.setParam('OutputFlag', 0)
    lpModel.setParam('TimeLimit', timelimit)

    # Create variables
    isEdgeUsedVar = lpModel.addVars(g.w.keys(), vtype=GRB.BINARY, name='isEdgeUsedVar')
    arriveCargoLoadVar = lpModel.addVars(g.nodes, vtype=GRB.INTEGER, name='arriveCargoLoadVar')
    arriveTimeVar = lpModel.addVars(g.nodes, vtype=GRB.CONTINUOUS, name='arriveTimeVar')
    arriveBatteryVar = lpModel.addVars(g.nodes, vtype=GRB.CONTINUOUS, name='arriveBatteryVar')

    allNodesN = instance.chargeStationsNodes + instance.clientsNodes + [instance.depotNodeIn]
    allNodes0 = instance.chargeStationsNodes + instance.clientsNodes + [instance.depotNodeOut]

    for i in instance.clientsNodes:
        lpModel.addConstr(  # client visit limit constraint
            sum(isEdgeUsedVar[i, j] for j in allNodesN if i != j) == 1
        )

    for i in instance.chargeStationsNodes:
        lpModel.addConstr(  # Charge stations visit limit constraint
            sum(isEdgeUsedVar[i, j] for j in allNodesN if i != j) <= 1
        )

    # lpModel.addConstr(  # vehicles number constraint
    #     sum(isEdgeUsedVar[instance.depotNodeOut, j] for j in allNodes if i != j) <= vehicles_number
    # )

    for j in instance.clientsNodes + instance.chargeStationsNodes:
        lpModel.addConstr(  # flow preservation constraint
            sum(isEdgeUsedVar[j, i] for i in allNodesN if i != j) - sum(isEdgeUsedVar[i, j] for i in allNodes0 if i != j) == 0
        )

    # Cargo Load constraints
    for i in allNodes0:
        for j in allNodesN:
            if i == j:
                continue
            lpModel.addConstr(0 <= arriveCargoLoadVar[j])
            lpModel.addConstr(
                arriveCargoLoadVar[j]
                <= arriveCargoLoadVar[i] - instance.demands[i] * isEdgeUsedVar[i, j] + instance.loadCapacity * (1 - isEdgeUsedVar[i, j])
            )

    lpModel.addConstr(
        0 <= arriveCargoLoadVar[instance.depotNodeOut]
    )
    lpModel.addConstr(
        arriveCargoLoadVar[instance.depotNodeOut] <= instance.loadCapacity
    )

    # Battery constraints
    for i in instance.clientsNodes:
        for j in allNodesN:
            if i == j:
                continue
            lpModel.addConstr(
                0 <= arriveBatteryVar[j]
            )
            lpModel.addConstr(
                arriveBatteryVar[j]
                <= arriveBatteryVar[i]
                - instance.batteryConsumptionRate * g.w[i, j] * isEdgeUsedVar[i, j]
                + instance.batteryCapacity * (1 - isEdgeUsedVar[i, j])
            )

    for i in instance.chargeStationsNodes + [instance.depotNodeOut]:
        for j in allNodesN:
            if i == j:
                continue
            lpModel.addConstr(
                0 <= arriveBatteryVar[j]
            )
            lpModel.addConstr(
                arriveBatteryVar[j]
                <= instance.batteryCapacity - instance.batteryConsumptionRate * g.w[i, j] * isEdgeUsedVar[i, j]
            )

    # time constraints
    for i in instance.clientsNodes + [instance.depotNodeOut]:
        for j in allNodesN:
            if i == j:
                continue
            lpModel.addConstr (
                arriveTimeVar[i] + (instance.servicesTimes[i] + g.t[i, j])
                * isEdgeUsedVar[i, j] - instance.availableTime * (1 - isEdgeUsedVar[i, j])
                <= arriveTimeVar[j]
            )

    for i in instance.chargeStationsNodes:
        for j in allNodesN:
            if i == j:
                continue
            lpModel.addConstr(
                arriveTimeVar[i]
                + g.t[i, j] * isEdgeUsedVar[i, j]
                + instance.batteryChargeRate * (instance.batteryCapacity - arriveBatteryVar[i])
                - (instance.availableTime + instance.batteryCapacity * instance.batteryChargeRate) * (1 - isEdgeUsedVar[i, j])
                <= arriveTimeVar[j]
            )

    for j in allNodes0 + [instance.depotNodeIn]:
        lpModel.addConstr(
            arriveTimeVar[j] <= instance.availableTime
        )

        lpModel.addConstr(
            0 <= arriveTimeVar[j]
        )


    # Objective function
    lpModel.setObjective(
        sum(isEdgeUsedVar[i, j] * g.w[i, j] for i in allNodes0 for j in allNodesN if i != j)
    )
    lpModel.ModelSense = GRB.MINIMIZE
    lpModel._isEdgeUsedVar = isEdgeUsedVar
    lpModel._arriveBatteryVar = arriveBatteryVar
    lpModel._arriveTimeVar = arriveTimeVar

    return lpModel


def run_model(model, instance, time):
    start = datetime.now()
    model.optimize()

    if model.SolCount <= 0:
        return "-", "-", time
    model.Params.SolutionNumber = 0

    n = len(instance.nodesCoordinates)

    usedEdges = model.getAttr('X', model._isEdgeUsedVar)
    usedEVs = sum([1 for i in range(n) if usedEdges[instance.depotNodeOut, i] > 0.5])
    solCost = model.ObjVal
    totalTime = (datetime.now() - start).total_seconds()

    return round(solCost, 2), usedEVs, round(totalTime)


def get_instances_dir():
    dirs = glob.glob("instances/*")
    return [_ for _ in dirs if "readme" not in _]


if __name__ == "__main__":
    solutions = dict(
        instance=[],
        clients=[],
        chargeStations=[],
        batteryCapacity=[],
        loadCapacity=[],
        fs=[],
        usedEVs=[],
        time=[],
        f=[]
    )

    # for d in get_instances_dir():
    for d in ["instances/rc108C5.txt"]:
    # for d in ["instances/c101C5.txt"]:
        instance: Instance = read_instance(d)
        graph = Graph(instance)

        fs = ceil(len(instance.clientsNodes) / 2.0)

        timelimit = 60 * 60 * 5
        model = create_model(graph, instance, fs, timelimit)
        solCost, usedEvs, totalTime = run_model(model, instance, timelimit)

        plot_solution(instance, graph, model, usedEvs)

        solutions['instance'].append(d)
        solutions['batteryCapacity'].append(instance.batteryCapacity)
        solutions['loadCapacity'].append(instance.loadCapacity)
        solutions['usedEVs'].append(usedEvs)
        solutions['f'].append(str(solCost).replace('.', ','))
        solutions['time'].append(str(totalTime).replace('.', ','))
        solutions['fs'].append(fs)
        solutions['clients'].append(len(instance.clientsNodes))
        solutions['chargeStations'].append(len(instance.chargeStationsNodes))

        df_solutions = pd.DataFrame(solutions)
        df_solutions.to_csv('solutions.csv', sep='\t', index=False)
