package problems.ecvrp.solver;

import problems.ecvrp.ECVRP;
import problems.ecvrp.MoveWithCost;
import problems.ecvrp.Utils;
import solutions.Route;
import solutions.Solution;
import tabusearch.AbstractTS;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static problems.ecvrp.Utils.*;


public class TS_ECVRP extends AbstractTS<Route> {

    TS_EVRP_NEIGHBORHOOD neighborhood;
    int fleetSize;

    public TS_ECVRP(Integer tenure, Integer iterations, ECVRP instance) throws IOException {
        super(instance, tenure, iterations);
        this.fleetSize = instance.fleetSize;
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
        List<String> instances = List.of(
                "instances/c101C5.txt",
                "instances/c101C10.txt",
                "instances/c101_21.txt",
                "instances/c102_21.txt",
                "instances/c103C15.txt",
                "instances/c103C5.txt",
                "instances/r101_21.txt",
                "instances/r102C10.txt",
                "instances/r102C15.txt",
                "instances/r102_21.txt",
                "instances/r103C10.txt",
                "instances/r103_21.txt",
                "instances/r104C5.txt"
        );

        List<Double> fleetSizeFactors = List.of(1.5);

        System.out.println("instance\tclients\tchargeStations\tbatteryCapacity\tloadCapacity\tfs\tusedEVs\ttime\tF\tavgF\tisValid");

        for (String instanceDir : instances) {
            for (double fleetSizeFactor : fleetSizeFactors) {
                ECVRP instance = new ECVRP(instanceDir, fleetSizeFactor);
                TS_ECVRP ts = new TS_ECVRP(10, 10000, instance);
                verbose = false;

                double minCost = Double.MAX_VALUE;
                double avgCost = 0;
                double avgTime = 0;
                boolean isValid = false;
                int usedEvs = 0;

                for (int i = 0; i < 10; i++) {
                    long startTime = System.currentTimeMillis();
                    Solution<Route> sol = ts.solve();
                    if (sol.cost < minCost) {
                        minCost = sol.cost;
                        isValid = sol.isValid;
                        usedEvs = 0;
                        for (Route r : sol.routes) {
                            if (r.clients.size() > 0) {
                                usedEvs++;
                            }
                        }
                    }
                    minCost = min(minCost, sol.cost);
                    avgCost += sol.cost;
                    avgTime += (double) (System.currentTimeMillis() - startTime) / (double) 1000;
                }

                avgCost /= 10;
                avgTime /= 10;

                //            System.out.print("instance clients chargeStations batteryCapacity loadCapacity fs usedEVs time F avgF isValid");
                System.out.printf(
                        "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                        instanceDir,
                        ts.ObjFunction.getNumberClients(),
                        ts.ObjFunction.getChargingStations().size(),
                        ts.ObjFunction.getBatteryCapacity(),
                        ts.ObjFunction.getLoadCapacity(),
                        (int) (ts.ObjFunction.getNumberClients() / fleetSizeFactor),
                        usedEvs,
                        avgTime,
                        minCost,
                        avgCost,
                        isValid
                );
            }
        }
    }
}
