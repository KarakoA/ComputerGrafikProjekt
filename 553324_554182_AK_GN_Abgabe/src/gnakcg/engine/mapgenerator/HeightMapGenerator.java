package gnakcg.engine.mapgenerator;

import org.joml.SimplexNoise;
import org.joml.Vector2f;

import java.util.Random;

/**
 * A class for generating height maps using simplex noise.
 * @author Anton K.
 * @author Gires N.
 */
public class HeightMapGenerator {

    private final float scale;
    private final int octaves;
    private final float frequencyFactor;
    private final float amplitudeFactor;

    /**
     * Approx. Min and max heights for normalization.
     */
    private final float MAX_NOISE_HEIGHT = 1;
    private final float MIN_NOISE_HEIGHT = -5;

    private Vector2f[] octaveOffsets;

    //seed used only for octaves offsets
    public HeightMapGenerator(long seed, float scale, int octaves, float frequencyFactor, float amplitudeFactor) {
        this.scale = scale <= 0 ? 0.0001f : scale;
        this.octaves = octaves;
        this.frequencyFactor = frequencyFactor;
        this.amplitudeFactor = amplitudeFactor;
        Random r = new Random(seed);
        octaveOffsets = new Vector2f[octaves];
        for (int i = 0; i < octaves; i++) {
            float offsetX = (r.nextFloat() * 20000) - 10000;
            float offsetY = (r.nextFloat() * 20000) - 10000;
            octaveOffsets[i] = new Vector2f(offsetX, offsetY);
        }
    }

    public float[][] generate(int startX, int startY, int width, int height) {
        float[][] heightMap = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float sampleX = (startX + x);
                float sampleY = (startY + y);
                heightMap[x][y] = getHeightAtPosition(sampleX, sampleY);
            }
        }
        normalize(heightMap, 0, 255 * 255 * 255);
        return heightMap;
    }

    private float getHeightAtPosition(float x, float y) {
        float amplitude = 1;
        float frequency = 1;
        float noiseHeight = 0;

        for (int i = 0; i < octaves; i++) {
            //otherwise we will not get smooth transitions
            float sampleX = x * frequency / scale + octaveOffsets[i].x;
            float sampleY = y * frequency / scale + octaveOffsets[i].y;
            float simplexNoise = SimplexNoise.noise(sampleX, sampleY) * 2 - 1;
            noiseHeight += simplexNoise * amplitude;

            amplitude *= amplitudeFactor;
            frequency *= frequencyFactor;
        }
        return noiseHeight;
    }

    private void normalize(float[][] data, float normMin, float normMax) {
        float delta = MAX_NOISE_HEIGHT - MIN_NOISE_HEIGHT;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = normMin + ((data[i][j] - MIN_NOISE_HEIGHT) / delta) * (normMax - normMin);
            }
        }
    }
}