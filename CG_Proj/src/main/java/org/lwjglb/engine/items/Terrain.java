package org.lwjglb.engine.items;

import org.joml.Vector2i;
import org.lwjglb.engine.graph.HeightMapMesh;
import org.lwjglb.engine.graph.Texture;
import org.lwjglb.engine.mapgenerator.HeightMapGeneratorMultiThreaded;

import java.util.*;



public class Terrain {
    public static final int CHUNK_SIZE = 256;
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
        HeightMapGeneratorMultiThreaded generator = new HeightMapGeneratorMultiThreaded(seed, 150, 8, 2, 0.5f);
        Texture texture = new Texture(textureFile);
        updater = new ChunkUpdater(scale, minY, maxY, textInc, texture, generator);

        currentChunk=updater.createChunk(new Vector2i(0,0));
        chunkPositionToChunksMap.put(new Vector2i(0,0),currentChunk);
        //create the adjacent chunks
        new Thread(updater).start();
    }

    public float getHeight(float x, float z) {
        //no initilized yet
        if (currentChunk == null)
            return 0;
        return currentChunk.getHeight(x, z);
    }

    private class ChunkUpdater implements Runnable {

        private final float scale;
        private final float minY;
        private final float maxY;
        private final int textInc;
        private final Texture texture;
        private final HeightMapGeneratorMultiThreaded generator;

        public ChunkUpdater(float scale, float minY, float maxY, int textInc, Texture texture, HeightMapGeneratorMultiThreaded generator) {
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
            float[][] heightMap = generator.generate(chunkPosition.y * (CHUNK_SIZE - 1), chunkPosition.x * (CHUNK_SIZE - 1), CHUNK_SIZE, CHUNK_SIZE);
            HeightMapMesh heightMapMesh = new HeightMapMesh(minY, maxY, heightMap, texture, textInc);

            float xDisplacement = chunkPosition.x * scale * HeightMapMesh.getXLength();
            float zDisplacement = chunkPosition.y * scale * HeightMapMesh.getZLength();

            Chunk terrainChunk = new Chunk(chunkPosition,heightMapMesh);
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
