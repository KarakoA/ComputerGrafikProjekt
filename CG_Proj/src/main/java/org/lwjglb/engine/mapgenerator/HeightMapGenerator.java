package org.lwjglb.engine.mapgenerator;

import org.joml.SimplexNoise;
import org.joml.Vector2f;

import java.util.Random;

/**
 * @author Anton K.
 */
public class HeightMapGenerator {

    private final int seed;
    private final float scale;
    private final int octaves;
    private final float frequencyFactor;
    private final float amplitudeFactor;

    public HeightMapGenerator(int seed, float scale, int octaves, float frequencyFactor, float amplitudeFactor) {
        this.seed = seed;
        this.scale = scale <= 0 ? 0.0001f : scale;
        this.octaves = octaves;
        this.frequencyFactor = frequencyFactor;
        this.amplitudeFactor = amplitudeFactor;
    }

    public float[][] generate(int startX, int startY, int width, int height) {

        Random r = new Random(seed);
        Vector2f[] octaveOffsets = new Vector2f[octaves];
        for (int i = 0; i < octaves; i++) {
            float offsetX = (r.nextFloat() * 20000) - 10000;
            float offsetY = (r.nextFloat() * 20000) - 10000;
            octaveOffsets[i] = new Vector2f(offsetX, offsetY);
        }

        float[][] heightMap = new float[width][height];
        float maxNoiseHeight = 1;//Float.MIN_VALUE;
        float minNoiseHeight = -5;//Float.MAX_VALUE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float amplitude = 1;
                float frequency = 1;
                float noiseHeight = 0;

                for (int i = 0; i < octaves; i++) {
                    //otherwise we will not get smooth transitions
                    float sampleX = (startX+x) * frequency / scale + octaveOffsets[i].x;
                    float sampleY = (startY+y) * frequency / scale + octaveOffsets[i].y;
                    float simplexNoise = SimplexNoise.noise(sampleX, sampleY) * 2 - 1;
                    heightMap[x][y] = simplexNoise;
                    noiseHeight += simplexNoise * amplitude;

                    amplitude *= amplitudeFactor;
                    frequency *= frequencyFactor;
                }
                if (noiseHeight > maxNoiseHeight) {
                  //  maxNoiseHeight = noiseHeight;
                } else if (noiseHeight < minNoiseHeight) {
                  //  minNoiseHeight = noiseHeight;
                }
                heightMap[x][y] = noiseHeight;
            }
        }
        normalize(heightMap, minNoiseHeight, maxNoiseHeight, 0, 255 * 255 * 255);
        return heightMap;
    }

    private void normalize(float[][] data, float min, float max, float normMin, float normMax) {
        float delta = max - min;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] =normMin+ ((data[i][j] - min) / delta) * (normMax -normMin) ;
            }
        }
    }
}