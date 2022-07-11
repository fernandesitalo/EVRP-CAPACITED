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
    public void createInitialSolution() throws Exception {
        this.sol = createARandomSolution();
    }

    @Override
    public Solution<Route> neighborhoodMove() throws Exception {
        List<MoveWithCost> possibleMoves = new ArrayList<>();

        for (int i = 0; i < 30; ++i) {
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

        for (MoveWithCost p : possibleMoves) {
            if (!this.TL.contains(p.mov) || p.cost < this.bestSol.cost) {
                neighborhood.applyMove(this.sol, p.mov);
                ObjFunction.evaluate(this.sol);
                TL.add(p.mov);
                if (TL.size() > this.tenure * 2) {
                    TL.pop();
                }
                break;
            } else {
                System.out.println(TL);
                System.out.println(" >>> " + p.mov);
            }
        }
        return null;
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
        System.out.println(ObjFunction.getClients());

        for (int i = 0; i < this.fleetSize; i++) {
            Route route = new Route();
            double capacity = this.ObjFunction.getBatteryCapacity();
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
            System.out.println(route.clients);
            this.sol.routes.add(route);
        }

        int routeIdx = 0;
        for (int c : clients) {
            this.sol.getRoute(routeIdx).addClient(c);
            routeIdx++;
        }

        ObjFunction.evaluate(this.sol);

        return this.sol;
    }

    public void printSolution_test(){
        int idx = 0;
        System.out.println("Total Cost: " + this.sol.cost);
        for(Route route: this.sol.routes) {
            System.out.println("Route: " + idx + " Cost:" + route.cost);
            System.out.println("Clients: " + route.clients);

            System.out.print("CS: ");
            for(RechargePoint cs: route.chargingStations) {
                System.out.print("(" + cs.chargingStation + "," + cs.index +")");
            }
            System.out.println("\n");
        }
        System.out.println("\n\n");
    }

    // just to tests
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        int fleetSize = 3;
        TS_ECVRP tabusearch = new TS_ECVRP(5, 10000, "instances/c101C5.txt", fleetSize);
//        208,9

        verbose = true;
        Solution<Route> bestSol = tabusearch.solve();

        System.out.println("minVal = " + bestSol);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
    }
}
