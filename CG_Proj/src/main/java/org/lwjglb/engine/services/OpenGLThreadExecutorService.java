package org.lwjglb.engine.services;

import org.lwjglb.engine.GameEngine;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * Helper class to run a Task on the OpenGlUI Thread asynchronously.
 */
public class OpenGLThreadExecutorService {

    private static OpenGLThreadExecutorService mExecutor = new OpenGLThreadExecutorService();

    public static OpenGLThreadExecutorService getInstance() {
        return mExecutor;
    }

    private List<FutureTask> queue = new LinkedList<>();

    public synchronized void submit(FutureTask task) {
        //just in case, to prevent deadlocks.
        if (calledFromOpenGlThread()) {
            task.run();
        } else {
            queue.add(task);
        }
    }

    private boolean calledFromOpenGlThread() {
        return Thread.currentThread().getName().equals(GameEngine.OPENGL_THREAD_NAME);
    }

    public Collection<FutureTask> tasksToExecute() {
        List<FutureTask> toReturn = queue;
        queue = new LinkedList<>();
        return toReturn;
    }
}
