import networkx as nx
import matplotlib.pyplot as plt


def format_double(x):
    return "{:.2f}".format(x)


def plot_solution(instance, graph, model, usedEvs):
    if model.SolCount <= 0:
        return

    model.Params.SolutionNumber = 0
    n = len(instance.nodesCoordinates)

    usedEdgesVar = model.getAttr('X', model._isEdgeUsedVar)
    arriveBatteryVar = model.getAttr('X', model._arriveBatteryVar)
    arriveTimeVar = model.getAttr('X', model._arriveTimeVar)

    allNodes = [i for i in range(n)]
    usedEdges = [(i, j) for i in range(n) for j in range(n) if usedEdgesVar[i, j] > 0.5]
    usedNodes = set()
    G = nx.Graph()

    for i, j in usedEdges:
        G.add_edge(i, j, weight="{:.2f}".format(graph.w[i, j]))
        usedNodes.add(i)
        usedNodes.add(j)

    G.add_nodes_from(allNodes)
    unusedNodes = set(allNodes) - usedNodes
    for node in unusedNodes:
        assert node in instance.chargeStationsNodes

    colors = []
    nodeLabels = {}
    for node in G.nodes:
        if node == instance.depotNodeIn or node == instance.depotNodeOut:
            colors.append('blue')
        elif node in instance.chargeStationsNodes:
            colors.append('red')
        else:
            colors.append("green")

        batteryCharge = arriveBatteryVar[node]
        time = arriveTimeVar[node]
        nodeLabels[node] = (
        "node = " + str(node), "b = " + format_double(batteryCharge), "t = " + format_double(time), instance.nodesCoordinates[node])

    pos = nx.spring_layout(G, seed=6, k=4)
    nx.draw_networkx_nodes(G, pos, node_size=100, node_color=colors)
    nx.draw_networkx_edges(G, pos, edgelist=usedEdges, width=1)
    nx.draw_networkx_labels(G, pos, font_size=6, font_family="sans-serif", labels=nodeLabels)
    edge_labels = nx.get_edge_attributes(G, "weight")
    nx.draw_networkx_edge_labels(G, pos, edge_labels)
    plt.title("MaxTime = " + str(instance.availableTime) + ", Battery = " + str(instance.batteryCapacity) + " UsedEvs = " + str(usedEvs))

    # print([x for x in nodeLabels.values()])
    # plt.legend([x for x in nodeLabels.values()])
    # plt.legend()
    ax = plt.gca()
    ax.margins(0.08)
    plt.axis("on")
    plt.tight_layout()
    plt.show()
