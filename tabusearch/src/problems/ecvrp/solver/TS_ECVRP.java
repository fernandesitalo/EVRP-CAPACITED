package problems.ecvrp.solver;

import problems.ecvrp.ECVRP;
import problems.ecvrp.Movement;
import problems.ecvrp.Pair;
import problems.ecvrp.Utils;
import solutions.RechargePoint;
import solutions.Route;
import solutions.Solution;
import tabusearch.AbstractTS;

import java.io.IOException;
import java.util.*;

import static java.lang.System.exit;

public class TS_ECVRP extends AbstractTS<Route> {

    TS_EVRP_NEIGHBORHOOD neighborhood;

    public TS_ECVRP(Integer tenure, Integer iterations, String filename, Integer fleetSize) throws IOException {
        super(new ECVRP(filename, fleetSize), tenure, iterations, fleetSize);
        this.neighborhood = new TS_EVRP_NEIGHBORHOOD(this.ObjFunction, this.fleetSize);
    }

    @Override
    public ArrayDeque<Movement> makeTL() {
        ArrayDeque<Movement> _TS = new ArrayDeque<>();
        return _TS;
    }

    @Override
    public void createInitialSolution() throws Exception {
        this.sol = createARandomSolution();
    }

    @Override
    public Solution<Route> neighborhoodMove() throws Exception {
        List<Pair> possibleMoves = new ArrayList<>();

        for (int i = 0; i < 1; ++i) {
            Pair moveA = neighborhood.insertAChargingStationInRandomRoute(this.sol);
            Pair moveB = neighborhood.removeChargingStationInRandomRoute(this.sol);

//            Pair moveC = neighborhood.removeClientAndInsertInAnotherRoute(this.sol);
            Pair moveD = neighborhood.swapRandomNeighbor(this.sol);

            if (moveA != null) possibleMoves.add(moveA);
            if (moveB != null) possibleMoves.add(moveB);
//            if (moveC != null) possibleMoves.add(moveC);
            if (moveD != null) possibleMoves.add(moveD);
        }

        possibleMoves.sort(Comparator.comparingDouble(Pair::getCost));

        for (Pair p : possibleMoves) {
            if (!this.TL.contains(p.mov) || p.cost <= this.bestCost) {
                neighborhood.applyMove(this.sol, p.mov);
                TL.add(p.mov);
                if (TL.size() > this.tenure * 2) {
                    TL.pop();
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Solution<Route> createARandomSolution() throws Exception {
        List<Integer> clients = ObjFunction.getClients();

        List<Integer> chargingStations = ObjFunction.getChargingStations();

        System.out.println("Clients: " + clients);
        System.out.println("CSs: " + chargingStations);

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
        int fleetSize = 2;
        TS_ECVRP tabusearch = new TS_ECVRP(20, 1000, "instances/c101C5.txt", fleetSize);

        // TO TEST INSERT CS
//        tabusearch.initialSolution();
//        tabusearch.sol.getRoute(0).setChargingStations(new ArrayList<>());
//        tabusearch.sol.getRoute(1).setChargingStations(new ArrayList<>());
//
//        tabusearch.printSolution_test();
//        Pair a = tabusearch.neighborhood.insertAChargingStationInRandomRoute(tabusearch.sol);
//        int routeIdx = a.mov.getIndexes().get(0);
//        int cdIdx = a.mov.getIndexes().get(1);
//        int posIdx = a.mov.getIndexes().get(2);
//        tabusearch.neighborhood.applyInsertCsMove(tabusearch.sol,routeIdx,cdIdx,posIdx);
//        tabusearch.printSolution_test();

        // TO TEST REMOVE CS
//        tabusearch.initialSolution();
//        tabusearch.printSolution_test();
//        Pair a = tabusearch.neighborhood.removeChargingStationInRandomRoute(tabusearch.sol);
//        int routeIdx = a.mov.getIndexes().get(0);
//        int indexToRemove = a.mov.getIndexes().get(1);
//        tabusearch.neighborhood.applyRemoveCsMove(tabusearch.sol,routeIdx,indexToRemove);
//        tabusearch.printSolution_test();

//        exit(0);

        Solution<Route> bestSol = tabusearch.solve();

        System.out.println("minVal = " + bestSol);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
    }
}
