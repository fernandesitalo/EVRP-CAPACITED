package problems.ecvrp;

import lombok.*;

import static problems.ecvrp.Utils.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Truck {
    Double battery;
    Double time;
    Double capacity;

    Double batteryChargeRate;
    Double batteryConsumptionRate;
    Double velocity;

    Coordinates coord;

    Double cost;
    Integer status;

    public void goToNextChargingStation(Double batteryCapacity, Coordinates coordinates, Double servicesTime) {
        // estou em <this.coord> e vou para <coordinates>
        Double dist = Utils.calcDist(this.coord, coordinates);

        // handling with batterry and Time
        Double batteryNeeded = dist * this.batteryConsumptionRate;
        // E PQ FICOU SEM BATERIA NO MEIO DO CAMINHO...
        if(batteryNeeded > this.battery) {
            // apply penality
            this.cost += PENALTY_BATTERY;
            System.out.println("PENALTY_BATTERY");
            this.status = BAD_BLOCK;
        }

        this.battery -= batteryNeeded;

        Double travelTime = dist/this.velocity;
        this.time -= travelTime;

        if (0 > this.time) {
            // apply penalty for this move
            this.cost += PENALTY_TIME;
            System.out.println("PENALTY_TIME");
            this.status = BAD_BLOCK;
        }

        Double batteryToCharge = batteryCapacity - this.battery;
        Double timeToCharge = batteryToCharge * this.batteryChargeRate;

        this.time -= timeToCharge;

        this.time -= servicesTime;

        if (0 > this.time) {
            // apply penalty for this move
            this.cost += PENALTY_TIME;
            System.out.println("PENALTY_TIME");
            this.status = BAD_BLOCK;
        }

        // update cost!!!!!!!!!!!!!!!!!!!1
        this.cost += dist;
    }


    public void goToNextNode(Coordinates coordinates, Double demand) {
        // estou em <this.coord> e vou para <coordinates>
        Double dist = Utils.calcDist(this.coord, coordinates);

        // handling with battery --------------------------------------------------
        Double batteryNeeded = dist * this.batteryConsumptionRate;
        this.battery -= batteryNeeded;
        if(0 > this.battery) {
            // apply penalty for this move
            this.cost += PENALTY_BATTERY;
            System.out.println("PENALTY_BATTERY");
            this.status = BAD_BLOCK;
        }

        // handling with time ------------------------------------------------------
        Double travelTime = dist/this.velocity;
        this.time -= travelTime;
        if (0 > this.time) {
            // apply penalty for this move
            this.cost += PENALTY_TIME;
            System.out.println("PENALTY_TIME");
            this.status = BAD_BLOCK;
        }

        // handling with capacity --------------------------------------------------
        this.capacity -= demand;
        if(0 > this.capacity) {
            // apply penalty for this move
            this.cost += PENALTY_CAPACITY;
            System.out.println("PENALTY_CAPACITY");
            this.status = BAD_BLOCK;
        }

        // update cost!!!!!!!!!!!!!!!!!!!
        this.cost += dist;
    }
}
