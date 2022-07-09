package problems.ecvrp.solver;

import problems.Evaluator;
import problems.ecvrp.Movement;
import problems.ecvrp.Pair;
import problems.ecvrp.Utils;
import solutions.RechargePoint;
import solutions.Route;
import solutions.Solution;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.abs;
import static problems.ecvrp.Utils.MOVE_RELOCATE_CLIENT;
import static problems.ecvrp.Utils.swap;


public class TS_EVRP_NEIGHBORHOOD {

    private Evaluator<Route> ObjFunction;
    private Integer fleetSize;

    public TS_EVRP_NEIGHBORHOOD(Evaluator<Route> objFunction, Integer fleetSize1) throws IOException {
        ObjFunction = objFunction;
        this.fleetSize = fleetSize1;
    }

    protected Pair removeClientAndInsertInAnotherRoute(Solution<Route> sol) {
        int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);

        if (sol.getRoute(posRoute).clients.size() == 0) {
            return null;
        }


        int posClient1 = Utils.getRandomNumber(0, sol.getRoute(posRoute).getClients().size() - 1);
        int client1 = sol.getRoute(posRoute).clients.get(posClient1);

        Route route1 = sol.getRouteCopy(posRoute);

        int posRoute2 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int posClient2 = Utils.getRandomNumber(0, sol.getRoute(posRoute2).getClients().size());

        Double currentCost = sol.cost;

        if(posRoute == posRoute2){
            route1.clients.add(posClient2, client1);
            route1.clients.remove(posClient1);
            currentCost -= sol.getRoute(posRoute).cost;
            currentCost += ObjFunction.evaluateRoute(route1);
        } else {
            Route route2 = sol.getRouteCopy(posRoute2);
            route1.clients.remove(posClient1);
            route2.clients.add(posClient2, client1);
            currentCost -= route1.cost;
            currentCost += ObjFunction.evaluateRoute(route1);
            currentCost -= route2.cost;
            currentCost += ObjFunction.evaluateRoute(route2);
        }

        Movement mov = new Movement();
        mov.setType(Utils.MOVE_RELOCATE_CLIENT);
        mov.setIndexes(List.of(posRoute, posRoute2, posClient1, posClient2));

        return new Pair(currentCost, mov);
    }

    public Pair insertAChargingStationInRandomRoute(Solution<Route> sol) {
        List<Integer> freeCSs = getFreeChargingStations(sol).stream().toList();

        if (freeCSs.size() == 0) {
            return null;
        }

        int csToInsertIdx = Utils.getRandomNumber(0, freeCSs.size() - 1);
        int routeIdx = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int clientIdx = Utils.getRandomNumber(0, sol.getRouteCopy(routeIdx).getClients().size());

        RechargePoint newCSinRout = new RechargePoint(freeCSs.get(csToInsertIdx), clientIdx);
        Route tempRoute = sol.getRouteCopy(routeIdx);

        Double oldCost = sol.getRoute(routeIdx).cost;

        tempRoute.addCS(newCSinRout);
        Double newCost = ObjFunction.evaluateRoute(tempRoute);

        Double currentCost = sol.cost - oldCost + newCost;

        Movement mov = Movement.builder()
                .type(Utils.MOVE_INSERT_CS)
                .indexes(List.of(routeIdx, clientIdx, freeCSs.get(csToInsertIdx)))
                .build();

        return new Pair(currentCost, mov);
    }

    protected Pair removeChargingStationInRandomRoute(Solution<Route> sol) {
        int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        Route route = sol.getRouteCopy(posRoute);
        if (route.chargingStations.size() == 0) {
            return null;
        }
        int index = Utils.getRandomNumber(0, sol.getRouteCopy(posRoute).getChargingStations().size());

        route.removeCS(index);

        Double oldCostRoute = sol.getRouteCopy(posRoute).cost;
        Double currentCostRoute = ObjFunction.evaluateRoute(route);

        Double currentCost = sol.cost + currentCostRoute - oldCostRoute;

        Movement mov = Movement.builder().type(Utils.MOVE_REMOVE_CS).indexes(List.of(posRoute, index)).build();
        return new Pair(currentCost, mov);
    }

    protected Set<Integer> getFreeChargingStations(Solution<Route> sol) {
        Set<Integer> allChargingStations = new HashSet<>(ObjFunction.getChargingStations());
        for (int i = 0; i < sol.routes.size(); ++i) {
            for (int j = 0; j < sol.getRouteCopy(i).getChargingStations().size(); ++j) {
                allChargingStations.remove(sol.getRouteCopy(i).getChargingStations().get(j).chargingStation);
            }
        }
        return allChargingStations;
    }

    protected Pair swapRandomNeighbor(Solution<Route> sol) throws Exception {
        int routeAIdx = Utils.getRandomNumber(0, this.fleetSize - 1);
        int routeBIdx = Utils.getRandomNumber(0, this.fleetSize - 1);

        if (routeBIdx < routeAIdx) {
            swap(routeAIdx, routeBIdx);
        }

        Route route1 = sol.getRouteCopy(routeAIdx);
        Route route2 = sol.getRouteCopy(routeBIdx);

        if (route1.clients.isEmpty() || route2.clients.isEmpty()) {
            return null;
        }

        int client1Idx = Utils.getRandomNumber(0, route1.getClients().size() - 1);
        int client2Idx = Utils.getRandomNumber(0, route2.getClients().size() - 1);

        int client1 = route1.getClients().get(client1Idx);
        int client2 = route2.getClients().get(client2Idx);

        if (client1 > client2) {
            swap(client1, client2);
        }

        route1.getClients().set(client1Idx, client2);
        route2.getClients().set(client2Idx, client1);

        Double newCost = sol.cost - route1.cost + route2.cost;
        Double newRoutesCost = ObjFunction.evaluateRoute(route1) + ObjFunction.evaluateRoute(route2);
        newCost += newRoutesCost;

        Movement mov = Movement
                .builder()
                .type(Utils.SWAP_MOVE)
                .indexes(List.of(routeAIdx, routeBIdx, client1Idx, client2Idx))
                .build();

        return new Pair(newCost, mov);
    }


    protected void applyInsertCsMove(Solution<Route> sol, int routeIdx, int clientIdx, int csToInsertIdx){
        Double oldCostRoute = sol.getRouteCopy(routeIdx).cost;
        sol.addCS(routeIdx, clientIdx, csToInsertIdx);
        Double newRoute = ObjFunction.evaluateRoute(sol.getRouteCopy(routeIdx));
        sol.routes.get(routeIdx).cost = newRoute;
        sol.cost = sol.cost -oldCostRoute + newRoute;
    }

    protected void applyRemoveCsMove(Solution<Route> sol, int routeIdx, int indexToRemove){
        Double oldCostRoute = sol.getRouteCopy(routeIdx).cost;
        sol.routes.get(routeIdx).removeCS(indexToRemove);
        Double newRoute = ObjFunction.evaluateRoute(sol.getRouteCopy(routeIdx));
        sol.routes.get(routeIdx).cost = newRoute;
        sol.cost = sol.cost -oldCostRoute + newRoute;
    }

    protected void apply2OptMove(Solution<Route> sol,  int routeAIdx ,  int routeBIdx ,int clientIdxA, int clientIdxB) throws Exception {
        Route routeA = sol.getRoute(routeAIdx);
        Route routeB = sol.getRoute(routeBIdx);
        double tempCost = sol.cost - (routeA.cost + routeB.cost);

        int client1 = routeA.getClients().get(clientIdxA);
        int client2 = routeB.getClients().get(clientIdxB);

        if (client1 == client2) {
            return;
        }

        routeA.getClients().set(clientIdxA, client2);
        routeB.getClients().set(clientIdxB, client1);

        sol.cost = tempCost + ObjFunction.evaluateRoute(routeA) + ObjFunction.evaluateRoute(routeB);
    }

    protected void applyMove(Solution<Route> sol, Movement mov) throws Exception {
        if (Objects.equals(mov.type, Utils.SWAP_MOVE)) {
            int routeAIdx = mov.getIndexes().get(0);
            int routeBIdx = mov.getIndexes().get(1);
            int clientIdxA = mov.getIndexes().get(2);
            int clientIdxB = mov.getIndexes().get(3);

            apply2OptMove(sol, routeAIdx, routeBIdx, clientIdxA, clientIdxB);
        } else if (Objects.equals(mov.type, Utils.MOVE_INSERT_CS)) {
            int routeIdx = mov.getIndexes().get(0);
            int clientIdx = mov.getIndexes().get(1);
            int csToInsertIdx = mov.getIndexes().get(2);
            applyInsertCsMove(sol, routeIdx, clientIdx, csToInsertIdx);

        } else if (Objects.equals(mov.type, Utils.MOVE_REMOVE_CS)) {
            int routeIdx = mov.getIndexes().get(0);
            int indexToRemove = mov.getIndexes().get(1);
            applyRemoveCsMove(sol, routeIdx, indexToRemove);

        } else  if (Objects.equals(mov.type, MOVE_RELOCATE_CLIENT)){
            int routeIdx1 = mov.getIndexes().get(0);
            int routeIdx2 = mov.getIndexes().get(1);
            int clientIdx1 = mov.getIndexes().get(2);
            int clientIdx2 = mov.getIndexes().get(3);

            applyReallocateClientMove(sol, routeIdx1, routeIdx2, clientIdx1, clientIdx2);
        }
    }

    private void applyReallocateClientMove(Solution<Route> sol, int routeIdx1, int routeIdx2, int clientIdx1, int clientIdx2) {

        int client1 = sol.getRoute(routeIdx1).clients.get(clientIdx1);

        if(routeIdx1 == routeIdx2){
            sol.getRoute(routeIdx1).clients.add(clientIdx2, client1);
            sol.getRoute(routeIdx1).clients.remove(clientIdx1);
            sol.cost -= sol.getRoute(routeIdx1).cost;
            Double newCost = ObjFunction.evaluateRoute(sol.getRoute(routeIdx1));
            sol.getRoute(routeIdx1).cost = newCost;
            sol.cost += newCost;
        } else {
            Route route1 = sol.getRoute(routeIdx1);
            route1.clients.remove(clientIdx1);
            Route route2 = sol.getRoute(routeIdx2);
            route2.clients.add(clientIdx2, client1);

            sol.cost -= route1.cost;
            Double newCost1 = ObjFunction.evaluateRoute(route1);
            sol.cost -= route2.cost;
            Double newCost2 = ObjFunction.evaluateRoute(route2);
            route1.cost = newCost1;
            route2.cost = newCost2;
            sol.cost += newCost1 + newCost2;
        }
    }
}
