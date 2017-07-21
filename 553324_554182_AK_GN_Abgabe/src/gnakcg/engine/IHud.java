package gnakcg.engine;

import gnakcg.engine.items.GameItem;

/**
 * Defines the hud of a game.
 * Taken from the LWJGL Book
 */
public interface IHud {

    GameItem[] getGameItems();

    default void cleanup() {
        GameItem[] gameItems = getGameItems();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}
