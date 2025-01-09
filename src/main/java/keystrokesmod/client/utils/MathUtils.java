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

    public static float randomFloat(double inputMin, double inputMax) {
        return rand().nextFloat() * ((float) inputMax - (float) inputMin) + (float) inputMin;
    }

    public static int limit(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static long limit(long value, long min, long max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float limit(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static double limit(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }
}