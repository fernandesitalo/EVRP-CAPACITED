package problems.ecvrp.solver;

import problems.ecvrp.ECVRP;
import problems.ecvrp.MoveWithCost;
import problems.ecvrp.Utils;
import solutions.RechargePoint;
import solutions.Route;
import solutions.Solution;
import tabusearch.AbstractTS;

import java.io.IOException;
import java.util.*;

import static problems.ecvrp.Utils.*;


public class TS_ECVRP extends AbstractTS<Route> {

    TS_EVRP_NEIGHBORHOOD neighborhood;

    public TS_ECVRP(Integer tenure, Integer iterations, String filename, Integer fleetSize) throws IOException {
        super(new ECVRP(filename, fleetSize), tenure, iterations, fleetSize);
        this.neighborhood = new TS_EVRP_NEIGHBORHOOD(this.ObjFunction, this.fleetSize);
    }

    @Override
    public ArrayDeque<List<Integer>> makeTL() {
        ArrayDeque<List<Integer>> _TS = new ArrayDeque<>();
        return _TS;
    }

    @Override
    public Solution<Route> neighborhoodMove() throws Exception {
        List<MoveWithCost> possibleMoves = new ArrayList<>();

        for (int i = 0; i < this.ObjFunction.getNumberClients(); ++i) {
            MoveWithCost moveA = neighborhood.insertAChargingStationInRandomRoute(this.sol);
            MoveWithCost moveB = neighborhood.removeChargingStationInRandomRoute(this.sol);
            MoveWithCost moveC = neighborhood.removeClientAndInsertInAnotherRoute(this.sol);
            MoveWithCost moveD = neighborhood.swapRandomNeighbor(this.sol);

            if (moveA != null) possibleMoves.add(moveA);
            if (moveB != null) possibleMoves.add(moveB);
            if (moveC != null) possibleMoves.add(moveC);
            if (moveD != null) possibleMoves.add(moveD);
        }

        possibleMoves.sort(Comparator.comparingDouble(MoveWithCost::getCost));
//        System.out.println("SolCost =  " + sol.cost);
//        for (MoveWithCost x : possibleMoves) {
//            System.out.print( x.cost + " # " + x.mov + ", ");
//        }
//        System.out.println();
        for (MoveWithCost p : possibleMoves) {
            List<Integer> mov = resolveMoveToVerify(p.mov);
            if (!this.TL.contains(p.mov) || p.cost < this.bestSol.cost) {
                neighborhood.applyMove(this.sol, p.mov);
                ObjFunction.evaluate(this.sol);
                TL.add(p.mov);
                if (TL.size() > this.tenure * 2) {
                    TL.pop();
                }
                break;
            }
        }
        return null;
    }

    private List<Integer> resolveMoveToVerify(List<Integer> mov) {
        int moveType = mov.get(0);
        if (moveType == MOVE_RELOCATE_CLIENT) {
            int routeIdx1 = mov.get(1);
            int routeIdx2 = mov.get(2);
            int client = mov.get(3);
            int idxFrom = mov.get(4);
            int idxToInsert = mov.get(5);

            return List.of(routeIdx2, routeIdx1, client, idxToInsert, idxFrom);
        }

        return mov;
    }

    @Override
    public Solution<Route> createARandomSolution() throws Exception {
        List<Integer> clients = ObjFunction.getClients();

        List<Integer> chargingStations = ObjFunction.getChargingStations();

        Collections.shuffle(clients);
        Collections.shuffle(chargingStations);

        this.sol = new Solution<Route>(this.fleetSize);

        for (int client : clients) {
            int randomRoutIdx = Utils.getRandomNumber(0, this.fleetSize);
            this.sol.addClient(randomRoutIdx, client);
        }

        for (int cs : chargingStations) {
            int randomRouteIdx = Utils.getRandomNumber(0, this.fleetSize);
            while (this.sol.getRoute(randomRouteIdx).getClients().isEmpty()) {
                randomRouteIdx = Utils.getRandomNumber(0, this.fleetSize);
            }
            int clientRouteSize = this.sol.getRoute(randomRouteIdx).chargingStations.size();
            int clientIndex = Utils.getRandomNumber(0, clientRouteSize);
            this.sol.addCS(randomRouteIdx, clientIndex, cs);
        }

        this.sol.cost = ObjFunction.evaluate(this.sol);

        return this.sol;
    }

    public int findClosestClient(int node, List<Integer> clients, double capacity, double time, double battery, List<Double> demands) {
        double dist = Double.MAX_VALUE;
        int clientIdx = -1;
        for (int i = 0 ; i <  clients.size(); i++) {
            int c = clients.get(i);
            double d = ObjFunction.calcDist(node, c);
            if (d > battery || demands.get(c) > capacity) {
                continue;
            }
            if (d  < dist) {
                dist = d;
                clientIdx = i;
            }
        }
        return clientIdx;
    }

    @Override
    public Solution<Route> createGreedSolution() throws Exception {
        List<Integer> clients = ObjFunction.getClients();
        List<Integer> chargingStations = ObjFunction.getChargingStations();
        Collections.shuffle(clients);
        Collections.shuffle(chargingStations);

        this.sol = new Solution<Route>(0);

        for (int i = 0; i < this.fleetSize; i++) {
            Route route = new Route();
            double capacity = this.ObjFunction.getLoadCapacity();
            double time = this.ObjFunction.getTimeAvailable();
            double battery = this.ObjFunction.getBatteryCapacity();
            List<Double> demands = this.ObjFunction.getDemands();
            int curNode = 0;

            while(true) {
                int closestIdx = findClosestClient(curNode, clients, capacity, time, battery, demands);
                if (closestIdx == -1) {
                    break;
                }
                int closestNode = clients.get(closestIdx);
                double dist = ObjFunction.calcDist(curNode, closestNode);

                battery -= dist;
                capacity -= demands.get(closestNode);

                route.addClient(closestNode);
                clients.remove(closestIdx);
                curNode = closestNode;
            }
            this.sol.routes.add(route);
        }

        int routeIdx = 0;
        for (int c : clients) {
            this.sol.getRoute(routeIdx).addClient(c);
            routeIdx = (routeIdx+1) % fleetSize;
        }

        ObjFunction.evaluate(this.sol);

        return this.sol;
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        int fleetSize = 13;
//        TS_ECVRP tabusearch = new TS_ECVRP(5, 10000, "instances/c101C5.txt", fleetSize);
        while(true) {
            TS_ECVRP tabusearch = new TS_ECVRP(10, 10000, "instances/c201_21.txt", fleetSize);
//        208,9

            verbose = true;
            Solution<Route> bestSol = tabusearch.solve();

            System.out.println("minVal = " + bestSol);
            System.out.println("valid = " + bestSol.isValid);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
        }
    }
}
