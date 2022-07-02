package problems.ecvrp;

import problems.Evaluator;
import solutions.Solution;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.max;


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

        readInput(filename);
    }

    @Override
    public Integer getDomainSize() {
        return size;
    }

    @Override
    public Double evaluate(Solution<Integer> sol) {
        return sol.cost = evaluateECVRP();
    }

    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        return evaluateInsertionECVRP(elem);
    }

    public Double evaluateECVRP() {
        // como vamos calcular????
        return 10.;
    }

    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        return evaluateRemovalECVRP(elem);
    }

    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        return evaluateExchangeECVRP(elemIn, elemOut);
    }

    public Double evaluateInsertionECVRP(int i) {
        return evaluateContributionECVRP(i);
    }

    public Double evaluateRemovalECVRP(int i) {
        return evaluateContributionECVRP(i);
    }

    /**
     * Determines the contribution to the ECVRP objective function from the
     * exchange of two elements one belonging to the solution and the other not.
     *
     * @param nodeA
     *            The index of the element that is considered entering the
     *            solution.
     * @param nodeB
     *            The index of the element that is considered exiting the
     *            solution.
     * @return The variation of the objective function resulting from the
     *         exchange.
     */
    public Double evaluateExchangeECVRP(int nodeA, int nodeB) {

        Double sum = 0.0;

        if (nodeA == nodeB)
            return 0.0;

        sum += evaluateContributionECVRP(nodeA);
        sum -= evaluateContributionECVRP(nodeB);
        return sum;
    }

    private Double evaluateContributionECVRP(int i) {
        return 1.;
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

    // just to test
    public static void main(String[] args) throws IOException {
        ECVRP ecvrp = new ECVRP("instances/c101_21.txt");
    }

}
