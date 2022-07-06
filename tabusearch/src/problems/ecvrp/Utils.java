package problems.ecvrp;

public class Utils {
    public static final Integer BAD_BLOCK = -1;
    public static final Integer GOOD_BLOCK = -2;

    public static final Integer MOVE_CLIENT_NEIGHBORHOOD = -1;
    public static final Integer MOVE_INSERT_CS = -2;
    public static final Integer MOVE_REMOVE_CS = -3;
    public static final Integer MOVE_RELOCATE_CLIENT = -4;


    public static final Double PENALTY_CAPACITY = 1000.0;
    public static final Double PENALTY_TIME = 1000.0;
    public static final Double PENALTY_BATTERY = 1000.0;


    public static final Integer DEPOT_NODE = 0;

    public static Integer getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static Double calcDist(Coordinates nodeA, Coordinates nodeB) {
        double deltaX = nodeA.getX() - nodeB.getX();
        double deltaY = nodeA.getY() - nodeB.getY();
        return Math.sqrt(deltaY*deltaY + deltaX*deltaX);
    }
}


