package problems.ecvrp;

public class Utils {
    public static final Integer BAD_BLOCK = -1;
    public static final Integer GOOD_BLOCK = -2;

    public static final Double PENALTY_CAPACITY = 1000.0;
    public static final Double PENALTY_TIME = 1000.0;
    public static final Double PENALTY_BATTERY = 1000.0;

    public static final Integer DEPOT_NODE = 0;

    public static Double calcDist(Coordinates nodeA, Coordinates nodeB) {
        double deltaX = nodeA.getX() - nodeB.getX();
        double deltaY = nodeA.getY() - nodeB.getY();
        return Math.sqrt(deltaY*deltaY + deltaX*deltaX);
    }
}
