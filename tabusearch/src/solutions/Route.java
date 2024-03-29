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

    public Boolean isValid;

    public Route(List<Integer> clients, List<RechargePoint> chargingStations, double cost, boolean isValid) {
        this.clients = new ArrayList<>(clients);
        this.chargingStations = new ArrayList<>(chargingStations);
        this.cost = cost;
        this.isValid = isValid;
    }

    public Route() {
        this.clients = new ArrayList<>();
        this.chargingStations = new ArrayList<>();
        this.cost = 0;
        this.isValid = true;
    }

    public void addClient(Integer e){
        this.clients.add(e);
    }

    public void addCS(RechargePoint rp){
        this.chargingStations.add(rp);
    }

    public void removeCS(int indexToRemove){
        this.chargingStations.remove(indexToRemove);
    }

    public List<Integer> getClientsCopy() {
        return new ArrayList<>(clients);
    }

    public Route getCopy() {
        return new Route(this.clients, this.chargingStations, this.cost, this.isValid);
    }
}
