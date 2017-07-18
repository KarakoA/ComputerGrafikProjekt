package gnakcg.game;

import java.io.File;
import java.nio.file.Paths;

import gnakcg.engine.IHud;
import gnakcg.engine.Utils;
import gnakcg.engine.Window;
import gnakcg.engine.graph.Mesh;
import gnakcg.engine.items.GameItem;
import gnakcg.engine.loaders.StaticMeshesLoader;
import org.joml.Vector4f;

/**
 * Implementation of the game hud.
 * Based on <a href="https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter12/chapter12.html">LWJGL Book Chapter 12</a>
 */
public class Hud implements IHud {
    private final GameItem[] gameItems;

    private final GameItem compassItem;

    public Hud() throws Exception {
        // Create compass
        String path = Utils.getResourceAbsolutePath("/models/compass/compass.obj");
        Mesh[] meshes = (StaticMeshesLoader.load(path, "/models/compass"));
        compassItem = new GameItem(meshes);
        compassItem.getMesh().getMaterial().setAmbientColour(new Vector4f(1, 0, 0, 1));
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
