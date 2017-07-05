package org.lwjglb.engine.items;

import javafx.util.Pair;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjglb.engine.graph.HeightMapMesh;
import org.lwjglb.engine.items.GameItem;

import java.util.Arrays;
import java.util.Collection;

import static org.lwjglb.engine.items.Terrain.CHUNK_SIZE;


public class Chunk extends GameItem {
    private Vector2i chunkPosition;
    private HeightMapMesh heightMapMesh;

    public Vector2i getChunkPosition() {
        return chunkPosition;
    }

    public Chunk(Vector2i chunkPosition, HeightMapMesh mesh) {
        super(mesh.getMesh());
        this.chunkPosition = chunkPosition;
        this.heightMapMesh=mesh;
    }

    public Collection<Vector2i> getAdjacentChunksPositions() {
        int x = chunkPosition.x;
        int y = chunkPosition.y;
        Vector2i[] res = new Vector2i[]{new Vector2i(x, y - 1), new Vector2i(x, y + 1), new Vector2i(x + 1, y), new Vector2i(x + 1, y + 1), new Vector2i(x + 1, y - 1),
                new Vector2i(x - 1, y), new Vector2i(x - 1, y - 1), new Vector2i(x - 1, y + 1)};
        return Arrays.asList(res);
    }

    //returns the hegiht in world coordinates of a given point in world coordinates
    public float getHeight(float x, float z) {
        Vector2f coordinate = convertToChunkLocalCoordinate(x, z);

        Vector3f[] triangle = getTriangles(coordinate);
        float result = interpolateHeight(triangle[0], triangle[1], triangle[2], x, z);

        return result;
    }

    private int toHeightMapIndex(float c) {
        return (int) (c * CHUNK_SIZE);
    }

    private float getCoordinateInWorldScale(int index, boolean isX) {
        float tmp = (float) index / CHUNK_SIZE * this.getScale();
        tmp = isX ? tmp + this.getPosition().x : tmp + this.getPosition().z;
        return tmp;
    }

    private Vector2f convertToChunkLocalCoordinate(float x, float z) {
        float xChunk = chunkPosition.x;
        float zChunk = chunkPosition.y;
        float scale = this.getScale();
        //0-1
        x = Float.min(1, Float.max(0, (x - xChunk * scale) / scale));
        z = Float.min(1, Float.max(0, (z - zChunk * scale) / scale));
        return new Vector2f(x, z);

    }

    protected Vector3f[] getTriangles(Vector2f localCoordinate) {
        int col = toHeightMapIndex(localCoordinate.x);
        int row = toHeightMapIndex(localCoordinate.y);

        Vector3f[] triangle = new Vector3f[3];
        triangle[1] = new Vector3f(
                getCoordinateInWorldScale(col, true),
                getWorldHeight(row + 1, col),
                getCoordinateInWorldScale(row + 1, false));
        triangle[2] = new Vector3f(
                getCoordinateInWorldScale(col + 1, true),
                getWorldHeight(row, col + 1),
                getCoordinateInWorldScale(row, false));
//removed a * scale here
        if (getCoordinateInWorldScale(row, false)
                < getDiagonalZCoord(triangle[1].x, triangle[1].z, triangle[2].x, triangle[2].z, getCoordinateInWorldScale(col, true))) {
            triangle[0] = new Vector3f(
                    getCoordinateInWorldScale(col, true),
                    getWorldHeight(row, col),
                    getCoordinateInWorldScale(row, false));
        } else {
            triangle[0] = new Vector3f(
                    getCoordinateInWorldScale((col + 1), true),
                    getWorldHeight(row + 2, col + 1),
                    getCoordinateInWorldScale((row + 1), false));
        }

        return triangle;
    }

    private float getDiagonalZCoord(float x1, float z1, float x2, float z2, float x) {
        float z = ((z1 - z2) / (x1 - x2)) * (x - x1) + z1;
        return z;
    }

    private float getWorldHeight(int row, int col) {

        float y = heightMapMesh.getHeight(row, col);
        return y * this.getScale() + this.getPosition().y;
    }

    private float interpolateHeight(Vector3f pA, Vector3f pB, Vector3f pC, float x, float z) {
        // Plane equation ax+by+cz+d=0
        float a = (pB.y - pA.y) * (pC.z - pA.z) - (pC.y - pA.y) * (pB.z - pA.z);
        float b = (pB.z - pA.z) * (pC.x - pA.x) - (pC.z - pA.z) * (pB.x - pA.x);
        float c = (pB.x - pA.x) * (pC.y - pA.y) - (pC.x - pA.x) * (pB.y - pA.y);
        float d = -(a * pA.x + b * pA.y + c * pA.z);
        // y = (-d -ax -cz) / b
        float y = (-d - a * x - c * z) / b;
        return y;
    }
}