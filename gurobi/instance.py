from gurobipy import *
from collections import namedtuple

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
    'depotNodeOut '
    'depotNodeIn '
)


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
    depotNodeOut = 0

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
            # for _ in range(1):
            #     nodeIdx += 1
            #     chargeStationNodes.append(nodeIdx)
            #     servicesTimes.append(float(serviceTime))
            #     demands.append(float(demand))
            #     coordinates.append((float(x), float(y)))
        elif nodeType == 'c':
            clientNodes.append(nodeIdx)

        nodeIdx += 1

    batteryCapacity: float = float(input().split('/')[1])
    loadCapacity = float(input().split('/')[1])
    batteryConsumptionRate = float(input().split('/')[1])
    batteryChargeRate = float(input().split('/')[1])
    velocity = float(input().split('/')[1])

    depotNodeIn = nodeIdx
    coordinates.append(coordinates[0])
    demands.append(0)
    servicesTimes.append(0)

    print(" >>>>>> ", depotNodeIn, depotNodeOut)

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
        depotNodeOut,
        depotNodeIn
    )
