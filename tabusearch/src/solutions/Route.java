package solutions;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    public List<Integer> clients;
    public List<RechargePoint> chargingStations;
    public Double cost;

    public void addClient(Integer e){
        this.clients.add(e);
    }

    public void addCS(RechargePoint rp){
        this.chargingStations.add(rp);
    }
}
