package org.lwjglb.engine.items;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjglb.engine.graph.HeightMapMesh;
import org.lwjglb.engine.graph.Texture;
import org.lwjglb.engine.mapgenerator.HeightMapGenerator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class Terrain {
    public static final int CHUNK_SIZE = 256;
    private final float MUSIC_BOX_DISTANCE_FACTOR = 0.75f;
    private final Map<Vector2i, Chunk> chunkPositionToChunksMap;
    private final ChunkUpdater updater;

    private final Set<GameItem> gameItemsToRender = Collections.synchronizedSet(new HashSet<>());

    public Collection<GameItem> getGameItems() {
        return gameItemsToRender;
    }

    private Chunk currentChunk;

    public synchronized Vector2i getCurrentChunkPosition() {
        return currentChunk.getChunkPosition();
    }

    public void setCurrentChunkPosition(Vector2i newChunkPosition) {
        if (newChunkPosition.equals(this.currentChunk.getChunkPosition()))
            return;
        this.currentChunk = chunkPositionToChunksMap.get(newChunkPosition);
        new Thread(updater).start();
    }

    public Terrain(float scale, float minY, float maxY, String textureFile, int textInc) throws Exception {
        chunkPositionToChunksMap = Collections.synchronizedMap(new HashMap<>());

        int seed = 250;//150;
        HeightMapGenerator generator = new HeightMapGenerator(seed, 150, 8, 2, 0.5f);
        //HeightMapGenerator generator = new HeightMapGenerator(seed, 150, 0, 2, 0.5f);
        Texture texture = new Texture(textureFile);
        updater = new ChunkUpdater(scale, minY, maxY, textInc, texture, generator);

        currentChunk = updater.createChunk(new Vector2i(0, 0));
        chunkPositionToChunksMap.put(new Vector2i(0, 0), currentChunk);
        //create the adjacent chunks
        new Thread(updater).start();
    }

    public float getHeight(float x, float z) {
        return currentChunk.getHeight(x, z);
    }

    public Vector3f getMusicBoxPosition(float terrainScale, float cameraStepSize, float songLengthInMiliSeconds) {
        //find and create the chunk
        Vector2i chunkCoordinates = generateBoxChunkCoordinates(terrainScale, cameraStepSize, songLengthInMiliSeconds);
        Chunk chunkWithMusicBox = updater.createChunk(chunkCoordinates);
        chunkPositionToChunksMap.put(chunkCoordinates, chunkWithMusicBox);

        //position within the chunk
        Random r = ThreadLocalRandom.current();
        float chunkLocalX = r.nextInt(CHUNK_SIZE) / (float) CHUNK_SIZE;
        float chunkLocalZ = r.nextInt(CHUNK_SIZE) / (float) CHUNK_SIZE;
        float y = chunkWithMusicBox.getHeightFromChunkLocalCoordinates(chunkLocalX, chunkLocalZ);
        return new Vector3f(chunkCoordinates.x + chunkLocalX, y, chunkCoordinates.y + chunkLocalZ);
    }

    private Vector2i generateBoxChunkCoordinates(float terrainScale, float cameraStepSize, float songLengthInMiliSeconds) {
        float songLengthInSeconds = songLengthInMiliSeconds / 1000;
        //we assume the user can press a direction button 5 times per second at maximum
        float maxDistancePerSecond = terrainScale * cameraStepSize * 5;
        float maximumDistanceInStraightLineForSongDuration = maxDistancePerSecond * songLengthInSeconds;
        int maxChunk = (int) (MUSIC_BOX_DISTANCE_FACTOR * maximumDistanceInStraightLineForSongDuration / terrainScale);

        Random r = ThreadLocalRandom.current();
        int signX = r.nextInt() > 0 ? 1 : -1;
        int signZ = r.nextInt() > 0 ? 1 : -1;
        int x = (maxChunk + r.nextInt(4) - 2) * signX;
        int z = (maxChunk + r.nextInt(4) - 2) * signZ;
        return new Vector2i(x, z);
    }

    private class ChunkUpdater implements Runnable {

        private final float scale;
        private final float minY;
        private final float maxY;
        private final int textInc;
        private final Texture texture;
        private final HeightMapGenerator generator;

        public ChunkUpdater(float scale, float minY, float maxY, int textInc, Texture texture, HeightMapGenerator generator) {
            this.scale = scale;
            this.minY = minY;
            this.maxY = maxY;
            this.textInc = textInc;
            this.texture = texture;
            this.generator = generator;
        }

        @Override
        public void run() {
            updateGameItemsToRender();
        }

        private Chunk createChunk(Vector2i chunkPosition) {
//            float[][] heightMap = generator.generate(chunkPosition.y * (CHUNK_SIZE - 1), chunkPosition.x * (CHUNK_SIZE - 1), CHUNK_SIZE, CHUNK_SIZE);
           // HeightMapMesh heightMapMesh = new HeightMapMesh(minY, maxY, heightMap, texture, textInc);
            float[][] heightMap = generator.generate(chunkPosition.y * (CHUNK_SIZE - 1)-1, chunkPosition.x * (CHUNK_SIZE - 1)-1, CHUNK_SIZE+2, CHUNK_SIZE+2);
             HeightMapMesh heightMapMesh = new HeightMapMesh(minY, maxY, heightMap,CHUNK_SIZE,CHUNK_SIZE, texture, textInc);
            float xDisplacement = chunkPosition.x * scale;
            float zDisplacement = chunkPosition.y * scale;

            Chunk terrainChunk = new Chunk(chunkPosition, heightMapMesh);
            terrainChunk.setScale(scale);
            terrainChunk.setPosition(xDisplacement, 0, zDisplacement);
            return terrainChunk;
        }

        private void updateGameItemsToRender() {

            Vector2i current = getCurrentChunkPosition();

            Collection<Vector2i> adjacentChunkPositions = currentChunk.getAdjacentChunksPositions();

            adjacentChunkPositions.forEach(this::createChunkIfNecessary);

            List<GameItem> newGameItems = new LinkedList<>();

            for (Vector2i adjacentChunkPosition : adjacentChunkPositions) {
                GameItem chunk = chunkPositionToChunksMap.get(adjacentChunkPosition);
                newGameItems.add(chunk);
            }

            newGameItems.add(chunkPositionToChunksMap.get(current));
            //remove the old ones and afterwards add all new
            gameItemsToRender.removeIf((item) -> !newGameItems.contains(item));
            gameItemsToRender.addAll(newGameItems);
        }

        private void createChunkIfNecessary(Vector2i chunkPosition) {
            if (!chunkPositionToChunksMap.containsKey(chunkPosition))
                chunkPositionToChunksMap.put(chunkPosition, createChunk(chunkPosition));
        }
    }
}
