package org.lwjglb.engine;

import org.lwjglb.engine.services.Audio;
import org.lwjglb.engine.services.OpenGLThreadExecutorService;

import java.util.concurrent.FutureTask;

/**
 * The Game Engine. Executes the game loop.
 */
public class GameEngine implements Runnable {
    /**
     * Used to identify the thread with the open gl context.
     */
    public static String OPENGL_THREAD_NAME = "OpenGLMainThread";

    /**
     * Updates per second.
     */
    public static final int TARGET_UPS = 30;

    private final Window window;
    private final Timer timer;
    private final IGameLogic gameLogic;
    private final MouseInput mouseInput;
    private final Audio audio;

    public GameEngine(String windowTitle,IGameLogic gameLogic) throws Exception {
        window = new Window(windowTitle);
        mouseInput = new MouseInput();
        this.gameLogic = gameLogic;
        timer = new Timer();
        audio = Audio.getInstance();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void init() throws Exception {
        audio.init();
        window.init();
        timer.init();
        mouseInput.init(window);
        gameLogic.init(window);

    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        while (!window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }
            runSubmittedTasksOnOpenGlThread();
            render();
        }
    }

    protected void input() {
        mouseInput.input();
        gameLogic.input(window, mouseInput);
    }

    protected void runSubmittedTasksOnOpenGlThread() {
        OpenGLThreadExecutorService executorService = OpenGLThreadExecutorService.getInstance();
        executorService.tasksToExecute().forEach(FutureTask::run);
    }

    protected void update(float interval) {
        gameLogic.update(interval, mouseInput);
    }

    protected void render() {
        gameLogic.render(window);
        window.update();
    }

    protected void cleanup() {
        audio.cleanup();
        gameLogic.cleanup();
    }
}
