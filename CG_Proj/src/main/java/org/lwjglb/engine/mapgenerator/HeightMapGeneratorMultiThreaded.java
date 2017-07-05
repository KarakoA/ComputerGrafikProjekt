package org.lwjglb.engine.mapgenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * A class used to generate a height map with multiple thread using simplex noise.
 */
public class HeightMapGeneratorMultiThreaded extends HeightMapGenerator {
    /**
     * Creates a new instance
     *
     * @see HeightMapGenerator#HeightMapGenerator(int, float, int, float, float)
     */
    public HeightMapGeneratorMultiThreaded(int seed, float scale, int octaves, float frequencyFactor, float amplitudeFactor) {
        super(seed, scale, octaves, frequencyFactor, amplitudeFactor);
    }

    /**
     * Generates a heightmap using multiple threads.
     *
     * @param width           the width of the heightmap
     * @param height          the height of the heightmap
     * @param numberOfThreads the number of threads to use
     * @return a heightmap with dimensions (width,height )
     */
    public float[][] generateMultiThreaded(int width, int height, int numberOfThreads) {
        final float[][] result = new float[width][height];
        final List<FutureTask<float[][]>> futures = new LinkedList<>();
        final int widthStep = width / numberOfThreads;

        //assign work
        for (int i = 0; i < numberOfThreads; i++) {
            int startX = widthStep * i;
            FutureTask<float[][]> future = new FutureTask<>(() -> generate(startX, 0, widthStep, height));
            futures.add(future);
        }
        //run
        futures.forEach(f -> new Thread(f).start());
        System.out.println(String.format("Dimensions: %d x %d",width,height));
        System.out.println(String.format("Running in %d Threads.", numberOfThreads));
        try {
            for (int i = 0; i < numberOfThreads; i++) {
                int startX = widthStep * i;
                //wait for result
                float[][] resultI = futures.get(i).get();
                System.arraycopy(resultI, 0, result, startX, resultI.length);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            futures.forEach(future -> future.cancel(true));
        }
        return result;
    }

    /**
     * Generates a height map using as many threads as processor cores.
     *
     * @see HeightMapGeneratorMultiThreaded#generateMultiThreaded(int, int, int)
     */
    public float[][] generateMultiThreaded(int width, int height) {
        int processorCount = Runtime.getRuntime().availableProcessors();
        return generateMultiThreaded(width, height, processorCount);
    }
}