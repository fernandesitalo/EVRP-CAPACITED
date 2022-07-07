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

    public TS_ECVRP(Integer tenure, Integer iterations, String filename, Integer numberOfRoutes) throws IOException {
        super(new ECVRP(filename, numberOfRoutes), tenure, iterations);
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
        for(int i = 0 ; i < ObjFunction.numberOfRoutes() ; ++i) {
            sol.add(Route.builder().build());
        }
        return sol;
    }

    protected Pair removeClientAndInsertInAnotherRoute() {
        int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        if (this.sol.get(posRoute).clients.size() == 0) {
            return null;
        }
        Double currentCost = this.cost;
        Movement mov = new Movement();
        mov.setType(Utils.MOVE_RELOCATE_CLIENT);

        int posClient = Utils.getRandomNumber(0, this.sol.get(posRoute).getClients().size() - 1);
        int client1 = this.sol.get(posRoute).clients.get(posClient);

        Route route1 = this.sol.get(posRoute);
        route1.clients.remove(posClient);

        int posRoute2 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int posClient2 = Utils.getRandomNumber(0, this.sol.get(posRoute2).getClients().size());

        Route route2 = this.sol.get(posRoute2);
        route2.clients.add(posClient2, client1);

        currentCost -= route1.cost;
        currentCost += ObjFunction.evaluateRoute(route1);
        currentCost -= route2.cost;
        currentCost += ObjFunction.evaluateRoute(route2);

        mov.setIndexes(List.of(posRoute, posRoute2, posClient, posClient2));

        return new Pair(currentCost, mov);
    }

    protected Pair insertAChargingStationInRandomRoute() {
        List<Integer> freeCSs = getFreeChargingStations().stream().toList();
        if (freeCSs.size() == 0) {
            return null;
        }

        int csToInsertIdx = Utils.getRandomNumber(0, freeCSs.size() - 1);
        int routeIdx = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        int clientIdx = Utils.getRandomNumber(0, this.sol.get(routeIdx).getClients().size());

        RechargePoint newCSinRout = new RechargePoint(freeCSs.get(csToInsertIdx), clientIdx);
        Route tempRoute = this.sol.get(routeIdx); // todo make copy

        Double newCost = ObjFunction.evaluateRoute(tempRoute);
        tempRoute.addCS(newCSinRout);
        Double oldCost = ObjFunction.evaluateRoute(tempRoute);

        Double currentCost = this.sol.cost - oldCost + newCost;
        Movement mov = Movement.builder()
                .type(Utils.MOVE_INSERT_CS)
                .indexes(List.of(routeIdx, clientIdx, freeCSs.get(csToInsertIdx)))
                .build();

        return new Pair(currentCost, mov);
    }

    protected Pair removeChargingStationInRandomRoute() {
        int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
        if (this.sol.get(posRoute).getChargingStations().size() == 0) {
            return null;
        }

        int index = Utils.getRandomNumber(0, this.sol.get(posRoute).getChargingStations().size());
        Route route = this.sol.get(posRoute);

        if (route.chargingStations.isEmpty()) {
            return null;
        }

        route.getChargingStations().remove(index);
        Double oldCostRoute = this.sol.get(posRoute).cost;
        Double currentCostRoute = ObjFunction.evaluateRoute(route);

        Double currentCost = this.cost + currentCostRoute - oldCostRoute;

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
            int client2 = route2.getClients().get(posClient2);

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


    @Override
    public void createInitialSolution() {
//        Solution<Route> sol = new Solution<Route>();
//        sol.cost = Double.MAX_VALUE;
//
//        for (int i = 0; i < this.tenure; i++) {
//            sol.add(new Route(new ArrayList<>(), new ArrayList<>(),0,0,0.0));
//        }

        this.sol =  createARandomSolution();
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

    @Override
    public Solution<Route> createARandomSolution() {

        List<Integer> clients = ObjFunction.getClients();
        List<Integer> cs = ObjFunction.getChargingStations();
        Collections.shuffle(clients);
        Collections.shuffle(cs);

        this.sol = new Solution<Route>();

        for(int i = 0 ; i < ObjFunction.numberOfRoutes(); ++i) {
            this.sol.add(new Route());
        }

        while(clients.size() > 0) {
            int posRoute = Utils.getRandomNumber(0, ObjFunction.numberOfRoutes());
            this.sol.get(posRoute).addClient(clients.get(0));
            clients.remove(0);
        }

        while(cs.size() > 0) {
            int posRoute = Utils.getRandomNumber(0, ObjFunction.numberOfRoutes());
            int index = Utils.getRandomNumber(0, this.sol.get(posRoute).getClients().size());
            this.sol.get(posRoute).addCS(new RechargePoint(cs.get(0), index));
            cs.remove(0);
        }
        this.sol.cost = 0.;
        for(int i = 0 ; i < this.sol.size(); ++i) {
            this.sol.get(i).cost = ObjFunction.evaluateRoute(this.sol.get(i));
            this.sol.cost += this.sol.get(i).cost;
        }
        this.cost = this.sol.cost;

        return null;
    }

    protected void applyMove(Solution<Route> sol, Movement mov) {
        if (Objects.equals(mov.type, Utils.MOVE_CLIENT_NEIGHBORHOOD)) {
            int posRoute1 = mov.getIndexes().get(0);
            int posRoute2 = mov.getIndexes().get(1);
            int posClient1 = mov.getIndexes().get(2);
            int posClient2 = mov.getIndexes().get(3);

            int client1 = this.sol.get(posRoute1).getClients().get(posClient1);
            int client2 = this.sol.get(posRoute2).getClients().get(posClient2);

            this.sol.get(posRoute1).getClients().set(posClient1, client2);
            this.sol.get(posRoute2).getClients().set(posClient2, client1);


            Double oldCost = this.sol.get(posRoute1).cost + this.sol.get(posRoute2).cost;
            this.sol.get(posRoute1).cost = ObjFunction.evaluateRoute(this.sol.get(posRoute1));
            this.sol.get(posRoute2).cost = ObjFunction.evaluateRoute(this.sol.get(posRoute2));
            Double newCost = this.sol.get(posRoute1).cost + this.sol.get(posRoute2).cost;

            this.cost = this.sol.cost + newCost - oldCost;
            this.sol.cost = this.cost;
        } else if (Objects.equals(mov.type, Utils.MOVE_INSERT_CS)) {

            List<Integer> freeChargingStations = getFreeChargingStations().stream().collect(Collectors.toList());

            int posRoute = mov.getIndexes().get(0);
            int index = mov.getIndexes().get(1);
            int pos = mov.getIndexes().get(2);

            this.sol.get(posRoute).getChargingStations().add(new RechargePoint(freeChargingStations.get(pos), index));
            Double oldCostRoute = this.sol.get(posRoute).cost;
            Double costRoute = ObjFunction.evaluateRoute(this.sol.get(posRoute));
            this.sol.cost += oldCostRoute + costRoute;
        } else if (Objects.equals(mov.type, Utils.MOVE_REMOVE_CS)) {
            int posRoute = mov.getIndexes().get(0);
            int index = mov.getIndexes().get(1);

            this.sol.get(posRoute).getChargingStations().remove(index);
            Double oldCostRoute = this.sol.get(posRoute).cost;
            Double currentCostRoute = ObjFunction.evaluateRoute(this.sol.get(posRoute));

            this.cost += +currentCostRoute - oldCostRoute;
        } else  if (Objects.equals(mov.type, Utils.MOVE_RELOCATE_CLIENT)){
            int posRoute = mov.getIndexes().get(0);
            int posRoute2 = mov.getIndexes().get(1);
            int posClient = mov.getIndexes().get(2);
            int posClient2 = mov.getIndexes().get(3);

            int client1 = this.sol.get(posRoute).clients.get(posClient);

            Route route1 = this.sol.get(posRoute);
            route1.clients.remove(posClient);

            Route route2 = this.sol.get(posRoute2);
            route2.clients.add(posClient2, client1);

            this.cost -= route1.cost;
            this.cost += ObjFunction.evaluateRoute(route1);
            this.cost -= route2.cost;
            this.cost += ObjFunction.evaluateRoute(route2);
        }
    }


    // just to tests
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        int fleetSize = 10;
        TS_ECVRP tabusearch = new TS_ECVRP(20, 1000, "instances/c101C5.txt", fleetSize);
        Solution<Route> bestSol = tabusearch.solve();

        System.out.println("maxVal = " + bestSol);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
    }
}
