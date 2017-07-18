package gnakcg.engine;

import gnakcg.engine.items.GameItem;

import java.util.*;

/**
 * Represents a scene.
 * Based on the LWJGL Book
 */
public class Scene {

    private Collection<GameItem> gameItems;

    private GameItem skyBox;

    private SceneLight sceneLight;

    private GameItem musicBox;

    public GameItem getMusicBox() {
        return musicBox;
    }

    public void setMusicBox(GameItem musicBox) {
        this.musicBox = musicBox;
    }

    public Collection<GameItem> getGameItems() {
        return new ArrayList<>(gameItems);
    }

    public void setGameItems(Collection<GameItem> gameItems) {
        this.gameItems = gameItems;
    }

    public void cleanup() {
        gameItems.forEach(gameItem -> gameItem.getMesh().cleanUp());
    }

    public GameItem getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(GameItem skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() {
        return sceneLight;
    }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
}
