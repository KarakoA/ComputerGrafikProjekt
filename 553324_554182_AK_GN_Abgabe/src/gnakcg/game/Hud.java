package gnakcg.game;

import gnakcg.engine.IHud;
import gnakcg.utils.ResourceLoader;
import gnakcg.engine.Window;
import gnakcg.engine.graph.Mesh;
import gnakcg.engine.items.GameItem;
import gnakcg.engine.loaders.StaticMeshesLoader;
import org.joml.Vector4f;

/**
 * Implementation of the game hud.
 * Based on the LWJGL Book
 */
public class Hud implements IHud {
    private final GameItem[] gameItems;

    private final GameItem compassItem;

    public Hud() throws Exception {
        // Create compass
        String path = ResourceLoader.getInstance().getResourcePath("/models/compass/compass.obj");
        Mesh[] meshes = (StaticMeshesLoader.load(path, "/models/compass"));
        compassItem = new GameItem(meshes);
        compassItem.getMesh().getMaterial().setColour(new Vector4f(1, 0, 0, 1));
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
