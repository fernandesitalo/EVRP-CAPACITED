package problems.ecvrp;

import java.util.Random;

public class Utils {
    public static final Integer BAD_BLOCK = -1;
    public static final Integer GOOD_BLOCK = -2;

    public static final Integer SWAP_MOVE = -10;
    public static final Integer MOVE_INSERT_CS = -20;
    public static final Integer MOVE_REMOVE_CS = -30;
    public static final Integer MOVE_RELOCATE_CLIENT = -40;


    public static final Double PENALTY_CAPACITY = 1000.0;
    public static final Double PENALTY_TIME = 1000.0;
    public static final Double PENALTY_BATTERY = 1000.0;

    public static Integer getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static Integer getRandomNumberInclusiveMax(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));

    }

    public static void swap(Integer a, Integer b) {
        int temp = b;
        b = a;
        a = temp;
    }
}


