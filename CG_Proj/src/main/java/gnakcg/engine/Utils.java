package gnakcg.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
     * Returns the url of a given file.
     *
     * @param fileName the name of the file whose url to return.
     * @return the URL
     */
    public static URL getResourceURL(String fileName) {
        return Utils.class.getResource(fileName);
    }

    /**
     * Fully loads a resource and returns it as a string. Useful for loading shaders.
     */
    public static String loadResource(String fileName) throws IOException {
        String result;
        try (InputStream in = getResourceURL(fileName).openStream();
             Scanner scanner = new Scanner(in, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }


    public static File loadResourceAsFile(String fileName) throws NullPointerException {
        return new File(getResourceURL(fileName).getFile());
    }

    public static String getResourceAbsolutePath(String fileName) throws NullPointerException {
        return loadResourceAsFile(fileName).getAbsolutePath();
    }

    public static byte[] getResourceAsByteArray(String fileName) throws IOException {

        InputStream is = getResourceURL(fileName).openStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[0xFFF];
        while ((read = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }
        buffer.flush();

        return buffer.toByteArray();
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