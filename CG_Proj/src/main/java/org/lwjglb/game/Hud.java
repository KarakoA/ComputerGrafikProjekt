package org.lwjglb.game;

import org.joml.Vector4f;
import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.IHud;
import org.lwjglb.engine.Window;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.OBJLoader;

public class Hud implements IHud {
    private final GameItem[] gameItems;

    private final GameItem compassItem;

    public Hud() throws Exception {
        // Create compass
        Mesh mesh = OBJLoader.loadMesh("/models/compass.obj");
        Material material = new Material();
        material.setAmbientColour(new Vector4f(1, 0, 0, 1));
        mesh.setMaterial(material);
        compassItem = new GameItem(mesh);
        compassItem.setScale(40.0f);
        // Rotate to transform it to screen coordinates
        compassItem.setRotation(0f, 0f, 180f);

        // Create list that holds the items that compose the HUD
        gameItems = new GameItem[]{compassItem};
    }

    public void rotateCompass(float angle) {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }
   
    public void updateSize(Window window) {
        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
    }
}
