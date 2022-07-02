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
    public Integer depotNode;
    public Integer size;
    public List<List<Double>> dist;


    public List<Integer> solution;


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
        this.size = 0;
        this.dist = new ArrayList<>();
        this.solution = new ArrayList<>();

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
        // TODO: implement
        Double sum = 0.;
        for(int i = 0 ; i < this.solution.size() - 1; ++i){
            sum += getDist(i,i+1);
        }
        return sum;
    }

    protected boolean isFeasible(){
        // TODO: implement
        return true;
    }

    protected void makeFeasible(){
        // TODO: implement
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
