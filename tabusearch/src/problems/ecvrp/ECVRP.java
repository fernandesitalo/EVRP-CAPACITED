package problems.ecvrp;

import problems.Evaluator;
import solutions.Block;
import solutions.Solution;

import java.io.*;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static problems.ecvrp.Utils.DEPOT_NODE;
import static problems.ecvrp.Utils.GOOD_BLOCK;


public class ECVRP implements Evaluator<Block> {
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


    public Solution<Block> blocks;


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
        this.blocks = new Solution<>();

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

        // numero de caminhoes/carros
        size = this.clientsNodes.size();
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
        // numero de blocks!!! isto e, numero de carros
        return size;
    }

    @Override
    public Double evaluate(Solution<Block> sol) {
        System.out.println("QUANTIDADE DE CARROS: " + blocks.size());
        Double sum = 0.;
        for(int i = 0 ; i < blocks.size() ; ++i){
            sum += evaluateBlock(blocks.get(i));
        }
        blocks.cost = sum;
        return sum;
    }

    @Override
    public Double evaluateInsertionCost(Block elem, Solution<Block> sol) {
        // TODO: implement - como inserir um block???
        return null;
    }

    @Override
    public Double evaluateRemovalCost(Block elem, Solution<Block> sol) {
        // TODO: implement - como remover um block???
        return null
    }

    @Override
    public Double evaluateExchangeCost(Block elemIn, Block elemOut, Solution<Block> sol) {
        // TODO: implement - como avalivar o custo de troca de um block para outro????
        return null;
    }

    protected Double evaluateBlock(Block block) {

        Truck truck = new Truck(
                this.batteryCapacity,
                this.availableTime,
                this.loadCapacity,
                this.batteryChargeRate,
                this.batteryConsumptionRate,
                this.nodesCoordinates.get(DEPOT_NODE),
                0.,
                GOOD_BLOCK);

        block.resetIndex();

        // talvez tenha que ordenar o vetor de charging station pelo indice
        block.getChargingStations().sort((cs1, cs2) -> {
            //Compares its two arguments for order.
            // Returns a negative integer, zero, or a positive integer
            // as the first argument is less than, equal to, or greater than the second.
            if (cs1.getIndex() > cs2.getIndex()) return 1;
            if (cs1.getIndex() < cs2.getIndex()) return -1;
            return 0;
        });

        while (block.hasNextClient()){
            if (block.visitCSNow()) {
                int csIdx = block.getCurrentCs().getChargingStation();
                truck.goToNextChargingStation(this.batteryCapacity,this.nodesCoordinates.get(csIdx));
                block.nextCS();
                continue;
            }
            // go to the next client
            int clientIndex = block.getCurrentClient();
            truck.goToNextNode(this.nodesCoordinates.get(clientIndex), this.demands.get(clientIndex));
            block.nextClient();
        }
        // go to depot
        truck.goToNextNode(this.nodesCoordinates.get(DEPOT_NODE), this.demands.get(DEPOT_NODE));
        block.setCost(truck.getCost());

        return block.getCost();
    }


    // just to test
    public static void main(String[] args) throws IOException {
        ECVRP ecvrp = new ECVRP("instances/c101_21.txt");
        // TODO: create a random solution for test and evaluate
    }

}
