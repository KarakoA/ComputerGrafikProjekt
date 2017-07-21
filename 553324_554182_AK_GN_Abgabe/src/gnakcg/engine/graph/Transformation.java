package gnakcg.engine.graph;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import gnakcg.engine.items.GameItem;

/**
 * Class for applying the world matrix/transformations(translation, scale and rotation.)
 * Based on the LWJGL Book.
 */
public class Transformation {
    /**
     * The projection matrix. Calculated anew each render cycle and this way updated if the window was resized.
     * Takes cares of the distance( z coordinate) of the objects.
     * Used in the vertex shader, same for all objects each render cycle.
     */
    private final Matrix4f projectionMatrix;

    private final Matrix4f modelMatrix;

    private final Matrix4f modelViewMatrix;

    private final Matrix4f viewMatrix;

    private final Matrix4f orthoMatrix;


    public Transformation() {
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Updates the projection matrix with the new values. Only width and height change throughout
     * the course of the program. They are equal to the width and height of the window.
     */
    public Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public static Matrix4f updateGenericViewMatrix(Vector3f position, Vector2f rotation, Matrix4f matrix) {
        // First do the rotation so camera rotates over its position
        return matrix.rotationX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .translate(-position.x, -position.y, -position.z);
    }

    /**
     * Updates the view matrix. Updated once per render cycle.
     * The view matrix is used to simulate a camera movement in OpenGL by moving all objects
     * in the opposite direction of the cameraq.
     */
    public Matrix4f updateViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector2f rotation = camera.getRotation();

        viewMatrix.identity();
        // First do the rotation so camera rotates over its position
        return updateGenericViewMatrix(cameraPos, rotation, viewMatrix);
    }

    /**
     * Projects 3d items on a 2d plane. Used in the HUD.
     */
    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    /**
     * Builds the model view matrix for the given item. That is: applies the transformation, scale and rotation
     * of the game items and returns it as a matrix. Used by the vertex shader to compute the world
     * coordinates of an item.
     */
    public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {
        Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float) Math.toRadians(-rotation.x)).
                rotateY((float) Math.toRadians(-rotation.y)).
                rotateZ((float) Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        modelViewMatrix.set(viewMatrix);
        return modelViewMatrix.mul(modelMatrix);
    }
}
