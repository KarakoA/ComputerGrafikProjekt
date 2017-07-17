package org.lwjglb.engine.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import org.joml.Vector3f;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.services.OpenGLThreadExecutorService;

public class HeightMapMesh {

    private static final int MAX_COLOUR = 255 * 255 * 255;

    private static final float STARTX = 0;

    private static final float STARTZ = 0;

    private final float minY;

    private final float maxY;

    private final Mesh mesh;
    private float[][] heights;

    public HeightMapMesh(float minY, float maxY, float[][] heightMap, int width, int height, Texture texture, int textInc) {
        assert heightMap.length - 2 == height;
        assert heightMap[0].length - 2 == width;

        this.minY = minY;
        this.maxY = maxY;

        heights = new float[height][width];

        float incx = 1f / (width - 1);
        float incz = 1f/ (height - 1);

        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                // Create vertex for current position
                positions.add(STARTX + col * incx); // x
                //the heightmap is 2 squares bigger than the actual
                float currentHeight = getHeight(heightMap[row + 1][col + 1]);
                heights[row][col] = currentHeight;
                positions.add(currentHeight); //y
                positions.add(STARTZ + row * incz); //z

                // Set texture coordinates
                textCoords.add((float) textInc * (float) col / (float) width);
                textCoords.add((float) textInc * (float) row / (float) height);

                // Create indices
                if (col < width - 1 && row < height - 1) {
                    int leftTop = row * width + col;
                    int leftBottom = (row + 1) * width + col;
                    int rightBottom = (row + 1) * width + col + 1;
                    int rightTop = row * width + col + 1;

                    indices.add(leftTop);
                    indices.add(leftBottom);
                    indices.add(rightTop);

                    indices.add(rightTop);
                    indices.add(leftBottom);
                    indices.add(rightBottom);
                }
            }
        }
        float[] posArr = Utils.floatListToArray(positions);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        float[] textCoordsArr = Utils.floatListToArray(textCoords);
        float[] normalsArr = calcNormals(width, height, heightMap);

        FutureTask<Mesh> task = new FutureTask<Mesh>(() -> {
            Mesh mesh = new Mesh(posArr, textCoordsArr, normalsArr, indicesArr);
            Material material = new Material(texture, 0.0f);
            mesh.setMaterial(material);
            return mesh;
        });
        OpenGLThreadExecutorService.getInstance().submit(task);
        try {
            mesh = task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public float getHeight(int row, int col) {
        //clip to array bounds
        row = Integer.min(heights.length - 1, Integer.max(0, row));
        col = Integer.min(heights.length - 1, Integer.max(col, 0));
        return heights[row][col];
    }

    public Mesh getMesh() {
        return mesh;
    }



    private float[] calcNormals(int width, int height, float[][] heightMap) {
        float incx = 1f / (width - 1);
        float incz = 1f/ (height - 1);

        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        Vector3f v12 = new Vector3f();
        Vector3f v23 = new Vector3f();
        Vector3f v34 = new Vector3f();
        Vector3f v41 = new Vector3f();
        List<Float> normals = new ArrayList<>();
        Vector3f normal;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int v0ZI = row + 1;
                int v0XI = col + 1;
                v0.x = STARTX + col * incx;
                v0.z = STARTX + row * incz;
                v0.y = getHeight(heightMap[v0ZI][v0XI]);

                v1.x = v0.x - incx;
                v1.y = getHeight(heightMap[v0ZI][v0XI - 1]);
                v1.z = v0.z;
                v1 = v1.sub(v0);

                v2.x = v0.x;
                v2.y = getHeight(heightMap[v0ZI + 1][v0XI]);
                v2.z = v0.z + incz;
                v2 = v2.sub(v0);

                v3.x = v0.x + incx;
                v3.y = getHeight(heightMap[v0ZI][v0XI + 1]);
                v3.z = v0.z;
                v3 = v3.sub(v0);

                v4.x = v0.x;
                v4.y = getHeight(heightMap[v0ZI - 1][v0XI]);
                v4.z = v0.z - incz;
                v4 = v4.sub(v0);

                v1.cross(v2, v12);
                v12.normalize();

                v2.cross(v3, v23);
                v23.normalize();

                v3.cross(v4, v34);
                v34.normalize();

                v4.cross(v1, v41);
                v41.normalize();

                normal = v12.add(v23).add(v34).add(v41);
                normal.normalize();
                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }
        }
        return Utils.floatListToArray(normals);
    }

    private float getHeight(float y) {
        return this.minY + Math.abs(this.maxY - this.minY) * ((float) y / (float) MAX_COLOUR);
    }
}

