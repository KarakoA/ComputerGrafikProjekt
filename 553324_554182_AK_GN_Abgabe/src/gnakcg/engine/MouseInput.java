package gnakcg.engine;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Encapsulates mouse input.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class MouseInput {
    private final float SENSITIVITY = 5f;

    private final Vector2f currentPos;
    private Vector2f rotation;

    public MouseInput() {
        currentPos = new Vector2f();
        rotation = new Vector2f(10, 0);
    }

    public Vector2f getRotation() {
        return rotation;
    }

    private Window window;

    private void centerMouse() {
        glfwSetCursorPos(window.getWindowHandle(), window.getWidth() / 2f, window.getHeight() / 2f);
    }

    public void init(Window window) {
        glfwSetInputMode(window.getWindowHandle(),GLFW_CURSOR,GLFW_CURSOR_HIDDEN);
        this.window = window;
        centerMouse();
        currentPos.x = window.getWidth() / 2f;
        currentPos.y = window.getHeight() / 2f;
        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {
            currentPos.x = (float) xpos;
            currentPos.y = (float) ypos;
        });
    }

    public void input() {
        float halfWidth = window.getWidth() / 2f;
        float halfHeight = window.getHeight() / 2f;
        float dx = currentPos.x - halfWidth;
        float dy = currentPos.y - halfHeight;
        //the menu bar width
        if (dy == -0.5)
            dy = 0;

        rotation.y += dx / SENSITIVITY;
        rotation.x -= dy / SENSITIVITY;

        rotation.x = Math.min(90, Math.max(rotation.x, -90));
        centerMouse();
    }
}
