package solutions;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {
    public List<Integer> clients;
    public List<MyPair> chargingStations;

    public Integer currentClientsIndex;
    public Integer currentCSIndex;

    public Double cost;

    public void resetIndex() {
        this.currentClientsIndex = 0;
        this.currentCSIndex = 0;
    }

    public boolean hasNextClient() {
        return this.clients.size() > this.currentClientsIndex;
    }

    public boolean hasNextCS() {
        return this.chargingStations.size() > currentCSIndex;
    }

    public void nextClient() {
        this.currentClientsIndex++;
    }

    public void nextCS() {
        this.currentCSIndex++;
    }

    public Integer getCurrentClient() {
        return this.clients.get(this.currentClientsIndex);
    }

    public MyPair getCurrentCs() {
        return this.chargingStations.get(this.currentCSIndex);
    }

    public boolean visitCSNow() {
        if (hasNextClient() && hasNextCS()) {
            return this.currentClientsIndex == this.chargingStations.get(this.currentCSIndex).index;
        }
        return false;
    }
}
