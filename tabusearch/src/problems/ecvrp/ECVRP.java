package problems.ecvrp;

import problems.Evaluator;
import solutions.Solution;

import java.io.*;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;


public class ECVRP implements Evaluator<Integer> {
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
    public final Integer DEPOT_NODE = 0;
    public Integer size;
    public List<List<Double>> dist;
    public List<Integer> solution;

    public List<Integer> closestChargingStation;


    public ECVRP(String filename) throws IOException {
        this.demands = new ArrayList<>();
        this.nodesCoordinates = new ArrayList<>();
        this.availableTime = 0.;
        this.servicesTimes = new ArrayList<>();
        this.clientsNodes = new ArrayList<>();
        this.chargeStationsNodes = new ArrayList<>();
        this.velocity = 0.;
        this.batteryCapacity = 0.;
        this.loadCapacity = 0.;
        this.batteryConsumptionRate = 0.;
        this.batteryChargeRate = 0.;
        this.size = 0;
        this.dist = new ArrayList<>();
        this.solution = new ArrayList<>();
        this.closestChargingStation = new ArrayList<>();
        readInput(filename);
        calculateEuclideanDistance();
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

        size = nodesCoordinates.size();
    }

    protected void calculateEuclideanDistance(){
        this.dist.clear();
        for(int i = 0; i < this.size; ++i) {
            this.dist.add(new ArrayList<>());
            for(int j = 0; j < this.size; ++j) {
                Double deltaX = nodesCoordinates.get(i).x - nodesCoordinates.get(j).x;
                Double deltaY = nodesCoordinates.get(i).y - nodesCoordinates.get(j).y;
                Double calcDist = sqrt(deltaX*deltaX + deltaY*deltaY);
                dist.get(i).add(calcDist);
            }
        }
    }

    protected void calculateClosesChargingStation() {
        // I will calculate for every nodes..
        this.closestChargingStation.clear();
        for (int i = 0 ; i < nodesCoordinates.size() ; ++i) {
            Double minDist = Double.MAX_VALUE;
            int closestNode = 0;
            for(int j = 0; j < this.chargeStationsNodes.size() ; ++j){
                int cs = this.chargeStationsNodes.get(j);

                if (minDist > getDist(i, cs)) {
                    closestNode = cs;
                    minDist = getDist(i, cs);
                }
            }
            this.closestChargingStation.add(closestNode);
        }
    }

    protected Double getDist(int a, int b){
        return this.dist.get(a).get(b);
    }

    @Override
    public Integer getDomainSize() {
        return size;
    }

    @Override
    public Double evaluate(Solution<Integer> sol) {
        return sol.cost = evaluateECVRP();
    }

    public Double evaluateECVRP() {
        // give list with a specific order of costumer, we need calculate the cost

        int carAmount = 1;

        Double currentBattery = this.batteryCapacity;
        Double currentCapacity = this.loadCapacity;
        Double currentTime = 0.;

        int currentIdx = 0; // idx in solution [2,1,3,4,5,...]
        int nextClient = solution.get(currentIdx);
        int currentNode = DEPOT_NODE;
        // the DEPOT must be the last client at solution
        while(currentIdx < solution.size()) {

    // possibilities at any moment
    // 1 - Go to the another client (I need have battery to back to depot or another CS AND need capacity AND has time to arrive at next client and back to depot)
    // 2 - Go to the charging station (if I have products for the next client AND I don't have battery to arrive AND has time to arrive at next client and back to depot)
    // 3 - Back to the depot (if I don't have products for the next client AND I have battery to go to the depot AND has time)
    // 4 - get a new car (if I'm at depot and I don't have time to go to the next client and back to the depot)

            // possibility 4 - get a new car
            boolean hasTimeToNextClientAndBackToTheDepot_ = hasTimeToNextClientAndBackToTheDepot(currentTime, currentNode, nextClient);
            if(currentNode == DEPOT_NODE && !hasTimeToNextClientAndBackToTheDepot_) {
                // new car!!
                currentBattery = this.batteryCapacity;
                currentCapacity = this.loadCapacity;
                currentTime = 0.;
                carAmount++;
                continue;
            }

            // possibility 1 - Go to the another client
            boolean hasBatteryToNextClientAndBackToTheDepot_ = hasBatteryToNextClientAndBackToTheDepot(currentNode, nextClient);
            boolean hasBatteryToNextClientAndAnotherCS_ = hasBatteryToNextClientAndAnotherCS(currentNode, nextClient);
            boolean hasCapacityToNextClient_ = hasCapacityToNextClient(currentCapacity, nextClient);
            boolean hasTimeToGoToClientAnotherCSAndDepot_ = hasTimeToGoToClientAnotherCSAndDepot(currentTime, nextClient);
            if (hasCapacityToNextClient_
                    && (hasTimeToNextClientAndBackToTheDepot_ || hasTimeToGoToClientAnotherCSAndDepot_)
                    && (hasBatteryToNextClientAndAnotherCS_ || hasBatteryToNextClientAndBackToTheDepot_)) {
                // update car
                currentBattery -= calculateBatterySpent(currentNode, nextClient);
                currentCapacity -= calculateCapacitySpent(nextClient);
                currentTime += calculateTravelTimeSpent(currentNode, nextClient);

                // update next client
                currentIdx++;
                nextClient = solution.get(currentIdx);
                continue;
            }

            // possibility 2 - Go to the charging station
            boolean hasBatteryForNextClient_ = hasBatteryForNextClient(currentNode, nextClient);
            boolean hasTimeToNextCSAndBackToDepot_ = hasTimeToNextCSAndBackToDepot(currentTime, currentNode); // lembrar de incluir tempo de recarga nessa funcao
            if (hasTimeToNextCSAndBackToDepot_
                    && hasCapacityToNextClient_
                    && !hasBatteryForNextClient_) {
                // update car
                currentBattery -= calculateBatterySpent(currentNode, nextClient);
                currentTime += calculateChargingTimeSpent(currentBattery);
                currentBattery = this.batteryCapacity;
                currentTime += calculateTravelTimeSpent(currentNode, nextClient);
                continue;
            }

            // possibility 3 - Back to the depot
            boolean hasTimeToBackToTheDepot_ = hasTimeToBackToTheDepot(currentTime, currentNode);
            assert (hasTimeToBackToTheDepot_ == true): "This shit is wrong! Check condition time again...";
            if(!hasCapacityToNextClient_) {
                // dfs mesmo, no fodac
                obj a = calculateTheBackToTheDepotInMiniMumTime(currentNode, currentTime, DEPOT_NODE);
                currentBattery -= a.battery();
                currentTime += a.time(currentNode, DEPOT_NODE);
                continue;
            }
        }

        return 0.;
    }


    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        // TODO: implement
        return 0.;
    }

    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        // TODO: implement
        return 0.;
    }

    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        // TODO: implement
        return 0.;
    }


    // just to test
    public static void main(String[] args) throws IOException {
        ECVRP ecvrp = new ECVRP("instances/c101_21.txt");
        // TODO: create a random solution for test and evaluate
    }

}
