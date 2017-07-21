package gnakcg.engine;

/**
 * Abstraction for the game logic.
 * Taken from LWJGL Book
 */
public interface IGameLogic {
    /**
     * Initialize: called once
     */
    void init(Window window) throws Exception;

    /**
     * Called each render cycle. Hopefully > 60 times per second.
     */
    void input(Window window, MouseInput mouseInput);

    /**
     * Called {@link GameEngine#TARGET_UPS} times per second.
     */
    void update(float interval, MouseInput mouseInput);

    /**
     * Called each render cycle. Hopefully > 60 times per second.
     */
    void render(Window window);

    /**
     * Called when the program exits.
     */
    void cleanup();
}