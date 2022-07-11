package problems.ecvrp;

import problems.Evaluator;
import solutions.RechargePoint;
import solutions.Route;
import solutions.Solution;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;
import static problems.ecvrp.Utils.*;


public class ECVRP implements Evaluator<Route> {
    public Double batteryCapacity;
    public Double loadCapacity;
    public Double batteryConsumptionRate;
    public Double batteryChargeRate;
    public Double velocity;
    public List<Double> demands;
    public List<Coordinates> nodesCoordinates;
    public Double availableTime;
    public List<Double> servicesTimes;
    public List<Integer> clientsNodes;
    public List<Integer> chargeStationsNodes;
    public Integer depotNode;
    public Integer fleetSize;

    public ECVRP(String filename, Integer fleetSize) throws IOException {
        this.demands = new ArrayList<>();
        this.nodesCoordinates = new ArrayList<>();
        this.availableTime = 0.;
        this.servicesTimes = new ArrayList<>();
        this.clientsNodes = new ArrayList<>();
        this.chargeStationsNodes = new ArrayList<>();
        this.depotNode = 0;
        this.velocity = 0.;
        this.batteryCapacity = 0.;
        this.loadCapacity = 0.;
        this.batteryConsumptionRate = 0.;
        this.batteryChargeRate = 0.;
        this.fleetSize = fleetSize;
        readInput(filename);
    }

    protected void readInput(String filename) throws IOException {
        Reader fileInst = new BufferedReader(new FileReader(filename));
        StreamTokenizer stok = new StreamTokenizer(fileInst);

        // ignore first line
        for(int i = 0 ; i < 8 ; ++i) stok.nextToken();

        int nodeIdx = 0;
        int typeStok = 0;

        stok.nextToken();
        do{
            String stringID = stok.sval;

            stok.nextToken();
            String nodeType = stok.sval;

            stok.nextToken();
            Double x = stok.nval;

            stok.nextToken();
            Double y = stok.nval;

            stok.nextToken();
            Double demand = stok.nval;

            stok.nextToken();
            Double readyTime = stok.nval;

            stok.nextToken();
            Double dueDate = stok.nval;

            stok.nextToken();
            Double serviceTime = stok.nval;

            this.nodesCoordinates.add(Coordinates.builder().x(x).y(y).build());
            this.demands.add(demand);
            this.availableTime = max(availableTime, dueDate);
            this.servicesTimes.add(serviceTime);

            if(Objects.equals(nodeType, "f")){
                this.chargeStationsNodes.add(nodeIdx);
            } else if (Objects.equals(nodeType, "c")){
                this.clientsNodes.add(nodeIdx);
            }
            nodeIdx++;

            typeStok = stok.nextToken();
        } while (typeStok != StreamTokenizer.TT_WORD || !Objects.equals(stok.sval, "Q"));

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        this.batteryCapacity = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        this.loadCapacity = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        this.batteryConsumptionRate = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        this.batteryChargeRate = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        this.velocity = stok.nval;
    }

    @Override
    public List<Integer> getClients() {
        return this.clientsNodes;
    }

    @Override
    public Integer numberOfRoutes() {
        return this.fleetSize;
    }


    @Override
    public List<Integer> getChargingStations() {
        return this.chargeStationsNodes;
    }

    @Override
    public Integer getNumberRoutes() {
        return this.fleetSize;
    }

    @Override
    public Integer getNumberClients() {
        return this.clientsNodes.size();
    }

    @Override
    public Integer getNumberChargingStations() {
        return this.chargeStationsNodes.size();
    }

    @Override
    public Double evaluate(Solution<Route> sol) throws Exception {

        Set<Integer> visitedClients = new HashSet<>();
        double sum = 0.;
        for (Route route : sol.routes) {
            evaluateRoute(route);

            sum += route.cost;
            visitedClients.addAll(route.clients);
        }

        if (visitedClients.size() != this.clientsNodes.size()) {
            return sol.cost = Double.MAX_VALUE;
        }

        return sol.cost = sum;
    }

    @Override
    public Double evaluateRoute(Route route) {

        if(route.clients.isEmpty()) {
            return route.cost = 0.0;
        }

        List<Integer> completeRoute = route.getClientsCopy();
        for(RechargePoint r : route.chargingStations) {
            try {
                completeRoute.add(min(r.index, completeRoute.size()-1), r.chargingStation);
            } catch (Exception e) {
                System.out.println("DEBUG ARRUMAR AQUI "  + r.index + " " + completeRoute.size() + " " + min(r.index, completeRoute.size()-1) + " " +e.getMessage());
            }
        }
        completeRoute.add(0, depotNode);
        completeRoute.add(depotNode);

        double cost = 0.0;
        double loadCapacity = this.loadCapacity;
        double battery = this.batteryCapacity;
        double time = this.availableTime;

        for (int i = 1; i < completeRoute.size(); i++) {
            int node1 = completeRoute.get(i-1);
            int node2 = completeRoute.get(i);

            double dist = calcDist(nodesCoordinates.get(node1), nodesCoordinates.get(node2));

            loadCapacity -= this.demands.get(node2);
            battery -= dist/this.batteryConsumptionRate;
            time -= dist/this.velocity + servicesTimes.get(node2);

            if (battery < 0) {
//                System.out.println("Battery Penalty");
                cost += abs(battery) * PENALTY_BATTERY;
            }

            if (chargeStationsNodes.contains(node2)) {
                time -= (batteryCapacity - battery) * batteryChargeRate;
                battery = batteryCapacity;
            }
            cost += dist;
        }

        if (time < 0)  {
            cost += abs(time) * PENALTY_TIME;
//            System.out.println("Time Penalty");
        }
        if (loadCapacity < 0) {
            cost += abs(loadCapacity) * PENALTY_CAPACITY;
//            System.out.println("Load Penality");
        }

        return route.cost = cost;
    }

    // just to test
    public static void main(String[] args) throws IOException {
        ECVRP ecvrp = new ECVRP("instances/c101_21.txt", 7);
        // TODO: create a random solution for test and evaluate
    }
}
