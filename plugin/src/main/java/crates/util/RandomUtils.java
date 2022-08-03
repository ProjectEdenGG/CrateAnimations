package crates.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomUtils {

    private static final Random random = new Random();

    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return new ArrayList<>(list).get(RandomUtils.random.nextInt(list.size()));
    }

    public static int randomInt(int max) {
        return RandomUtils.randomInt(0, max);
    }

    public static int randomInt(int min, int max) {
        if (min == max) {
            return min;
        }
        return (int) ((RandomUtils.random.nextDouble() * ((max - min) + 1)) + min);
    }

    public static double randomDouble(double max) {
        return RandomUtils.randomDouble(0, max);
    }

    public static double randomDouble(double min, double max) {
        if (min == max) {
            return min;
        }
        return min + (max - min) * RandomUtils.random.nextDouble();
    }

    public static boolean chanceOf(int chance) {
        return RandomUtils.chanceOf((double) chance);
    }

    public static boolean chanceOf(double chance) {
        return RandomUtils.randomInt(100) <= chance;
    }

}
