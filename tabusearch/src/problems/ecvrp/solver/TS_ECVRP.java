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
import java.util.stream.Collectors;

public class TS_ECVRP extends AbstractTS<Route> {

    int fleetSize;

    public TS_ECVRP(Integer tenure, Integer iterations, String filename, int fleetSize) throws IOException {
        super(new ECVRP(filename), tenure, iterations);
        this.fleetSize = fleetSize;
    }

    @Override
    public ArrayDeque<Movement> makeTL() {
        ArrayDeque<Movement> _TS = new ArrayDeque<>();
        return _TS;
    }

    @Override
    public Solution<Route> createEmptySol() {
        Solution<Route> sol = new Solution<Route>();
        sol.cost = Double.MAX_VALUE;
        return sol;
    }

    @Override
    public void createInitialSolution() {
        Solution<Route> sol = new Solution<Route>();
        sol.cost = Double.MAX_VALUE;

        for (int i = 0; i < this.fleetSize; i++) {
            sol.add(new Route(new ArrayList<>(), new ArrayList<>(),0,0,0.0));
        }

        this.sol =  sol;
    }


    @Override
    public Solution<Route> neighborhoodMove() {
        List<Pair> possibleMoves = new ArrayList<>();

        for (int i = 0; i < 20; ++i) {
            Pair moveA = insertAChargingStationInRandomRoute();
            Pair moveB = removeChargingStationInRandomRoute();
            Pair moveC = removeClientAndInsertInAnotherRoute();
            Pair moveD = moveRandom2OptClients();

            if (moveA != null) {
                possibleMoves.add(moveA);
            }
            if (moveB != null) {
                possibleMoves.add(moveB);
            }
            if (moveC != null) {
                possibleMoves.add(moveC);
            }
            if (moveD != null) {
                possibleMoves.add(moveD);
            }
        }

        possibleMoves.sort((pair1, pair2) -> {
            if (pair1.cost == pair2.cost) return 0;
            if (pair1.cost > pair2.cost) return 1;
            return -1;
        });

        for (Pair p : possibleMoves) {
            if (!this.TL.contains(p.mov) || p.cost <= this.bestCost) {
                applyMove(this.sol, p.mov);
                TL.add(p.mov);
                if (TL.size() > this.tenure * 2) {
                    TL.pop();
                }
                break;
            }
        }
        return null;
    }

    protected void applyMove(Solution<Route> sol, Movement mov) {
        if (mov.type == Utils.MOVE_CLIENT_NEIGHBORHOOD) {
            // TODO: this case
            return;
        }
        if (mov.type == Utils.MOVE_INSERT_CS) {
            // TODO: this case
            return;
        }
        // Utils.MOVE_REMOVE_CS
        // TODO: this case
    }

    protected Pair removeClientAndInsertInAnotherRoute() {
        // TODO: implementar essa porra aqui: funcao para remover um client de uma route e adcionar em outra Route
        return Pair.builder().build();
    }

    protected Pair insertAChargingStationInRandomRoute() {
        List<Integer> freeChargingStations = getFreeChargingStations().stream().collect(Collectors.toList());
        if (freeChargingStations.size() == 0) {
            return null;
        }
        int pos = Utils.getRandomNumber(0, freeChargingStations.size() - 1);
        int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        // TODO: optimize
        int index = Utils.getRandomNumber(0, this.sol.get(posRoute).getClients().size());
        this.sol.get(posRoute).getChargingStations().add(new RechargePoint(freeChargingStations.get(pos), index));
        Double oldCostRoute = this.sol.get(posRoute).cost;
        Double costRoute = ObjFunction.evaluateRoute(this.sol.get(posRoute));
        Double currentCost = this.sol.cost - oldCostRoute + costRoute;
        Movement mov = Movement.builder().type(Utils.MOVE_INSERT_CS).indexes(List.of(posRoute, index)).build();
        return new Pair(currentCost, mov);
    }

    protected Pair removeChargingStationInRandomRoute() {
        int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int index = Utils.getRandomNumber(0, this.sol.get(posRoute).getClients().size());
        Route route = this.sol.get(posRoute);

        if (route.chargingStations.isEmpty()) {
            return null;
        }

        route.getChargingStations().remove(index);
        Double oldCostRoute = this.sol.get(posRoute).cost;
        Double currentCostRoute = ObjFunction.evaluateRoute(route);
        Double currentCost = this.sol.cost - oldCostRoute + currentCostRoute;
        Movement mov = Movement.builder().type(Utils.MOVE_REMOVE_CS).indexes(List.of(posRoute, index)).build();
        return new Pair(currentCost, mov);
    }

    protected Set<Integer> getFreeChargingStations() {
        Set<Integer> allChargingStations = new HashSet<>(ObjFunction.getChargingStations());
        for (int i = 0; i < this.sol.size(); ++i) {
            for (int j = 0; j < sol.get(i).getChargingStations().size(); ++j) {
                allChargingStations.remove(sol.get(i).getChargingStations().get(j).chargingStation);
            }
        }
        return allChargingStations;
    }


    protected Pair moveRandom2OptClients() {
        int posRoute1 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int posRoute2 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        Route route1 = this.sol.get(posRoute1);
        Route route2 = this.sol.get(posRoute2);

        if (route1.getClients().size() > 0 && route2.getClients().size() > 0) {
            int sizeClients = route1.getClients().size();
            int posClient1 = Utils.getRandomNumber(0, sizeClients - 1);

            sizeClients = route2.getClients().size();
            int posClient2 = Utils.getRandomNumber(0, sizeClients - 1);

            int client1 = route1.getClients().get(posClient1);
            int client2 = route1.getClients().get(posClient2);

            route1.getClients().set(posClient1, client2);
            route2.getClients().set(posClient2, client1);

            Double oldCost = route1.cost + route2.cost;
            route1.cost = ObjFunction.evaluateRoute(route1);
            route2.cost = ObjFunction.evaluateRoute(route2);
            Double newCost = route1.cost + route2.cost;

            Double currentCost = this.sol.cost + newCost - oldCost;
            Movement mov = Movement.builder().type(Utils.MOVE_CLIENT_NEIGHBORHOOD).indexes(List.of(posRoute1, posRoute2, posClient1, posClient2)).build();
            return new Pair(currentCost, mov);
        }
        return null;
    }


    // just to tests
    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        int fleetSize = 10;

        TS_ECVRP ts = new TS_ECVRP(20, 1000, "instances/c101_21.txt", fleetSize);

        Solution<Route> bestSol = ts.solve();

        System.out.println("maxVal = " + bestSol);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
    }
}
