package solutions;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class Route {
    public List<Integer> clients;
    public List<RechargePoint> chargingStations;
    public double cost;

    public Route(List<Integer> clients, List<RechargePoint> chargingStations, double cost) {
        this.clients = new ArrayList<>(clients);
        this.chargingStations = new ArrayList<>(chargingStations);
        this.cost = cost;
    }

    public Route() {
        this.clients = new ArrayList<>();
        this.chargingStations = new ArrayList<>();
        this.cost = Double.MAX_VALUE;
    }

    public void addClient(Integer e){
        this.clients.add(e);
    }

    public void addCS(RechargePoint rp){
        this.chargingStations.add(rp);
    }

    public List<Integer> getClientsCopy() {
        return new ArrayList<>(clients);
    }

    public Route getCopy() {
        return new Route(this.clients, this.chargingStations, this.cost);
    }
}
