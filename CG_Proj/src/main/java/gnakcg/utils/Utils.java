package gnakcg.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.Collection;
import java.util.Scanner;

/**
 * Utility class with helper methods.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class Utils {
    public static boolean isZero(float f, float threshold) {
        return f >= -threshold && f <= threshold;
    }

    /**
     * Converts a Collection of integers to a an array of primitive integers.
     */
    public static int[] intListToArray(Collection<Integer> list) {
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }

    /**
     * Converts a Collection of floats to a an array of primitive floats.
     */
    public static float[] floatListToArray(Collection<Float> list) {
        if (list == null)
            return new float[0];

        float[] floatArr = new float[list.size()];
        int i = 0;
        for (Float aFloat : list) {
            floatArr[i] = aFloat;
            i = i + 1;
        }
        return floatArr;
    }
}