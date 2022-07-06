package problems.ecvrp;

import problems.Evaluator;
import solutions.Route;
import solutions.Solution;

import java.io.*;
import java.util.*;

import static java.lang.Math.max;
import static problems.ecvrp.Utils.DEPOT_NODE;
import static problems.ecvrp.Utils.GOOD_BLOCK;


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
        this.numberOfRoutes = fleetSize;
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
        return this.numberOfRoutes;
    }


    @Override
    public List<Integer> getChargingStations() {
        return this.chargeStationsNodes;
    }

    @Override
    public Integer getNumberRoutes() {
        return this.numberOfRoutes;
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
    public Double evaluate(Solution<Route> sol) {
        System.out.println("QUANTIDADE DE CARROS: " + sol.size());
        Double sum = 0.;
        for(int i = 0; i < sol.size() ; ++i){
            sum += evaluateRoute(sol.get(i));
        }
        sol.cost = sum;
        return sum;
    }

    @Override
    public Double evaluateRoute(Route route) {

        Truck truck = new Truck(
                this.batteryCapacity,
                this.availableTime,
                this.loadCapacity,
                this.batteryChargeRate,
                this.batteryConsumptionRate,
                this.velocity,
                this.nodesCoordinates.get(DEPOT_NODE),
                0.,
                GOOD_BLOCK);

        route.resetIndex();

        route.getChargingStations().sort((cs1, cs2) -> {
            if (cs1.getIndex() > cs2.getIndex()) return 1;
            if (cs1.getIndex() < cs2.getIndex()) return -1;
            return 0;
        });

        while (route.hasNextClient() || route.hasNextCS()){
            if (route.visitCSNow()) {
                int csIdx = route.getCurrentCs().getChargingStation();
                truck.goToNextChargingStation(this.batteryCapacity,this.nodesCoordinates.get(csIdx), this.servicesTimes.get(csIdx));
                route.nextCS();
                continue;
            }
            if (!route.hasNextClient()) {
                route.nextCS();
                continue;
            }
            // go to the next client
            int clientIndex = route.getCurrentClient();
            truck.goToNextNode(this.nodesCoordinates.get(clientIndex), this.demands.get(clientIndex), this.servicesTimes.get(clientIndex));
            route.nextClient();
        }
        // go to depot
        truck.goToNextNode(this.nodesCoordinates.get(DEPOT_NODE), this.demands.get(DEPOT_NODE), this.servicesTimes.get(DEPOT_NODE));
        route.setCost(truck.getCost());

        return route.getCost();
    }


    // just to test
    public static void main(String[] args) throws IOException {
        ECVRP ecvrp = new ECVRP("instances/c101_21.txt", 7);
        // TODO: create a random solution for test and evaluate
    }
}
