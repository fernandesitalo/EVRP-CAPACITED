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
    public List<List<Double>> dist;

    public Integer parameterM = 10;


    public Solution<Route> routes;


    public ECVRP(String filename) throws IOException {
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
        this.dist = new ArrayList<>();
        this.routes = new Solution<>();

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

            nodesCoordinates.add(Coordinates.builder().x(x).y(y).build());
            demands.add(demand);
            availableTime = max(availableTime, dueDate);
            servicesTimes.add(serviceTime);
            if(nodeType == "f"){
                chargeStationsNodes.add(nodeIdx);
            } else if (nodeType == "c"){
                clientsNodes.add(nodeIdx);
            }
            nodeIdx++;

            typeStok = stok.nextToken();
        } while (typeStok != StreamTokenizer.TT_WORD || !Objects.equals(stok.sval, "Q"));

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        batteryCapacity = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        loadCapacity = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        batteryConsumptionRate = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        batteryChargeRate = stok.nval;

        while(stok.nextToken() != StreamTokenizer.TT_NUMBER){}
        velocity = stok.nval;
    }

    protected Double getDist(int a, int b){
        return this.dist.get(a).get(b);
    }

    @Override
    public List<Integer> getClients() {
        return this.clientsNodes;
    }

    @Override
    public List<Integer> getChargingStations() {
        return this.chargeStationsNodes;
    }

    @Override
    public Integer getNumberRoutes() {
        return parameterM;
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
        System.out.println("QUANTIDADE DE CARROS: " + routes.size());
        Double sum = 0.;
        for(int i = 0; i < routes.size() ; ++i){
            sum += evaluateRoute(routes.get(i));
        }
        routes.cost = sum;
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

        // talvez tenha que ordenar o vetor de charging station pelo indice
        route.getChargingStations().sort((cs1, cs2) -> {
            //Compares its two arguments for order.
            // Returns a negative integer, zero, or a positive integer
            // as the first argument is less than, equal to, or greater than the second.
            if (cs1.getIndex() > cs2.getIndex()) return 1;
            if (cs1.getIndex() < cs2.getIndex()) return -1;
            return 0;
        });

        while (route.hasNextClient()){
            if (route.visitCSNow()) {
                int csIdx = route.getCurrentCs().getChargingStation();
                truck.goToNextChargingStation(this.batteryCapacity,this.nodesCoordinates.get(csIdx));
                route.nextCS();
                continue;
            }
            // go to the next client
            int clientIndex = route.getCurrentClient();
            truck.goToNextNode(this.nodesCoordinates.get(clientIndex), this.demands.get(clientIndex));
            route.nextClient();
        }
        // go to depot
        truck.goToNextNode(this.nodesCoordinates.get(DEPOT_NODE), this.demands.get(DEPOT_NODE));
        route.setCost(truck.getCost());

        return route.getCost();
    }


    // just to test
    public static void main(String[] args) throws IOException {
        ECVRP ecvrp = new ECVRP("instances/c101_21.txt");
        // TODO: create a random solution for test and evaluate
    }

}
