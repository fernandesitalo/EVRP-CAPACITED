package problems.ecvrp.solver;

import problems.Evaluator;
import problems.ecvrp.MoveWithCost;
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

    protected MoveWithCost removeClientAndInsertInAnotherRoute(Solution<Route> sol) {
        int route1Idx = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int route2Idx = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);

        while (route2Idx == route1Idx)
            route2Idx = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);

        if (route1Idx > route2Idx) swap(route1Idx, route2Idx);

        Route route1 = sol.getRouteCopy(route1Idx);
        if (route1.clients.size() == 0)  return null;
        Route route2 = sol.getRouteCopy(route2Idx);


        int client1Idx = Utils.getRandomNumber(0, route1.clients.size() - 1);
        int client2Idx = Utils.getRandomNumber(0, route2.clients.size());
        int client1 = route1.clients.get(client1Idx);

        double currentCost = sol.cost - route1.cost - route2.cost;

        route1.clients.remove(client1Idx);
        route2.clients.add(client2Idx, client1);
        currentCost += ObjFunction.evaluateRoute(route1) + ObjFunction.evaluateRoute(route2);

        return new MoveWithCost(currentCost, List.of(Utils.MOVE_RELOCATE_CLIENT, route1Idx, route2Idx, client1Idx, client2Idx));
    }

    public MoveWithCost insertAChargingStationInRandomRoute(Solution<Route> sol) {
        List<Integer> freeCSs = getFreeChargingStations(sol).stream().toList();

        if (freeCSs.size() == 0) {
            return null;
        }

        int csToInsertIdx = Utils.getRandomNumber(0, freeCSs.size() - 1);
        int routeIdx = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int clientIdx = Utils.getRandomNumber(0, sol.getRouteCopy(routeIdx).getClients().size());

        RechargePoint newCSinRout = new RechargePoint(freeCSs.get(csToInsertIdx), clientIdx);
        Route tempRoute = sol.getRouteCopy(routeIdx);

        double oldRouteCost = sol.getRoute(routeIdx).cost;
        tempRoute.addCS(newCSinRout);
        Double newCost = ObjFunction.evaluateRoute(tempRoute);

        Double currentCost = sol.cost - oldRouteCost + newCost;

        return new MoveWithCost(currentCost, List.of(Utils.MOVE_INSERT_CS, routeIdx, clientIdx, freeCSs.get(csToInsertIdx)));
    }

    protected MoveWithCost removeChargingStationInRandomRoute(Solution<Route> sol) throws Exception {
        int routeIdx = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        Route route = sol.getRouteCopy(routeIdx);
        double oldCostRoute = route.cost;

        if (route.chargingStations.size() == 0) {
            return null;
        }
        int index = Utils.getRandomNumber(0, sol.getRouteCopy(routeIdx).getChargingStations().size());

        route.removeCS(index);

        double currentCostRoute = ObjFunction.evaluateRoute(route);

        double newSolCost = sol.cost + currentCostRoute - oldCostRoute;

        return new MoveWithCost(newSolCost, List.of(Utils.MOVE_REMOVE_CS, routeIdx, index));
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

    protected MoveWithCost swapRandomNeighbor(Solution<Route> sol) throws Exception {
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

        return new MoveWithCost(newCost, List.of(Utils.SWAP_MOVE, routeAIdx, routeBIdx, client1Idx, client2Idx));
    }

    protected void applyInsertCsMove(Solution<Route> sol, int routeIdx, int clientIdx, int csToInsert){
        double oldCostRoute = sol.getRoute(routeIdx).cost;
        sol.addCS(routeIdx, clientIdx, csToInsert);
        double newRoute = ObjFunction.evaluateRoute(sol.getRoute(routeIdx));
        sol.cost = sol.cost - oldCostRoute + newRoute;
    }

    protected void applyRemoveCsMove(Solution<Route> sol, int routeIdx, int indexToRemove){
        double oldCostRoute = sol.getRouteCopy(routeIdx).cost;
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

    protected void applyMove(Solution<Route> sol, List<Integer> mov) throws Exception {
        int moveType = mov.get(0);
        if (Objects.equals(moveType, Utils.SWAP_MOVE)) {
            int routeAIdx = mov.get(1);
            int routeBIdx = mov.get(2);
            int clientIdxA = mov.get(3);
            int clientIdxB = mov.get(4);

            apply2OptMove(sol, routeAIdx, routeBIdx, clientIdxA, clientIdxB);
        } else if (Objects.equals(moveType, Utils.MOVE_INSERT_CS)) {
            int routeIdx = mov.get(1);
            int clientIdx = mov.get(2);
            int csToInsert = mov.get(3);
            applyInsertCsMove(sol, routeIdx, clientIdx, csToInsert);

        } else if (Objects.equals(moveType, Utils.MOVE_REMOVE_CS)) {
            int routeIdx = mov.get(1);
            int indexToRemove = mov.get(2);
            applyRemoveCsMove(sol, routeIdx, indexToRemove);

        } else  if (Objects.equals(moveType, MOVE_RELOCATE_CLIENT)){
            int routeIdx1 = mov.get(1);
            int routeIdx2 = mov.get(2);
            int clientIdx1 = mov.get(3);
            int clientIdx2 = mov.get(4);

            applyReallocateClientMove(sol, routeIdx1, routeIdx2, clientIdx1, clientIdx2);
        }
    }

    private void applyReallocateClientMove(Solution<Route> sol, int routeIdx1, int routeIdx2, int clientIdx1, int clientIdx2) {

        int client1 = sol.getRoute(routeIdx1).clients.get(clientIdx1);

        Route route1 = sol.getRoute(routeIdx1);
        Route route2 = sol.getRoute(routeIdx2);

        route1.clients.remove(clientIdx1);
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
