package keystrokesmod.client.utils;

import java.util.Random;

import static keystrokesmod.client.utils.Utils.rand;

public class MathUtils {
    public static Random rand() {
        return rand;
    }

    public static double round(double n, int d) {
        if (d == 0) {
            return (double) Math.round(n);
        } else {
            double p = Math.pow(10.0D, d);
            return (double) Math.round(n * p) / p;
        }
    }

    public static int randomInt(double inputMin, double inputMax) {
        return (int) (Math.random() * (inputMax - inputMin) + inputMin);
    }
}